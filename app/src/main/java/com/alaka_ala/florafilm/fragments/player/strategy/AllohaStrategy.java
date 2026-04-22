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

    private final java.util.concurrent.ConcurrentHashMap<String, String> activeHeaders =
            new java.util.concurrent.ConcurrentHashMap<>();

    private final java.util.concurrent.atomic.AtomicReference<
            Map<String, AllohaBnsiParserJava.QualityUrls>> qualityUrlsRef =
            new java.util.concurrent.atomic.AtomicReference<>(new java.util.LinkedHashMap<>());

    private AllohaParserJava parser;
    private AllohaStreaming streaming;


    private boolean isSerial;
    private Context context;
    private HlsProxyServerJava proxyServer;
    private String currentIframeUrl;
    private boolean isPlaying = false;

    @Override
    public void setupPlayback(Context context,
                              ExoPlayer player,
                              PlayerLaunchData launchData,
                              FilmDetails filmDetails,
                              Map<String, Long> savedPositionsMap,
                              ExecutorService executorService,
                              Handler mainHandler) {
        this.isSerial = filmDetails.isSerial();
        this.context = context;
        this.streaming = new AllohaStreaming(context, mainHandler, new AllohaStreaming.Callback() {
            @Override
            public void onQualities(Map<String, AllohaBnsiParserJava.QualityUrls> qualities) {
                // keep for fallback; current implementation uses atomic ref
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
        super.setupPlayback(context, player, launchData, filmDetails, savedPositionsMap, executorService, mainHandler);
    }

    @Override
    protected void setupSerialPlayback() {
        List<MediaItem> mediaItems = createSerialMediaItems();

        if (mediaItems.isEmpty()) {
            showToast("Не удалось создать плейлист для сериала");
            return;
        }

        player.setMediaItems(mediaItems, launchData.getSelectedIndexPath().get(INDEX_EPISODES), 0);
        int startEpisodeIndex = launchData.getSelectedIndexPath().get(INDEX_EPISODES);
        restorePositionForCurrentEpisode(startEpisodeIndex);
        updateFilmViewStatus(true);

        // Слушатель для смены серий
        player.addAnalyticsListener(new AnalyticsListener() {
            @Override
            public void onPlayerError(EventTime eventTime, PlaybackException error) {
                AnalyticsListener.super.onPlayerError(eventTime, error);
                // First try bnsi fallback URL (second link after "or")
                if (streaming != null) streaming.tryFallbackOnce();
                else tryFallbackFromBnsi();
            }

            @Override
            public void onMediaItemTransition(EventTime eventTime, @Nullable MediaItem mediaItem, int reason) {
                if (player != null) {
                    saveCurrentPositionForEpisode();
                    // При смене серии загружаем новое видео
                    loadCurrentEpisode();
                }
            }
        });

        // Загружаем первую серию
        loadCurrentEpisode();

        player.prepare();
        player.play();
    }

    @Override
    protected void setupMoviePlayback() {
        File selectedFile = findFileByPath(launchData.getSelectedIndexPath());
        if (selectedFile == null) {
            showToast("Не удалось найти файл для воспроизведения");
            return;
        }

        String mediaId = String.valueOf(filmDetails.getKinopoiskId());

        MediaItem mediaItem = new MediaItem.Builder()
                .setMediaId(mediaId)
                .setUri(PROXY_URL)
                .setTag(selectedFile)
                .build();

        player.setMediaItems(List.of(mediaItem));
        restorePositionForMovie();
        updateFilmViewStatus(true);

        loadVideo(selectedFile.videoUrl);

        player.prepare();
        player.play();
    }

    private List<MediaItem> createSerialMediaItems() {
        List<MediaItem> mediaItems = new ArrayList<>();
        List<Integer> selectedIndexPath = launchData.getSelectedIndexPath();

        // Получаем выбранный балансер
        Folder selectedBalancer = launchData.getRootFolders().size() == 1 ? launchData.getRootFolders().get(0) : launchData.getRootFolders().get(selectedIndexPath.get(INDEX_BALANCER));
        if (selectedBalancer == null) {
            return mediaItems;
        }

        // Получаем выбранную серию
        Item selectedSeason = selectedBalancer.children.get(selectedIndexPath.get(INDEX_SEASON));
        if (!(selectedSeason instanceof Folder)) {
            return mediaItems;
        }

        // Получаем выбранную озвучку
        Folder firstEpisode = (Folder) ((Folder) selectedSeason).children.get(selectedIndexPath.get(INDEX_EPISODES));
        Folder selectedVoiceTemplate = (Folder) firstEpisode.children.get(selectedIndexPath.get(INDEX_VOICE));
        String selectedVoiceTitle = selectedVoiceTemplate.name;

        Folder seasonFolder = (Folder) selectedSeason;

        for (int episodeIndex = 0; episodeIndex < seasonFolder.children.size(); episodeIndex++) {
            Item episodeItem = seasonFolder.children.get(episodeIndex);
            if (!(episodeItem instanceof Folder)) continue;

            Folder episodeFolder = (Folder) episodeItem;

            // Ищем озвучку
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

            if (selectedVoice == null && !episodeFolder.children.isEmpty()) {
                selectedVoice = (Folder) episodeFolder.children.get(0);
            }

            if (selectedVoice != null && selectedVoice.children.size() > selectedIndexPath.get(INDEX_QUALITY)) {
                File selectedQuality = (File) selectedVoice.children.get(selectedIndexPath.get(INDEX_QUALITY));

                if (selectedQuality != null) {
                    List<Integer> episodeIndexPath = new ArrayList<>(selectedIndexPath);
                    episodeIndexPath.set(INDEX_EPISODES, episodeIndex);

                    String mediaId = PlayerLaunchData.getIndexPathKey(episodeIndexPath);

                    MediaItem mediaItem = new MediaItem.Builder()
                            .setMediaId(mediaId)
                            .setUri(PROXY_URL)
                            .setTag(selectedQuality)
                            .build();
                    mediaItems.add(mediaItem);
                }
            }
        }

        return mediaItems;
    }

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

        // Remember current chosen quality key for fallback (use existing selection index)
        try {
            int qIndex = launchData.getSelectedIndexPath().get(INDEX_QUALITY);
            // In your adapter, quality label is stored in File.title. We'll map it later when needed.
            // Here we just try common set: 2160/1440/1080/720/480/360 if present.
            String[] ordered = new String[]{"2160", "1440", "1080", "720", "480", "360"};
            if (qIndex >= 0 && qIndex < ordered.length && streaming != null) streaming.setSelectedQualityKey(ordered[qIndex]);
        } catch (Exception ignored) {}

        if (streaming != null) {
            // streaming.start() internally switches to main thread for WebView,
            // but calling from main keeps ordering simpler.
            mainHandler.post(() -> streaming.start(iframeUrl));
        } else {
            // fallback to old implementation if streaming not initialized
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

        // Можно выбрать нужное качество по имени, здесь берем первое
        AllohaBnsiParserJava.QualityUrls q =
                map.values().iterator().next();

        String fallbackUrl =
                AllohaBnsiParserJava.pickWithFallback(q, true);

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

    private void saveCurrentPositionForEpisode() {
        if (player == null || player.getCurrentMediaItem() == null) return;

        String key = player.getCurrentMediaItem().mediaId;
        long position = player.getCurrentPosition();
        savedPositionsMap.put(key, position);

        executorService.execute(() -> savePositionToDatabase(key, position));
    }

    private void restorePositionForCurrentEpisode(int episodeIndex) {
        if (player == null || player.getMediaItemCount() <= episodeIndex) return;

        MediaItem mediaItem = player.getMediaItemAt(episodeIndex);

        if (mediaItem == null) return;

        String key = mediaItem.mediaId;
        Long savedPosition = savedPositionsMap.get(key);

        if (savedPosition != null && savedPosition > 0L) {
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
        if (isSerial && player.getCurrentMediaItem() != null) {
            return player.getCurrentMediaItem().mediaId;
        } else {
            return String.valueOf(kinopoiskId);
        }
    }

    @Override
    public void cleanup(ExoPlayer player) {
        stopProxy();
        super.cleanup(player);
    }

    private void updateFilmViewStatus(boolean isStartView) {
    }

    private void savePositionToDatabase(String key, long position) {
    }
}