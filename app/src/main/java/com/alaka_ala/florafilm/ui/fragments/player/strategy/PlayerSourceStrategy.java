package com.alaka_ala.florafilm.ui.fragments.player.strategy;

import android.content.Context;
import android.os.Handler;

import androidx.media3.exoplayer.ExoPlayer;

import com.alaka_ala.florafilm.data.media.PlayerLaunchData;
import com.alaka_ala.unofficial_kinopoisk_api.models.FilmDetails;

import java.util.Map;
import java.util.concurrent.ExecutorService;

public interface PlayerSourceStrategy {
    void setupPlayback(Context context, ExoPlayer player, PlayerLaunchData launchData, FilmDetails filmDetails,
                       Map<String, Long> savedPositionsMap, ExecutorService executorService,
                       Handler mainHandler);

    String getPositionKey(ExoPlayer player, int kinopoiskId);

    void cleanup(ExoPlayer player);
}
