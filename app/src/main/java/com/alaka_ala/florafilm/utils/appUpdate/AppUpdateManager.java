package com.alaka_ala.florafilm.utils.appUpdate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.alaka_ala.florafilm.BuildConfig;
import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.utils.appUpdate.models.Element;
import com.alaka_ala.florafilm.utils.appUpdate.models.OutputMetadata;
import com.alaka_ala.florafilm.utils.appUpdate.models.UpdateInfo;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Keep
public class AppUpdateManager {
    // Открытый репозиторий - токен не требуется
    private static final String METADATA_URL = "https://raw.githubusercontent.com/VarnavskyAV/XFloraFilm/refs/heads/master/app/release/output-metadata.json";
    private static final String APK_BASE_URL = "https://github.com/VarnavskyAV/XFloraFilm/raw/refs/heads/master/app/release/app-release.apk";

    private static AppUpdateManager INSTANCE;

    private Context context;
    private OkHttpClient httpClient;
    private volatile ExecutorService executorService;
    private Handler mainHandler;
    private DownloadCallback currentCallback;

    // Флаги для синхронизации
    private final AtomicBoolean isCheckingForUpdate = new AtomicBoolean(false);
    private final AtomicBoolean isDownloading = new AtomicBoolean(false);
    private final Object executorLock = new Object();

    public static AppUpdateManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppUpdateManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppUpdateManager(context);
                }
            }
        }
        return INSTANCE;
    }

    private AppUpdateManager(Context context) {
        this.context = context.getApplicationContext();

        // Для открытого репозитория не нужна авторизация
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        this.mainHandler = new Handler(Looper.getMainLooper());
        initExecutor();
    }

    private void initExecutor() {
        synchronized (executorLock) {
            if (executorService == null || executorService.isShutdown() || executorService.isTerminated()) {
                executorService = Executors.newSingleThreadExecutor();
            }
        }
    }

    private ExecutorService getExecutor() {
        synchronized (executorLock) {
            if (executorService == null || executorService.isShutdown() || executorService.isTerminated()) {
                executorService = Executors.newSingleThreadExecutor();
            }
            return executorService;
        }
    }

    public interface UpdateCheckCallback {
        void onUpdateAvailable(UpdateInfo updateInfo);
        void onNoUpdateAvailable();
        void onError(String errorMessage);
    }

    public interface DownloadCallback {
        void onDownloadStart();
        void onDownloadProgress(int progress);
        void onDownloadComplete(File apkFile);
        void onDownloadError(String error);
        void onInstallPermissionRequired();
    }

    // Основной метод проверки обновления
    public void checkForUpdate(UpdateCheckCallback callback) {
        // Проверяем, не выполняется ли уже проверка
        if (!isCheckingForUpdate.compareAndSet(false, true)) {
            mainHandler.post(() -> callback.onError("Проверка обновления уже выполняется"));
            return;
        }

        try {
            getExecutor().execute(() -> {
                try {
                    OutputMetadata metadata = downloadMetadata();
                    if (metadata.getElements() != null && !metadata.getElements().isEmpty()) {
                        Element latestElement = metadata.getElements().get(0);
                        int remoteVersionCode = latestElement.getVersionCode();
                        int currentVersionCode = getCurrentVersionCode();

                        if (remoteVersionCode > currentVersionCode) {
                            // Для открытого репозитория используем прямой URL к APK
                            String apkDownloadUrl = APK_BASE_URL;
                            long fileSize = getFileSize(apkDownloadUrl);

                            UpdateInfo updateInfo = new UpdateInfo(
                                    remoteVersionCode,
                                    latestElement.getVersionName(),
                                    apkDownloadUrl,
                                    "Доступна новая версия\n" + BuildConfig.VERSION_NAME + " > " + latestElement.getVersionName(),
                                    fileSize
                            );

                            mainHandler.post(() -> callback.onUpdateAvailable(updateInfo));
                        } else {
                            mainHandler.post(callback::onNoUpdateAvailable);
                        }
                    } else {
                        mainHandler.post(() -> callback.onError("Нет данных о версиях"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mainHandler.post(() -> callback.onError(e.getMessage()));
                } finally {
                    isCheckingForUpdate.set(false);
                }
            });
        } catch (Exception e) {
            isCheckingForUpdate.set(false);
            mainHandler.post(() -> callback.onError("Ошибка: " + e.getMessage()));
        }
    }

    @Keep
    private OutputMetadata downloadMetadata() throws IOException {
        Request request = new Request.Builder()
                .url(METADATA_URL)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ошибка загрузки metadata: " + response.code());
            }

            String jsonString = response.body().string();
            Gson gson = new Gson();
            return gson.fromJson(jsonString, OutputMetadata.class);
        }
    }

    // Получение размера файла
    private long getFileSize(String url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .head()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String contentLength = response.header("Content-Length");
                    return contentLength != null ? Long.parseLong(contentLength) : 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Проверка разрешения на установку
    public boolean canInstallPackages() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return context.getPackageManager().canRequestPackageInstalls();
        }
        return true;
    }

    // Скачивание обновления
    public void downloadUpdate(UpdateInfo updateInfo, DownloadCallback callback) {
        // Проверяем, не выполняется ли уже загрузка
        if (!isDownloading.compareAndSet(false, true)) {
            mainHandler.post(() -> callback.onDownloadError("Загрузка уже выполняется"));
            return;
        }

        this.currentCallback = callback;

        // Проверяем разрешение на установку
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !canInstallPackages()) {
            mainHandler.post(() -> {
                callback.onInstallPermissionRequired();
                isDownloading.set(false);
            });
            return;
        }

        try {
            getExecutor().execute(() -> {
                File outputFile = null;
                try {
                    mainHandler.post(() -> callback.onDownloadStart());

                    String fileName = "app-update-" + updateInfo.getVersionName() + ".apk";
                    File downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

                    if (downloadDir == null) {
                        downloadDir = context.getFilesDir();
                    }

                    if (!downloadDir.exists()) {
                        downloadDir.mkdirs();
                    }

                    outputFile = new File(downloadDir, fileName);

                    if (outputFile.exists()) {
                        outputFile.delete();
                    }

                    Request request = new Request.Builder()
                            .url(updateInfo.getDownloadUrl())
                            .build();

                    try (Response response = httpClient.newCall(request).execute()) {
                        if (!response.isSuccessful()) {
                            throw new IOException("Ошибка загрузки: " + response.code());
                        }

                        long contentLength = response.body().contentLength();
                        InputStream inputStream = response.body().byteStream();
                        FileOutputStream outputStream = new FileOutputStream(outputFile);

                        byte[] buffer = new byte[8192];
                        long totalBytesRead = 0;
                        int bytesRead;
                        int lastProgress = -1;

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            // Проверяем, не был ли отменен процесс загрузки
                            if (!isDownloading.get()) {
                                outputStream.close();
                                inputStream.close();
                                outputFile.delete();
                                return;
                            }

                            outputStream.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;

                            if (contentLength > 0) {
                                int progress = (int) ((totalBytesRead * 100) / contentLength);
                                if (progress != lastProgress) {
                                    lastProgress = progress;
                                    final int finalProgress = progress;
                                    mainHandler.post(() -> {
                                        if (currentCallback != null) {
                                            currentCallback.onDownloadProgress(finalProgress);
                                        }
                                    });
                                }
                            }
                        }

                        outputStream.flush();
                        outputStream.close();
                        inputStream.close();

                        final File finalOutputFile = outputFile;
                        mainHandler.post(() -> {
                            if (currentCallback != null) {
                                currentCallback.onDownloadComplete(finalOutputFile);
                            }
                            installApk(finalOutputFile);
                            isDownloading.set(false);
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (outputFile != null && outputFile.exists()) {
                        outputFile.delete();
                    }
                    mainHandler.post(() -> {
                        if (currentCallback != null) {
                            currentCallback.onDownloadError(e.getMessage());
                        }
                        isDownloading.set(false);
                    });
                }
            });
        } catch (Exception e) {
            isDownloading.set(false);
            mainHandler.post(() -> callback.onDownloadError("Ошибка: " + e.getMessage()));
        }
    }

    // Отмена загрузки
    public void cancelDownload() {
        isDownloading.set(false);
    }

    // Проверка, идет ли загрузка
    public boolean isDownloading() {
        return isDownloading.get();
    }

    // Проверка, идет ли проверка обновления
    public boolean isChecking() {
        return isCheckingForUpdate.get();
    }

    // Установка APK файла
    private void installApk(File apkFile) {
        try {
            if (!apkFile.exists()) {
                showToast("APK файл не найден");
                return;
            }

            Uri apkUri;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                apkUri = FileProvider.getUriForFile(context,
                        context.getPackageName() + ".fileprovider", apkFile);
            } else {
                apkUri = Uri.fromFile(apkFile);
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Для Android 8+ дополнительная проверка
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (canInstallPackages()) {
                    context.startActivity(intent);
                } else {
                    // Запрашиваем разрешение
                    if (currentCallback != null) {
                        currentCallback.onInstallPermissionRequired();
                    }
                }
            } else {
                context.startActivity(intent);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showToast("Ошибка установки: " + e.getMessage());
        }
    }

    private static final int REQUEST_INSTALL_PERMISSION = 1001;

    public void requestInstallPermission(Activity activity) {
        MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(activity)
                .setTitle("Требуется разрешение")
                .setMessage("Разрешите установку из неизвестных источников")
                .setPositiveButton("К настройкам", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivityForResult(intent, REQUEST_INSTALL_PERMISSION);
                })
                .setNegativeButton("Отмена", null);
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    // Получение текущей версии приложения
    private int getCurrentVersionCode() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return (int) context.getPackageManager()
                        .getPackageInfo(context.getPackageName(), 0)
                        .getLongVersionCode();
            } else {
                return context.getPackageManager()
                        .getPackageInfo(context.getPackageName(), 0)
                        .versionCode;
            }
        } catch (Exception e) {
            return 1;
        }
    }

    private void showToast(String message) {
        mainHandler.post(() ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show());
    }

    public void cleanup() {
        synchronized (executorLock) {
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }
        }
    }



}