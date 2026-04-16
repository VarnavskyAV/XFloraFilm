package com.alaka_ala.florafilm.ui.fragments.player.strategy;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.analytics.AnalyticsListener;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.data.media.PlayerLaunchData;
import com.alaka_ala.florafilm.ui.fragments.filmDetails.SelectorVoiceAdapter.File;
import com.alaka_ala.florafilm.ui.fragments.filmDetails.SelectorVoiceAdapter.Folder;
import com.alaka_ala.florafilm.ui.fragments.filmDetails.SelectorVoiceAdapter.Item;
import com.alaka_ala.florafilm.utils.balancers.hdvb.HDVB;
import com.alaka_ala.unofficial_kinopoisk_api.models.FilmDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@UnstableApi
public class HDVBStrategy extends BaseStrategy {

    private HDVB hdvb;
    private AnalyticsListener analyticsListener;
    private boolean isSerial;
    private Context context;

    @Override
    public void setupPlayback(Context context, ExoPlayer player, PlayerLaunchData launchData, FilmDetails filmDetails,
                              Map<String, Long> savedPositionsMap, ExecutorService executorService,
                              Handler mainHandler) {
        this.isSerial = filmDetails.isSerial();
        this.hdvb = new HDVB(context.getString(R.string.api_key_hdvb));
        this.context = context;
        super.setupPlayback(context, player, launchData, filmDetails, savedPositionsMap, executorService, mainHandler);
    }

    @Override
    protected void setupSerialPlayback() {
        List<MediaItem> mediaItems = createSerialMediaItems();

        if (mediaItems.isEmpty()) {
            showToast("Не удалось создать плейлист для сериала");
            return;
        }

        player.setMediaItems(mediaItems);
        int startEpisodeIndex = launchData.getSelectedIndexPath().get(INDEX_EPISODES);
        restorePositionForCurrentEpisode(startEpisodeIndex);
        setupSerialAnalyticsListener();
        updateFilmViewStatus(true);
        loadDirectVideoUrlForCurrentItem();

        player.prepare();
        player.play();
    }

    @Override
    protected void setupMoviePlayback() {
        File startingFile = findFileByPath(launchData.getSelectedIndexPath());
        if (startingFile == null) {
            showToast("Не удалось найти файл для воспроизведения");
            return;
        }

        MediaItem mediaItem = createMediaItem(
                String.valueOf(filmDetails.getKinopoiskId()),
                startingFile,
                startingFile.videoUrl
        );

        player.setMediaItems(List.of(mediaItem));
        restorePositionForMovie();
        updateFilmViewStatus(true);

        player.prepare();
        player.play();
    }

    private List<MediaItem> createSerialMediaItems() {
        List<MediaItem> mediaItems = new ArrayList<>();
        List<Integer> selectedIndexPath = launchData.getSelectedIndexPath();

        Folder selectedBalancer = launchData.getRootFolders().get(selectedIndexPath.get(INDEX_BALANCER));
        Item selectedSeason = selectedBalancer.children.get(selectedIndexPath.get(INDEX_SEASON));

        if (!(selectedSeason instanceof Folder)) {
            return mediaItems;
        }

        // Получаем выбранную озвучку по индексу из первой серии (как эталон)
        Folder firstEpisode = (Folder) ((Folder) selectedSeason).children.get(selectedIndexPath.get(INDEX_VOICE));
        //Folder firstEpisode = (Folder) ((Folder) selectedSeason).children.get(0);
        Folder selectedVoiceTemplate = (Folder) firstEpisode.children.get(selectedIndexPath.get(INDEX_VOICE));
        String selectedVoiceTitle = selectedVoiceTemplate.name; // Запоминаем название выбранной озвучки
        for (int episodeIndex = 0; episodeIndex < ((Folder) selectedSeason).children.size(); episodeIndex++) {
            Item episodeItem = ((Folder) selectedSeason).children.get(episodeIndex);
            if (!(episodeItem instanceof Folder)) continue;

            Folder episodeFolder = (Folder) episodeItem;

            // Ищем озвучку с таким же названием в текущей серии
            Folder selectedVoice = null;
            for (Item voiceItem : episodeFolder.children) {
                if (voiceItem instanceof Folder) {
                    Folder voiceFolder = (Folder) voiceItem;
                    if (voiceFolder.name.equals(selectedVoiceTitle)) {
                        selectedVoice = voiceFolder;
                        break;
                    }
                }
            }

            // Если озвучка не найдена в этой серии, пропускаем или берем первую доступную
            if (selectedVoice == null && !episodeFolder.children.isEmpty()) {
                // Вариант 1: берем первую доступную озвучку
                selectedVoice = (Folder) episodeFolder.children.get(0);
                // Вариант 2: пропускаем серию
                // continue;
            }

            if (selectedVoice != null && selectedVoice.children.size() > selectedIndexPath.get(INDEX_QUALITY)) {
                File selectedQuality = (File) selectedVoice.children.get(selectedIndexPath.get(INDEX_QUALITY));

                List<Integer> episodeIndexPath = new ArrayList<>(selectedIndexPath);
                episodeIndexPath.set(INDEX_EPISODES, episodeIndex);

                MediaItem mediaItem = createMediaItem(
                        PlayerLaunchData.getIndexPathKey(episodeIndexPath),
                        episodeIndexPath,
                        selectedQuality.videoUrl
                );
                mediaItems.add(mediaItem);
            }
        }

        return mediaItems;
    }

    private void restorePositionForCurrentEpisode(int episodeIndex) {
        if (player == null || player.getMediaItemCount() <= episodeIndex) {
            return;
        }

        MediaItem mediaItem = player.getMediaItemAt(episodeIndex);
        if (mediaItem == null) {
            return;
        }

        String key = mediaItem.mediaId;
        Long savedPosition = savedPositionsMap.get(key);

        if (savedPosition != null && savedPosition > 0) {
            player.seekTo(episodeIndex, savedPosition);
        }
    }

    private void restorePositionForMovie() {
        if (player == null) return;

        String key = String.valueOf(filmDetails.getKinopoiskId());
        Long savedPosition = savedPositionsMap.get(key);

        if (savedPosition != null && savedPosition > 0) {
            player.seekTo(savedPosition);
        }
    }

    private void setupSerialAnalyticsListener() {
        analyticsListener = new AnalyticsListener() {
            @Override
            public void onMediaItemTransition(EventTime eventTime, @Nullable MediaItem mediaItem, int reason) {
                AnalyticsListener.super.onMediaItemTransition(eventTime, mediaItem, reason);

                if (player != null) {
                    saveCurrentPositionForEpisode();
                    int currentIndex = player.getCurrentMediaItemIndex();
                    restorePositionForCurrentEpisode(currentIndex);
                    loadDirectVideoUrlForCurrentItem();
                }
            }
        };
        player.addAnalyticsListener(analyticsListener);
    }

    private void saveCurrentPositionForEpisode() {
        if (player == null || player.getCurrentMediaItem() == null) return;

        String key = player.getCurrentMediaItem().mediaId;
        long position = player.getCurrentPosition();

        savedPositionsMap.put(key, position);

        executorService.execute(() -> {
            // Сохранение в базу данных
            savePositionToDatabase(key, position);
        });
    }

    private void loadDirectVideoUrlForCurrentItem() {
        if (player == null || player.getCurrentMediaItem() == null) return;
        MediaItem currentItem = player.getCurrentMediaItem();
        if (currentItem.localConfiguration == null) return;
        String videoData = currentItem.localConfiguration.uri.toString();
        String mediaId = currentItem.mediaId;
        Object tag = currentItem.localConfiguration.tag;
        executorService.execute(() -> {
            String urlVideo = getVideoUrl(videoData);

            if (isValidUrl(urlVideo)) {
                MediaItem newMediaItem = createMediaItem(mediaId, tag, urlVideo);

                mainHandler.post(() -> {
                    if (player == null) return;

                    int currentIndex = player.getCurrentMediaItemIndex();
                    if (currentIndex >= 0) {
                        long currentPosition = player.getCurrentPosition();
                        player.replaceMediaItem(currentIndex, newMediaItem);
                        player.seekTo(currentIndex, currentPosition);

                        if (!player.isPlaying()) {
                            player.prepare();
                            player.play();
                        }
                    }
                });
            }
        });


    }

    @Override
    protected String getVideoUrl(String videoData) {
        return HDVB.getFileSerial(videoData);
    }

    private File findFileByPath(List<Integer> path) {
        if (path == null || path.isEmpty() || launchData == null) {
            return null;
        }

        try {
            Item currentItem = launchData.getRootFolders().get(path.get(0));
            for (int i = 1; i < path.size(); i++) {
                if (!(currentItem instanceof Folder)) {
                    return null;
                }
                currentItem = ((Folder) currentItem).children.get(path.get(i));
            }
            return currentItem instanceof File ? (File) currentItem : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void updateFilmViewStatus(boolean isStartView) {
        if (filmDetails == null) return;

        executorService.execute(() -> {
            // Обновление статуса просмотра в базе данных
            // Здесь нужно реализовать сохранение в БД
        });
    }

    private void savePositionToDatabase(String key, long position) {
        // Реализация сохранения позиции в БД
        // Используй - filmDetailsDao
    }

    @Override
    public String getPositionKey(ExoPlayer player, int kinopoiskId) {
        if (isSerial && player.getCurrentMediaItem() != null) {
            return player.getCurrentMediaItem().mediaId;
        } else {
            return String.valueOf(kinopoiskId);
        }
    }

    @Override
    public void cleanup(ExoPlayer player) {
        if (analyticsListener != null && player != null) {
            player.removeAnalyticsListener(analyticsListener);
        }
    }
}
