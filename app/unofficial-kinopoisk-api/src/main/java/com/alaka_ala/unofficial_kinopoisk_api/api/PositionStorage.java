package com.alaka_ala.unofficial_kinopoisk_api.api;

import android.os.Handler;

import com.alaka_ala.unofficial_kinopoisk_api.db.WatchPositionDao;
import com.alaka_ala.unofficial_kinopoisk_api.models.WatchPosition;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

public class PositionStorage {

    public interface PositionCallback {
        void onLoaded(WatchPosition position); // null, если нет записи
    }

    private final WatchPositionDao dao;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public PositionStorage(WatchPositionDao dao, ExecutorService executor, Handler mainHandler) {
        this.dao = dao;
        this.executor = executor;
        this.mainHandler = mainHandler;
    }

    // ===================== KEY BUILDERS =====================

    public static String serialKey(int kinopoiskId, int season, int episode) {
        return kinopoiskId + ":s" + season + ":e" + episode;
    }

    public static String movieKey(int kinopoiskId) {
        return String.valueOf(kinopoiskId);
    }

    // ===================== ASYNC API =====================

    public void getAsync(String idPosition, PositionCallback callback) {
        executor.execute(() -> {
            WatchPosition wp = dao.getByKey(idPosition);
            post(callback, wp);
        });
    }

    public void getLastWatchedSerialAsync(int kinopoiskId, PositionCallback callback) {
        executor.execute(() -> {
            WatchPosition wp = dao.getLastWatchedSerial(kinopoiskId);
            post(callback, wp);
        });
    }

    public void getLastWatchedMovieAsync(int kinopoiskId, PositionCallback callback) {
        executor.execute(() -> {
            WatchPosition wp = dao.getLastWatchedMovie(kinopoiskId);
            post(callback, wp);
        });
    }

    private void post(PositionCallback callback, WatchPosition wp) {
        if (mainHandler != null) {
            mainHandler.post(() -> callback.onLoaded(wp));
        } else {
            callback.onLoaded(wp);
        }
    }

    public void save(int kinopoiskId, String balancer, String idPosition,
                     int season, int episode, int voice, int quality, long position) {
        WatchPosition wp = new WatchPosition(
                idPosition, kinopoiskId, balancer,
                position, season, episode, voice, quality,
                System.currentTimeMillis()
        );
        try {
            executor.execute(() -> dao.upsert(wp));
        } catch (RejectedExecutionException ignored) {
            // executor уже закрыт — теряем одну итерацию сохранения
        }
    }

    public void clearForFilm(int kinopoiskId) {
        executor.execute(() -> dao.deleteAllForFilm(kinopoiskId));
    }
}