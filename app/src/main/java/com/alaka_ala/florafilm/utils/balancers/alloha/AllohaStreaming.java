package com.alaka_ala.florafilm.utils.balancers.alloha;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Full integration wrapper (Kotlin logic port) for:
 * WebView parser (bnsi + WS config_update + heartbeat + m3u8 refresh) -> headers -> local HLS proxy.
 *
 * Designed to be used from your Player strategy (ExoPlayer already points to fixed proxy URL).
 */
public final class AllohaStreaming {
    private static final String TAG = "AllohaStreaming";
    private static final long RESTART_DEBOUNCE_MS = 8000L;

    public interface Callback {
        /** Called when bnsi json parsed (quality -> primary/fallback). */
        void onQualities(@NonNull Map<String, AllohaBnsiParserJava.QualityUrls> qualities);

        /** Called when proxy is ready (after first m3u8 refresh) and master URL updated. */
        void onProxyReady(@NonNull HlsProxyServerJava proxy);

        /** Debug/status (optional). */
        default void onStatus(@NonNull String status) {}

        void onError(@NonNull String error);
    }

    private final Context appContext;
    private final Handler mainHandler;
    private final Callback callback;

    /** Shared headers map: parser updates, proxy reads. */
    private final ConcurrentHashMap<String, String> activeHeaders = new ConcurrentHashMap<>();
    private final AtomicReference<Map<String, AllohaBnsiParserJava.QualityUrls>> qualitiesRef =
            new AtomicReference<>(new LinkedHashMap<>());

    private AllohaParserJava parser;
    private HlsProxyServerJava proxy;

    private volatile String currentIframeUrl = "";
    private volatile String currentM3u8Url = "";
    private volatile String selectedQualityKey = "";
    private volatile boolean fallbackUsed = false;
    private volatile long lastRestartAtMs = 0L;
    private volatile boolean restartInFlight = false;

    public AllohaStreaming(@NonNull Context context, @NonNull Handler mainHandler, @NonNull Callback callback) {
        this.appContext = context.getApplicationContext();
        this.mainHandler = mainHandler;
        this.callback = callback;
    }

    public void start(@NonNull String iframeUrl) {
        // IMPORTANT: WebView must be created/used on main thread.
        mainHandler.post(() -> {
            currentIframeUrl = iframeUrl;
            callback.onStatus("Alloha: capturing session...");

            // Do NOT stop proxy here — stopping proxy while ExoPlayer is reading from it
            // leads to EOF/Interrupted and "Shutting down" executor errors.
            stopParserOnly();

            parser = new AllohaParserJava(appContext);
            parser.rotateUserAgent();

            parser.parse(iframeUrl, new AllohaParserJava.Callback() {
            @Override
            public void onHlsLinksReceived(String json, Map<String, String> extraHeaders) {
                try {
                    Map<String, AllohaBnsiParserJava.QualityUrls> q = AllohaBnsiParserJava.parseQualityUrls(json);
                    qualitiesRef.set(q);
                    activeHeaders.clear();
                    activeHeaders.putAll(extraHeaders);
                    try { AllohaHttpTraceJava.logJsHeaders(appContext, "onReady", extraHeaders); } catch (Exception ignored) {}

                    mainHandler.post(() -> callback.onQualities(new LinkedHashMap<>(q)));
                    callback.onStatus("Alloha: bnsi parsed, waiting m3u8 refresh...");
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError("Alloha bnsi parse: " + safeMsg(e)));
                }
            }

            @Override
            public void onConfigUpdate(String edgeHash, int ttlSeconds, Map<String, String> extraHeaders) {
                activeHeaders.putAll(extraHeaders);
                try { AllohaHttpTraceJava.logJsHeaders(appContext, "config_update", extraHeaders); } catch (Exception ignored) {}
                callback.onStatus("Alloha: config_update ttl=" + ttlSeconds + "s");
            }

            @Override
            public void onM3u8Refreshed(String url, Map<String, String> extraHeaders) {
                activeHeaders.putAll(extraHeaders);
                try { AllohaHttpTraceJava.logJsHeaders(appContext, "m3u8_refresh", extraHeaders); } catch (Exception ignored) {}
                currentM3u8Url = url;

                try {
                    ensureProxy();
                    proxy.updateMasterUrl(url);
                    fallbackUsed = false;
                    mainHandler.post(() -> callback.onProxyReady(proxy));
                    callback.onStatus("Alloha: proxy updated with refreshed m3u8");
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError("Alloha proxy: " + safeMsg(e)));
                }
            }

            @Override
            public void onStreamHeadersUpdated(Map<String, String> extraHeaders) {
                activeHeaders.putAll(extraHeaders);
                try { AllohaHttpTraceJava.logJsHeaders(appContext, "stream_push", extraHeaders); } catch (Exception ignored) {}
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> callback.onError(error));
            }
            });
        });
    }

    /**
     * Restart WebView session (parser) without stopping proxy.
     * Debounced to avoid restart loops on bad CDN nodes.
     */
    private void requestSessionRestart(@NonNull String reason) {
        long now = System.currentTimeMillis();
        if (restartInFlight) return;
        if (now - lastRestartAtMs < RESTART_DEBOUNCE_MS) return;
        restartInFlight = true;
        lastRestartAtMs = now;
        callback.onStatus("Alloha: restarting session (" + reason + ")");
        mainHandler.post(() -> {
            try {
                stopParserOnly();
                if (currentIframeUrl != null && !currentIframeUrl.isBlank()) {
                    // Keep proxy alive; new m3u8/headers will update it.
                    start(currentIframeUrl);
                }
            } finally {
                restartInFlight = false;
            }
        });
    }

    /**
     * Remember chosen quality so fallback can switch to its 2nd URL.
     */
    public void setSelectedQualityKey(@Nullable String qualityKey) {
        selectedQualityKey = qualityKey == null ? "" : qualityKey;
    }

    /**
     * Call from ExoPlayer error callback. Switches to fallback URL ("or" second URL) once.
     */
    public void tryFallbackOnce() {
        if (fallbackUsed) return;
        if (selectedQualityKey.isBlank()) return;
        if (proxy == null) return;

        Map<String, AllohaBnsiParserJava.QualityUrls> map = qualitiesRef.get();
        AllohaBnsiParserJava.QualityUrls urls = map != null ? map.get(selectedQualityKey) : null;
        if (urls == null) return;

        String fallback = AllohaBnsiParserJava.pickWithFallback(urls, true);
        if (fallback == null || fallback.isBlank()) return;

        proxy.updateMasterUrl(fallback);
        fallbackUsed = true;
        callback.onStatus("Alloha: switched to fallback URL for " + selectedQualityKey);
    }

    public void stop() {
        // ensure WebView release happens on main thread as well
        if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
            mainHandler.post(this::stop);
            return;
        }
        if (proxy != null && proxy.isRunning()) {
            proxy.stop();
        }
        proxy = null;
        stopParserOnly();
        activeHeaders.clear();
        qualitiesRef.set(new LinkedHashMap<>());
        currentM3u8Url = "";
        selectedQualityKey = "";
        fallbackUsed = false;
    }

    @Nullable
    public String getCurrentM3u8Url() {
        return currentM3u8Url;
    }

    @NonNull
    public Map<String, String> getActiveHeadersSnapshot() {
        return new LinkedHashMap<>(activeHeaders);
    }

    private void ensureProxy() throws IOException {
        if (proxy != null && proxy.isRunning()) return;
        proxy = new HlsProxyServerJava(activeHeaders, () -> {
            Log.d(TAG, "Session expired -> restart");
            requestSessionRestart("proxy_session_expired");
        });
        proxy.start();
    }

    private void stopParserOnly() {
        if (parser != null) {
            try {
                parser.release();
            } catch (Exception ignored) {}
        }
        parser = null;
    }

    private String safeMsg(Throwable t) {
        return t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
    }
}

