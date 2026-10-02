package com.alaka_ala.florafilm.fragments.player.strategy;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.analytics.AnalyticsListener;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.data.media.PlayerLaunchData;
import com.alaka_ala.unofficial_kinopoisk_api.api.PositionStorage;
import com.alaka_ala.florafilm.fragments.filmDetails.SelectorVoiceAdapter.File;
import com.alaka_ala.florafilm.fragments.filmDetails.SelectorVoiceAdapter.Folder;
import com.alaka_ala.florafilm.fragments.filmDetails.SelectorVoiceAdapter.Item;
import com.alaka_ala.florafilm.utils.balancers.hdvb.HDVB;
import com.alaka_ala.unofficial_kinopoisk_api.models.FilmDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

@UnstableApi
public class HDVBStrategy extends BaseStrategy {

    private static final String BALANCER_NAME = "HDVB";

    private HDVB hdvb;
    private AnalyticsListener analyticsListener;
    private boolean isSerial;
    private Context context;
    private PositionStorage positionStorage;

    /** Индекс целевого эпизода в плейлисте (может не совпадать с индексом в пути). */
    private int targetPlaylistIndex = 0;

    /** Периодическое сохранение позиции. */
    private final Runnable saveTick = new Runnable() {
        @Override
        public void run() {
            saveCurrentPosition();
            if (mainHandler != null) {
                mainHandler.postDelayed(this, 5000);
            }
        }
    };

    @Override
    public void setupPlayback(Context context, ExoPlayer player, PlayerLaunchData launchData, FilmDetails filmDetails,
                              PositionStorage positionStorage, ExecutorService executorService,
                              Handler mainHandler) {
        this.isSerial = filmDetails.isSerial();
        this.hdvb = new HDVB(context.getString(R.string.api_key_hdvb));
        this.context = context;
        this.positionStorage = positionStorage;
        super.setupPlayback(context, player, launchData, filmDetails, positionStorage, executorService, mainHandler);
    }

    // ===================== SERIAL =====================

    @Override
    protected void setupSerialPlayback() {
        List<MediaItem> mediaItems = createSerialMediaItems();

        if (mediaItems.isEmpty()) {
            showToast("Не удалось создать плейлист для сериала");
            return;
        }

        player.setMediaItems(mediaItems);

        // Сначала перейти на нужный эпизод, потом восстанавливать позицию внутри него.
        player.seekTo(targetPlaylistIndex, 0);

        restorePositionForCurrentEpisode(targetPlaylistIndex);
        setupSerialAnalyticsListener();
        updateFilmViewStatus();

        player.prepare();
        player.play();

        loadDirectVideoUrlForCurrentItem();
        mainHandler.postDelayed(saveTick, 5000);
    }

    private List<MediaItem> createSerialMediaItems() {
        List<MediaItem> mediaItems = new ArrayList<>();
        List<Integer> selectedIndexPath = launchData.getSelectedIndexPath();

        Folder selectedBalancer = launchData.getRootFolders().get(selectedIndexPath.get(INDEX_BALANCER));
        Item selectedSeason = selectedBalancer.children.get(selectedIndexPath.get(INDEX_SEASON));

        if (!(selectedSeason instanceof Folder)) {
            return mediaItems;
        }

        int selectedEpisodeIndex = selectedIndexPath.get(INDEX_EPISODES);
        int selectedVoiceIndex = selectedIndexPath.get(INDEX_VOICE);
        int selectedQualityIndex = selectedIndexPath.get(INDEX_QUALITY);

        Folder selectedEpisodeFolder = (Folder) ((Folder) selectedSeason).children.get(selectedEpisodeIndex);
        if (selectedEpisodeFolder.children.size() <= selectedVoiceIndex) {
            return mediaItems;
        }
        Folder selectedVoiceTemplate = (Folder) selectedEpisodeFolder.children.get(selectedVoiceIndex);
        String selectedVoiceTitle = selectedVoiceTemplate.name;

        for (int episodeIndex = 0; episodeIndex < ((Folder) selectedSeason).children.size(); episodeIndex++) {
            Item episodeItem = ((Folder) selectedSeason).children.get(episodeIndex);
            if (!(episodeItem instanceof Folder)) continue;

            Folder episodeFolder = (Folder) episodeItem;

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

            if (selectedVoice == null) continue;
            if (selectedVoice.children.size() <= selectedQualityIndex) continue;

            File selectedQuality = (File) selectedVoice.children.get(selectedQualityIndex);

            String idPosition = PositionStorage.serialKey(
                    filmDetails.getKinopoiskId(),
                    selectedIndexPath.get(INDEX_SEASON),
                    episodeIndex
            );

            List<Integer> episodeIndexPath = new ArrayList<>(selectedIndexPath);
            episodeIndexPath.set(INDEX_EPISODES, episodeIndex);

            MediaItem mediaItem = createMediaItem(idPosition, episodeIndexPath, selectedQuality.videoUrl);
            mediaItems.add(mediaItem);

            if (episodeIndex == selectedEpisodeIndex) {
                targetPlaylistIndex = mediaItems.size() - 1;
            }
        }

        return mediaItems;
    }

    private void restorePositionForCurrentEpisode(int playlistIndex) {
        if (player == null || player.getMediaItemCount() <= playlistIndex) return;

        MediaItem mediaItem = player.getMediaItemAt(playlistIndex);
        if (mediaItem == null) return;

        final int index = playlistIndex;
        positionStorage.getAsync(mediaItem.mediaId, wp -> {
            if (player == null || wp == null) return;
            if (player.getCurrentMediaItemIndex() != index) return;
            if (wp.getPosition() > 0) {
                player.seekTo(index, wp.getPosition());
            }
        });
    }

    private void setupSerialAnalyticsListener() {
        analyticsListener = new AnalyticsListener() {
            @Override
            public void onMediaItemTransition(EventTime eventTime, @Nullable MediaItem mediaItem, int reason) {
                AnalyticsListener.super.onMediaItemTransition(eventTime, mediaItem, reason);
                if (player == null) return;

                int currentIndex = player.getCurrentMediaItemIndex();
                restorePositionForCurrentEpisode(currentIndex);
                loadDirectVideoUrlForCurrentItem();
            }
        };
        player.addAnalyticsListener(analyticsListener);
    }

    // ===================== MOVIE =====================

    @Override
    protected void setupMoviePlayback() {
        File startingFile = findFileByPath(launchData.getSelectedIndexPath());
        if (startingFile == null) {
            showToast("Не удалось найти файл для воспроизведения");
            return;
        }

        String idPosition = PositionStorage.movieKey(filmDetails.getKinopoiskId());

        MediaItem mediaItem = createMediaItem(
                idPosition,
                launchData.getSelectedIndexPath(),
                startingFile.videoUrl
        );

        player.setMediaItems(List.of(mediaItem));
        restorePositionForMovie();
        updateFilmViewStatus();

        player.prepare();
        player.play();

        loadDirectVideoUrlForCurrentItem();
        mainHandler.postDelayed(saveTick, 5000);
    }

    private void restorePositionForMovie() {
        if (player == null) return;

        positionStorage.getAsync(PositionStorage.movieKey(filmDetails.getKinopoiskId()), wp -> {
            if (player == null || wp == null) return;
            if (wp.getPosition() > 0) {
                player.seekTo(wp.getPosition());
            }
        });
    }

    // ===================== SAVE =====================

    private void saveCurrentPosition() {
        if (player == null || player.getCurrentMediaItem() == null) return;

        MediaItem item = player.getCurrentMediaItem();
        if (item.localConfiguration == null) return;

        long position = player.getCurrentPosition();
        if (position <= 0) return;

        int season = -1;
        int episode = -1;
        int voice = -1;
        int quality = -1;

        Object tag = item.localConfiguration.tag;
        if (tag instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<Integer> path = (List<Integer>) tag;

            if (filmDetails.isSerial()) {
                // [balancer, season, episode, voice, quality]
                if (path.size() >= 5) {
                    season = path.get(INDEX_SEASON);
                    episode = path.get(INDEX_EPISODES);
                    voice = path.get(INDEX_VOICE);
                    quality = path.get(INDEX_QUALITY);
                }
            } else {
                // [balancer, voice, quality]
                if (path.size() >= 3) {
                    voice = path.get(1);
                    quality = path.get(2);
                }
            }
        }

        positionStorage.save(
                filmDetails.getKinopoiskId(),
                BALANCER_NAME,
                item.mediaId,
                season,
                episode,
                voice,
                quality,
                position
        );
    }

    // ===================== VIDEO URL =====================

    private void loadDirectVideoUrlForCurrentItem() {
        if (player == null || player.getCurrentMediaItem() == null) return;

        MediaItem currentItem = player.getCurrentMediaItem();
        if (currentItem.localConfiguration == null) return;

        String videoData = currentItem.localConfiguration.uri.toString();
        String mediaId = currentItem.mediaId;
        Object tag = currentItem.localConfiguration.tag;

        executorService.execute(() -> {
            String urlVideo = getVideoUrl(videoData);
            if (!isValidUrl(urlVideo)) return;

            MediaItem newMediaItem = createMediaItem(mediaId, tag, urlVideo);

            mainHandler.post(() -> {
                if (player == null) return;

                int currentIndex = findMediaItemIndexById(mediaId);
                if (currentIndex < 0) return;

                long currentPosition = player.getCurrentPosition();
                player.replaceMediaItem(currentIndex, newMediaItem);
                player.seekTo(currentIndex, currentPosition);

                if (!player.isPlaying()) {
                    player.prepare();
                    player.play();
                }
            });
        });
    }

    private int findMediaItemIndexById(String mediaId) {
        if (player == null) return -1;
        for (int i = 0; i < player.getMediaItemCount(); i++) {
            MediaItem item = player.getMediaItemAt(i);
            if (item != null && mediaId.equals(item.mediaId)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected String getVideoUrl(String videoData) {
        return HDVB.getFileSerial(videoData);
    }

    // ===================== HELPERS =====================

    private File findFileByPath(List<Integer> path) {
        if (path == null || path.isEmpty() || launchData == null) return null;

        try {
            Item currentItem = launchData.getRootFolders().get(path.get(0));
            for (int i = 1; i < path.size(); i++) {
                if (!(currentItem instanceof Folder)) return null;
                currentItem = ((Folder) currentItem).children.get(path.get(i));
            }
            return currentItem instanceof File ? (File) currentItem : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void updateFilmViewStatus() {
        if (filmDetails == null) return;
        executorService.execute(() -> {
            // Обновление статуса просмотра в БД (если понадобится).
        });
    }

    @Override
    public String getPositionKey(ExoPlayer player, int kinopoiskId) {
        if (player == null || player.getCurrentMediaItem() == null) {
            return PositionStorage.movieKey(kinopoiskId);
        }
        return player.getCurrentMediaItem().mediaId;
    }

    @Override
    public void cleanup(ExoPlayer player) {
        if (mainHandler != null) {
            mainHandler.removeCallbacks(saveTick);
        }
        saveCurrentPosition();

        if (analyticsListener != null && player != null) {
            player.removeAnalyticsListener(analyticsListener);
        }
    }
}