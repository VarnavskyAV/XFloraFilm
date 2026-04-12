package com.alaka_ala.florafilm.utils.appUpdate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.alaka_ala.florafilm.utils.appUpdate.models.Element;
import com.alaka_ala.florafilm.utils.appUpdate.models.OutputMetadata;
import com.alaka_ala.florafilm.utils.appUpdate.models.UpdateInfo;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class App2UpdateManager {
    // Github токен
    private static final String GITHUB_TOKEN = "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";
    private static final String METADATA_URL = "https://raw.githubusercontent.com/VarnavskyAV/XFloraFilm/refs/heads/master/app/release/output-metadata.json";
    private static final String APK_BASE_URL = "https://raw.githubusercontent.com/VarnavskyAV/XFloraFilm/refs/heads/master/app/release";

    private static App2UpdateManager INSTANCE;

    private Context context;
    private OkHttpClient httpClient;
    private ExecutorService executorService;
    private Handler mainHandler;

    public static App2UpdateManager getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new App2UpdateManager(context);
        }
        return INSTANCE;
    }

    private App2UpdateManager(Context context) {
        this.context = context.getApplicationContext();

        // Настройка OkHttpClient с токеном
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Authorization", "token " + GITHUB_TOKEN)
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .build();

        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
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
    }

    // Основной метод проверки обновления
    public void checkForUpdate(UpdateCheckCallback callback) {
        executorService.execute(() -> {
            try {
                OutputMetadata metadata = downloadMetadata();
                if (metadata.getElements() != null && !metadata.getElements().isEmpty()) {
                    Element latestElement = metadata.getElements().get(0);
                    int remoteVersionCode = latestElement.getVersionCode();
                    int currentVersionCode = getCurrentVersionCode();

                    if (remoteVersionCode > currentVersionCode) {
                        String apkDownloadUrl = APK_BASE_URL + "/" + latestElement.getOutputFile();
                        long fileSize = getFileSize(apkDownloadUrl);

                        UpdateInfo updateInfo = new UpdateInfo(
                                remoteVersionCode,
                                latestElement.getVersionName(),
                                apkDownloadUrl,
                                "Доступна новая версия " + latestElement.getVersionName(),
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
            }
        });
    }

    // Загрузка и парсинг metadata.json
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

    // Скачивание обновления
    public void downloadUpdate(UpdateInfo updateInfo, DownloadCallback callback) {
        executorService.execute(() -> {
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

                File outputFile = new File(downloadDir, fileName);

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
                        outputStream.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;

                        if (contentLength > 0) {
                            int progress = (int) ((totalBytesRead * 100) / contentLength);
                            if (progress != lastProgress) {
                                lastProgress = progress;
                                final int finalProgress = progress;
                                mainHandler.post(() -> callback.onDownloadProgress(finalProgress));
                            }
                        }
                    }

                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();

                    mainHandler.post(() -> {
                        callback.onDownloadComplete(outputFile);
                        installApk(outputFile);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onDownloadError(e.getMessage()));
            }
        });
    }

    // Установка APK файла
    private void installApk(File apkFile) {
        try {
            if (!apkFile.exists()) {
                showToast("APK файл не найден");
                return;
            }

            Uri apkUri;

            apkUri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider", apkFile);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Ошибка установки: " + e.getMessage());
        }
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
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}