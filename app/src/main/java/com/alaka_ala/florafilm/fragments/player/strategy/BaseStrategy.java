package com.alaka_ala.florafilm.fragments.player.strategy;


import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;

import com.alaka_ala.florafilm.data.media.PlayerLaunchData;
import com.alaka_ala.unofficial_kinopoisk_api.models.FilmDetails;

import java.util.Map;
import java.util.concurrent.ExecutorService;



// Базовая стратегия просмотра контента
@UnstableApi
public abstract class BaseStrategy implements PlayerSourceStrategy {

    protected static final int INDEX_BALANCER = 0;
    protected static final int INDEX_SEASON = 1;
    protected static final int INDEX_EPISODES = 2;
    protected static final int INDEX_VOICE = 3;
    protected static final int INDEX_QUALITY = 4;

    protected PlayerLaunchData launchData;
    protected FilmDetails filmDetails;
    protected Map<String, Long> savedPositionsMap;
    protected ExecutorService executorService;
    protected Handler mainHandler;
    protected ExoPlayer player;
    protected Context context;



    @Override
    public void setupPlayback(Context context, ExoPlayer player,
                              PlayerLaunchData launchData,
                              FilmDetails filmDetails,
                              Map<String, Long> savedPositionsMap,
                              ExecutorService executorService,
                              Handler mainHandler) {
        this.player = player;
        this.launchData = launchData;
        this.filmDetails = filmDetails;
        this.savedPositionsMap = savedPositionsMap;
        this.executorService = executorService;
        this.mainHandler = mainHandler;
        this.context = context;

        if (filmDetails.isSerial()) {
            setupSerialPlayback();
        } else {
            setupMoviePlayback();
        }
    }

    protected abstract void setupSerialPlayback();
    protected abstract void setupMoviePlayback();
    protected abstract String getVideoUrl(String videoData);

    protected MediaItem createMediaItem(String mediaId, Object tag, String uri) {
        return new MediaItem.Builder()
                .setMediaId(mediaId)
                .setTag(tag)
                .setUri(uri)
                .build();
    }

    protected boolean isValidUrl(String url) {
        return url != null && (url.startsWith("https://") || url.startsWith("http://"));
    }

    protected void showToast(String message) {
        if (context == null) return;
        mainHandler.post(() -> Toast.makeText(context,
                message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void cleanup(ExoPlayer player) {
        // Базовая реализация - ничего не делаем
    }
}
