package com.alaka_ala.florafilm.utils.jacred;

import android.os.Handler;
import android.os.Looper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class JacredParser {

    private final OkHttpClient okHttpClient;
    private final Gson gson;

    public JacredParser() {
        this.okHttpClient = new OkHttpClient.Builder().build();
        this.gson = new Gson();
    }

    /**
     * Асинхронный поиск торрентов по Kinopoisk ID
     *
     * @param kinopoiskId ID фильма с Кинопоиска (например: 666)
     * @param domain      Домен сайта (например: "jac-red.ru"). Если null или пустой — используется jac-red.ru
     * @param callback    Колбэк, в который придёт результат (всегда на главном потоке)
     */
    public void searchTorrents(long kinopoiskId, String domain, TorrentCallback callback) {
        String finalDomain = (domain == null || domain.trim().isEmpty()) ? "jac-red.ru" : domain.trim().toLowerCase();

        String url = "https://" + finalDomain +
                "/api/v1.0/torrents?search=kp" + kinopoiskId +
                "&sort=update&exact=true";

        Request request = new Request.Builder()
                .url(url)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postToMainThread(() -> callback.onError("Сетевая ошибка: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    postToMainThread(() -> callback.onError("HTTP ошибка: " + response.code()));
                    response.close();
                    return;
                }

                String jsonResponse;
                try {
                    jsonResponse = response.body().string();
                } catch (IOException e) {
                    postToMainThread(() -> callback.onError("Ошибка чтения ответа от сервера"));
                    response.close();
                    return;
                } finally {
                    response.close();
                }

                try {
                    Type listType = new TypeToken<List<TorrentModel>>() {}.getType();
                    List<TorrentModel> torrents = gson.fromJson(jsonResponse, listType);

                    if (torrents == null) {
                        torrents = Collections.emptyList();
                    }

                    List<TorrentModel> finalTorrents = torrents;
                    postToMainThread(() -> callback.onSuccess(finalTorrents));

                } catch (Exception e) {
                    postToMainThread(() -> callback.onError("Ошибка парсинга JSON: " + e.getMessage()));
                }
            }
        });
    }

    /**
     * Выполняет Runnable на главном потоке (UI thread)
     */
    private void postToMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
