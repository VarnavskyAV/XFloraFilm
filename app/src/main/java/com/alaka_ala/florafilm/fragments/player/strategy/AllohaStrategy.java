package com.alaka_ala.florafilm.fragments.player.strategy;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.analytics.AnalyticsListener;

import com.alaka_ala.florafilm.data.media.PlayerLaunchData;
import com.alaka_ala.unofficial_kinopoisk_api.api.PositionStorage;
import com.alaka_ala.florafilm.fragments.filmDetails.SelectorVoiceAdapter.File;
import com.alaka_ala.florafilm.fragments.filmDetails.SelectorVoiceAdapter.Folder;
import com.alaka_ala.florafilm.fragments.filmDetails.SelectorVoiceAdapter.Item;
import com.alaka_ala.florafilm.utils.balancers.alloha.AllohaBnsiParserJava;
import com.alaka_ala.florafilm.utils.balancers.alloha.AllohaParserJava;
import com.alaka_ala.florafilm.utils.balancers.alloha.AllohaStreaming;
import com.alaka_ala.florafilm.utils.balancers.alloha.HlsProxyServerJava;
import com.alaka_ala.unofficial_kinopoisk_api.models.FilmDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@UnstableApi
public class AllohaStrategy extends BaseStrategy {

    private static final String TAG = "AllohaStrategy";
    private static final String PROXY_URL = "http://127.0.0.1:8080/master.m3u8";
    private static final String BALANCER_NAME = "ALLOHA";

    private final java.util.concurrent.ConcurrentHashMap<String, String> activeHeaders =
            new java.util.concurrent.ConcurrentHashMap<>();

    private final java.util.concurrent.atomic.AtomicReference<
            Map<String, AllohaBnsiParserJava.QualityUrls>> qualityUrlsRef =
            new java.util.concurrent.atomic.AtomicReference<>(new java.util.LinkedHashMap<>());

    private AllohaParserJava parser;
    private AllohaStreaming streaming;

    private boolean isSerial;
    private Context context;
    private PositionStorage positionStorage;
    private HlsProxyServerJava proxyServer;
    private String currentIframeUrl;
    private boolean isPlaying = false;

    private int targetPlaylistIndex = 0;

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
    public void setupPlayback(Context context,
                              ExoPlayer player,
                              PlayerLaunchData launchData,
                              FilmDetails filmDetails,
                              PositionStorage positionStorage,
                              ExecutorService executorService,
                              Handler mainHandler) {
        this.isSerial = filmDetails.isSerial();
        this.context = context;
        this.positionStorage = positionStorage;
        this.streaming = new AllohaStreaming(context, mainHandler, new AllohaStreaming.Callback() {
            @Override
            public void onQualities(Map<String, AllohaBnsiParserJava.QualityUrls> qualities) {
                qualityUrlsRef.set(qualities);
            }

            @Override
            public void onProxyReady(HlsProxyServerJava proxy) {
                proxyServer = proxy;
                isPlaying = true;
                Log.d(TAG, "✅ Proxy ready, player continues via fixed URL");
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Streaming error: " + error);
                mainHandler.post(() -> showToast("Alloha: " + error));
            }
        });
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

        player.setMediaItems(mediaItems, targetPlaylistIndex, 0);
        restorePositionForCurrentEpisode(targetPlaylistIndex);
        updateFilmViewStatus();

        player.addAnalyticsListener(new AnalyticsListener() {
            @Override
            public void onPlayerError(EventTime eventTime, PlaybackException error) {
                AnalyticsListener.super.onPlayerError(eventTime, error);
                Log.e(TAG, "Player error: " + error.errorCode + " | " + error.getMessage());

                boolean isNetworkOrParseError = error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ||
                        error.errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED ||
                        (error.getMessage() != null && error.getMessage().contains("403"));

                if (isNetworkOrParseError) {
                    if (streaming != null) {
                        streaming.tryFallbackOnce();
                        streaming.forceRestart("403_fallback_triggered");
                    } else {
                        tryFallbackFromBnsi();
                    }
                }
            }

            @Override
            public void onMediaItemTransition(EventTime eventTime, @Nullable MediaItem mediaItem, int reason) {
                if (player == null) return;
                int currentIndex = player.getCurrentMediaItemIndex();
                restorePositionForCurrentEpisode(currentIndex);
                loadCurrentEpisode();
            }
        });

        loadCurrentEpisode();

        player.prepare();
        player.play();

        mainHandler.postDelayed(saveTick, 5000);
    }

    private List<MediaItem> createSerialMediaItems() {
        List<MediaItem> mediaItems = new ArrayList<>();
        List<Integer> selectedIndexPath = launchData.getSelectedIndexPath();

        Folder selectedBalancer = launchData.getRootFolders().size() == 1
                ? launchData.getRootFolders().get(0)
                : launchData.getRootFolders().get(selectedIndexPath.get(INDEX_BALANCER));
        if (selectedBalancer == null) return mediaItems;

        Item selectedSeason = selectedBalancer.children.get(selectedIndexPath.get(INDEX_SEASON));
        if (!(selectedSeason instanceof Folder)) return mediaItems;

        int selectedEpisodeIndex = selectedIndexPath.get(INDEX_EPISODES);
        int selectedVoiceIndex = selectedIndexPath.get(INDEX_VOICE);
        int selectedQualityIndex = selectedIndexPath.get(INDEX_QUALITY);

        Folder selectedEpisodeFolder = (Folder) ((Folder) selectedSeason).children.get(selectedEpisodeIndex);
        if (selectedEpisodeFolder.children.size() <= selectedVoiceIndex) return mediaItems;
        Folder selectedVoiceTemplate = (Folder) selectedEpisodeFolder.children.get(selectedVoiceIndex);
        String selectedVoiceTitle = selectedVoiceTemplate.name;

        Folder seasonFolder = (Folder) selectedSeason;

        for (int episodeIndex = 0; episodeIndex < seasonFolder.children.size(); episodeIndex++) {
            Item episodeItem = seasonFolder.children.get(episodeIndex);
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
            if (selectedQuality == null) continue;

            String idPosition = PositionStorage.serialKey(
                    filmDetails.getKinopoiskId(),
                    selectedIndexPath.get(INDEX_SEASON),
                    episodeIndex
            );

            List<Integer> episodeIndexPath = new ArrayList<>(selectedIndexPath);
            episodeIndexPath.set(INDEX_EPISODES, episodeIndex);

            MediaItem mediaItem = new MediaItem.Builder()
                    .setMediaId(idPosition)
                    .setUri(PROXY_URL)
                    .setTag(selectedQuality)
                    .build();

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
            if (wp.getPosition() > 0L) {
                player.seekTo(index, wp.getPosition());
            }
        });
    }

    // ===================== MOVIE =====================

    @Override
    protected void setupMoviePlayback() {
        File selectedFile = findFileByPath(launchData.getSelectedIndexPath());
        if (selectedFile == null) {
            showToast("Не удалось найти файл для воспроизведения");
            return;
        }

        String mediaId = PositionStorage.movieKey(filmDetails.getKinopoiskId());

        MediaItem mediaItem = new MediaItem.Builder()
                .setMediaId(mediaId)
                .setUri(PROXY_URL)
                .setTag(selectedFile)
                .build();

        player.setMediaItems(List.of(mediaItem));

        player.addAnalyticsListener(new AnalyticsListener() {
            @Override
            public void onPlayerError(EventTime eventTime, PlaybackException error) {
                AnalyticsListener.super.onPlayerError(eventTime, error);
                Log.e(TAG, "Player error: " + error.errorCode + " | " + error.getMessage());

                boolean isNetworkOrParseError = error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ||
                        error.errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED ||
                        (error.getMessage() != null && error.getMessage().contains("403"));

                if (isNetworkOrParseError) {
                    if (streaming != null) {
                        streaming.tryFallbackOnce();
                        streaming.forceRestart("403_fallback_triggered");
                    } else {
                        tryFallbackFromBnsi();
                    }
                }
            }

            @Override
            public void onMediaItemTransition(EventTime eventTime, @Nullable MediaItem mediaItem, int reason) {
                if (player == null) return;
                restorePositionForMovie();
                loadCurrentEpisode();
            }
        });

        restorePositionForMovie();
        updateFilmViewStatus();

        loadVideo(selectedFile.videoUrl);

        player.prepare();
        player.play();

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
        long position = player.getCurrentPosition();
        if (position <= 0) return;

        int season = -1;
        int episode = -1;
        int voice = -1;
        int quality = -1;

        if (item.localConfiguration != null && item.localConfiguration.tag instanceof File) {
            File f = (File) item.localConfiguration.tag;
            List<Integer> path = f.getIndexPath();

            if (filmDetails.isSerial()) {
                // [balancer, season, episode, voice, quality]
                if (path != null && path.size() >= 5) {
                    season = path.get(INDEX_SEASON);
                    episode = path.get(INDEX_EPISODES);
                    voice = path.get(INDEX_VOICE);
                    quality = path.get(INDEX_QUALITY);
                }
            } else {
                // [balancer, voice, quality]
                if (path != null && path.size() >= 3) {
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

    // ===================== VIDEO LOADING =====================

    private void loadCurrentEpisode() {
        if (player == null || player.getCurrentMediaItem() == null) return;

        MediaItem currentItem = player.getCurrentMediaItem();
        if (currentItem.localConfiguration == null) return;

        Object tag = currentItem.localConfiguration.tag;
        if (tag instanceof File) {
            File file = (File) tag;
            String iframeUrl = file.videoUrl;
            if (iframeUrl != null && !iframeUrl.isEmpty()) {
                loadVideo(iframeUrl);
            }
        }
    }

    private void loadVideo(String iframeUrl) {
        if (iframeUrl.equals(currentIframeUrl) && isPlaying) {
            Log.d(TAG, "Same video, skip loading");
            return;
        }

        currentIframeUrl = iframeUrl;
        isPlaying = false;
        stopProxy();

        try {
            int qIndex = launchData.getSelectedIndexPath().get(INDEX_QUALITY);
            String[] ordered = new String[]{"2160", "1440", "1080", "720", "480", "360"};
            if (qIndex >= 0 && qIndex < ordered.length && streaming != null) {
                streaming.setSelectedQualityKey(ordered[qIndex]);
            }
        } catch (Exception ignored) {}

        if (streaming != null) {
            mainHandler.post(() -> streaming.start(iframeUrl));
        } else {
            parser = new AllohaParserJava(context);
            executorService.execute(() -> parser.parse(iframeUrl, new AllohaParserJava.Callback() {
                @Override public void onHlsLinksReceived(String json, Map<String, String> extraHeaders) {}
                @Override public void onConfigUpdate(String edgeHash, int ttlSeconds, Map<String, String> extraHeaders) {}
                @Override public void onM3u8Refreshed(String url, Map<String, String> extraHeaders) {}
                @Override public void onError(String error) {}
            }));
        }
    }

    private void tryFallbackFromBnsi() {
        Map<String, AllohaBnsiParserJava.QualityUrls> map = qualityUrlsRef.get();
        if (map == null || map.isEmpty() || proxyServer == null) return;

        AllohaBnsiParserJava.QualityUrls q = map.values().iterator().next();
        String fallbackUrl = AllohaBnsiParserJava.pickWithFallback(q, true);

        if (fallbackUrl != null && !fallbackUrl.isEmpty()) {
            proxyServer.updateMasterUrl(fallbackUrl);
            Log.d(TAG, "Switched to fallback URL from bnsi");
        }
    }

    private void stopProxy() {
        if (proxyServer != null && proxyServer.isRunning()) {
            proxyServer.stop();
            Log.d(TAG, "Proxy stopped");
        }
        proxyServer = null;

        if (streaming != null) streaming.stop();
        if (parser != null) { parser.release(); parser = null; }
    }

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

    @Override
    protected String getVideoUrl(String videoData) {
        return PROXY_URL;
    }

    @Override
    public String getPositionKey(ExoPlayer player, int kinopoiskId) {
        if (isSerial && player != null && player.getCurrentMediaItem() != null) {
            return player.getCurrentMediaItem().mediaId;
        }
        return PositionStorage.movieKey(kinopoiskId);
    }

    @Override
    public void cleanup(ExoPlayer player) {
        if (mainHandler != null) {
            mainHandler.removeCallbacks(saveTick);
        }
        saveCurrentPosition();
        stopProxy();
        super.cleanup(player);
    }

    private void updateFilmViewStatus() {
        // Обновление статуса просмотра в БД (если понадобится).
    }
}