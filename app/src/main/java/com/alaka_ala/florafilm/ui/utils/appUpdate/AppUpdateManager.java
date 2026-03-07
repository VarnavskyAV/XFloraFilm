package com.alaka_ala.florafilm.ui.utils.appUpdate;

import android.app.DownloadManager;
import android.content.*;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.alaka_ala.florafilm.ui.utils.appUpdate.models.Element;
import com.alaka_ala.florafilm.ui.utils.appUpdate.models.OutputMetadata;
import com.alaka_ala.florafilm.ui.utils.appUpdate.models.UpdateInfo;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppUpdateManager {
    private static final String METADATA_URL = "https://raw.githubusercontent.com/VarnavskyAV/XFloraFilm/refs/heads/master/app/release/output-metadata.json";
    private static final String APK_BASE_URL = "https://github.com/VarnavskyAV/XFloraFilm/raw/refs/heads/master/app/release";

    private static AppUpdateManager INSTANCE;

    private Context context;
    private DownloadManager downloadManager;
    private OkHttpClient httpClient;
    private ExecutorService executorService;
    private Handler mainHandler;
    private long currentDownloadId = -1;
    private BroadcastReceiver downloadCompleteReceiver;

    public static AppUpdateManager getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AppUpdateManager(context);
        }
        return INSTANCE;
    }

    private AppUpdateManager(Context context) {
        this.context = context.getApplicationContext();
        this.downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        this.httpClient = new OkHttpClient();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public interface UpdateCheckCallback {
        void onUpdateAvailable(UpdateInfo updateInfo);
        void onNoUpdateAvailable();
        void onError(String errorMessage);
    }

    // Основной метод проверки обновления
    public void checkForUpdate(UpdateCheckCallback callback) {
        executorService.execute(() -> {
            try {
                // Загружаем и парсим metadata.json
                OutputMetadata metadata = downloadMetadata();
                if (metadata.getElements() != null && !metadata.getElements().isEmpty()) {
                    Element latestElement = metadata.getElements().get(0);
                    int remoteVersionCode = latestElement.getVersionCode();
                    int currentVersionCode = getCurrentVersionCode();

                    // Проверяем, есть ли новая версия
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

    // Загрузка APK файла
    public long downloadUpdate(UpdateInfo updateInfo) {
        String fileName = "app-update-" + updateInfo.getVersionName() + ".apk";

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(updateInfo.getDownloadUrl()))
                .setTitle("Обновление приложения до v" + updateInfo.getVersionName())
                .setDescription("Загрузка новой версии")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true);

        long downloadId = downloadManager.enqueue(request);
        currentDownloadId = downloadId;

        // Регистрируем BroadcastReceiver для обработки завершения загрузки
        downloadCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == currentDownloadId) {
                    installUpdate(id);
                    try {
                        context.unregisterReceiver(this);
                    } catch (Exception e) {
                        // Уже unregistered
                    }
                }
            }
        };

        context.registerReceiver(downloadCompleteReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        return downloadId;
    }

    // Установка загруженного APK
    public void installUpdate(long downloadId) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        try (android.database.Cursor cursor = downloadManager.query(query)) {
            if (cursor.moveToFirst()) {
                int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    Uri apkUri = downloadManager.getUriForDownloadedFile(downloadId);
                    if (apkUri != null) {
                        installApk(apkUri);
                    }
                }
            }
        }
    }

    private void installApk(Uri apkUri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showToast("Не найдено приложение для установки APK");
        } catch (Exception e) {
            showToast("Ошибка установки: " + e.getMessage());
        }
    }

    // Получение текущей версии приложения
    private int getCurrentVersionCode() {
        try {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .versionCode;
        } catch (Exception e) {
            return 1;
        }
    }

    private void showToast(String message) {
        mainHandler.post(() ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show());
    }

    public void cleanup() {
        if (downloadCompleteReceiver != null) {
            try {
                context.unregisterReceiver(downloadCompleteReceiver);
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}