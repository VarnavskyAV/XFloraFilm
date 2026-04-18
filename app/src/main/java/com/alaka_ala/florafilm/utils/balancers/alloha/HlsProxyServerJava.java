package com.alaka_ala.florafilm.utils.balancers.alloha;

import android.util.Base64;
import android.util.Log;
import android.webkit.CookieManager;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class HlsProxyServerJava {
    private static final String TAG = "HlsProxyJava";
    private static final int PORT = 8080;
    private static final int PREFETCH = 2;

    private final Map<String, String> activeHeaders;
    private final Runnable onSessionExpired;
    private final ExecutorService ioExecutor = Executors.newCachedThreadPool();
    private final ExecutorService acceptExecutor = Executors.newSingleThreadExecutor();

    private volatile ServerSocket serverSocket;

    public boolean isRunning() {
        return running;
    }

    private volatile boolean running;
    private volatile String activeMasterUrl = "";
    private volatile int sessionVersion = 0;
    private volatile ConnectionPool connectionPool = new ConnectionPool(5, 15, TimeUnit.SECONDS);
    private volatile OkHttpClient client = buildClient(connectionPool);

    private final byte[] emptyTsPacket = new byte[188];
    private final Object cacheLock = new Object();
    private final LinkedHashMap<String, byte[]> cache = new LinkedHashMap<String, byte[]>(PREFETCH + 2, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Entry<String, byte[]> eldest) {
            return size() > PREFETCH + 1;
        }
    };
    private final ConcurrentHashMap<String, CompletableFuture<byte[]>> inFlight = new ConcurrentHashMap<>();
    private final ArrayDeque<String> recentSegments = new ArrayDeque<>();

    // Kotlin-port: rewrite segment URL to current session path after refresh.
    @Nullable
    private String rewriteToCurrentPath(String oldUrl) {
        String master = activeMasterUrl;
        if (master == null || master.isBlank()) return null;
        int lastSlash = oldUrl.lastIndexOf('/');
        if (lastSlash < 0) return null;
        String segName = oldUrl.substring(lastSlash + 1);
        if (segName.isBlank() || !segName.contains(".ts")) return null;
        String newBase = master.substring(0, master.lastIndexOf('/') + 1);
        String oldBase = oldUrl.substring(0, lastSlash + 1);
        if (oldBase.equals(newBase)) return null;
        return newBase + segName;
    }

    public HlsProxyServerJava(Map<String, String> activeHeaders, Runnable onSessionExpired) {
        this.activeHeaders = Objects.requireNonNull(activeHeaders);
        this.onSessionExpired = onSessionExpired == null ? () -> {
        } : onSessionExpired;
        emptyTsPacket[0] = 0x47;
        emptyTsPacket[1] = 0x1F;
        emptyTsPacket[2] = (byte) 0xFF;
        emptyTsPacket[3] = 0x10;
    }

    public String getFixedMasterUrl() {
        return "http://127.0.0.1:" + PORT + "/master.m3u8";
    }

    public void updateMasterUrl(String url) {
        activeMasterUrl = url == null ? "" : url;
        sessionVersion++;
        ConnectionPool oldPool = connectionPool;
        connectionPool = new ConnectionPool(5, 15, TimeUnit.SECONDS);
        client = buildClient(connectionPool);
        synchronized (cacheLock) {
            cache.clear();
        }
        synchronized (recentSegments) {
            recentSegments.clear();
        }
        inFlight.clear();
        ioExecutor.execute(oldPool::evictAll);
    }

    public void start() throws IOException {
        if (running) {
            return;
        }
        running = true;
        serverSocket = new ServerSocket(PORT);
        acceptExecutor.execute(() -> {
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    ioExecutor.execute(() -> handleConnection(socket));
                } catch (Exception e) {
                    if (running) {
                        Log.w(TAG, "accept error: " + e.getMessage());
                    }
                }
            }
        });
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Exception ignored) {
        }
        acceptExecutor.shutdownNow();
        ioExecutor.shutdownNow();
    }

    public String proxyUrl(String originalUrl) {
        String encoded = Base64.encodeToString(originalUrl.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
        return "http://127.0.0.1:" + PORT + "/proxy?url=" + encoded;
    }

    private void handleConnection(Socket socket) {
        try (Socket s = socket;
             BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
             OutputStream output = s.getOutputStream()) {
            String requestLine = input.readLine();
            if (requestLine == null) {
                return;
            }
            String line;
            while ((line = input.readLine()) != null && !line.isBlank()) {
                // skip headers
            }
            String[] parts = requestLine.split(" ");
            String path = parts.length > 1 ? parts[1] : "";

            if (path.startsWith("/master.m3u8")) {
                String master = activeMasterUrl;
                if (master.isBlank()) {
                    send404(output);
                    return;
                }
                servePlaylist(master, output);
                return;
            }

            String encodedUrl = path.contains("url=") ? path.substring(path.indexOf("url=") + 4) : "";
            if (encodedUrl.isBlank()) {
                send404(output);
                return;
            }
            String url = new String(Base64.decode(URLDecoder.decode(encodedUrl, "UTF-8"), Base64.URL_SAFE));
            if (url.endsWith(".m3u8") || url.contains("master.m3u8")) {
                servePlaylist(url, output);
            } else {
                serveSegment(url, output);
            }
        } catch (Exception e) {
            Log.w(TAG, "connection error: " + e.getMessage());
        }
    }

    private void servePlaylist(String url, OutputStream out) throws IOException {
        String body = fetchText(url);
        if (body == null) {
            send404(out);
            return;
        }
        String rewritten = rewriteM3u8(body, url);
        byte[] bytes = rewritten.getBytes();
        out.write(("HTTP/1.1 200 OK\r\nContent-Type: application/vnd.apple.mpegurl\r\nContent-Length: "
                + bytes.length + "\r\nConnection: close\r\n\r\n").getBytes());
        out.write(bytes);
        out.flush();
    }

    private void serveSegment(String url, OutputStream out) throws IOException {
        byte[] bytes;
        synchronized (cacheLock) {
            bytes = cache.get(url);
        }
        if (bytes == null) {
            bytes = fetchBytes(url);
            if (bytes != null) {
                synchronized (cacheLock) {
                    cache.put(url, bytes);
                }
            }
        }
        if (bytes == null) {
            // Kotlin fix: try rewrite to current session path immediately
            String rewritten = rewriteToCurrentPath(url);
            if (rewritten != null) {
                Log.d(TAG, "Rewriting segment to current path: " + tail(rewritten));
                byte[] rewrittenBytes = fetchBytes(rewritten);
                if (rewrittenBytes != null) {
                    synchronized (cacheLock) {
                        cache.put(url, rewrittenBytes);
                    }
                    bytes = rewrittenBytes;
                }
            }
        }

        if (bytes == null) {
            CompletableFuture.runAsync(() -> fetchSegmentFromFreshPlaylist(url), ioExecutor);
            if (url.contains("-a1.ts") || url.contains("-a2.ts")) bytes = emptyTsPacket;
            else { send503(out); return; }
        }

        String ct = (url.contains(".vtt") || url.contains(".webvtt")) ? "text/vtt"
                : url.contains(".aac") ? "audio/aac"
                : (url.contains(".m4s") || url.contains(".mp4")) ? "video/mp4"
                : "video/MP2T";
        out.write(("HTTP/1.1 200 OK\r\nContent-Type: " + ct + "\r\nContent-Length: "
                + bytes.length + "\r\nConnection: close\r\n\r\n").getBytes());
        out.write(bytes);
        out.flush();

        // Prefetch next segments (Kotlin behavior)
        int idx;
        synchronized (recentSegments) {
            idx = indexOf(recentSegments, url);
        }
        if (idx >= 0) {
            for (int i = 1; i <= PREFETCH; i++) {
                String next;
                synchronized (recentSegments) {
                    next = getAt(recentSegments, idx + i);
                }
                if (next == null) break;
                boolean cached;
                synchronized (cacheLock) {
                    cached = cache.containsKey(next);
                }
                if (cached) continue;
                final String u = next;
                CompletableFuture.runAsync(() -> getOrFetch(u), ioExecutor);
            }
        }
    }

    private String rewriteM3u8(String content, String baseUrl) {
        String base = baseUrl.substring(0, baseUrl.lastIndexOf('/') + 1);
        List<String> segUrls = new ArrayList<>();
        StringBuilder out = new StringBuilder();
        String[] lines = content.split("\n");
        for (String line : lines) {
            String rewritten = line;
            if (!line.isBlank() && !line.startsWith("#")) {
                String abs = line.startsWith("http") ? line : base + line;
                rewritten = proxyUrl(abs);
                segUrls.add(abs);
            } else if (line.contains("URI=\"")) {
                int start = line.indexOf("URI=\"") + 5;
                int end = line.indexOf("\"", start);
                if (start > 4 && end > start) {
                    String uri = line.substring(start, end);
                    if (!uri.isBlank() && !"none".equalsIgnoreCase(uri)) {
                        String abs = uri.startsWith("http") ? uri : base + uri;
                        rewritten = line.substring(0, start) + proxyUrl(abs) + line.substring(end);
                    }
                }
            }
            out.append(rewritten).append('\n');
        }

        if (!segUrls.isEmpty()) {
            synchronized (recentSegments) {
                recentSegments.clear();
                recentSegments.addAll(segUrls);
            }
            for (int i = 0; i < Math.min(PREFETCH, segUrls.size()); i++) {
                String u = segUrls.get(i);
                CompletableFuture.runAsync(() -> getOrFetch(u), ioExecutor);
            }
        }
        return out.toString();
    }

    private byte[] getOrFetch(String url) {
        synchronized (cacheLock) {
            if (cache.containsKey(url)) {
                return cache.get(url);
            }
        }
        CompletableFuture<byte[]> future = inFlight.computeIfAbsent(url, key ->
                CompletableFuture.supplyAsync(() -> {
                    byte[] bytes = fetchBytes(key);
                    if (bytes != null) {
                        synchronized (cacheLock) {
                            cache.put(key, bytes);
                        }
                    }
                    inFlight.remove(key);
                    return bytes;
                }, ioExecutor)
        );
        try {
            return future.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            inFlight.remove(url);
            return null;
        }
    }

    private byte[] fetchSegmentFromFreshPlaylist(String failedUrl) {
        String segName = failedUrl.substring(failedUrl.lastIndexOf('/') + 1);
        long deadline = System.currentTimeMillis() + 30_000L;
        int lastVersion = -1;
        while (System.currentTimeMillis() < deadline) {
            int version = sessionVersion;
            if (version == lastVersion) {
                sleep(200);
                continue;
            }
            lastVersion = version;
            String master = activeMasterUrl;
            if (master.isBlank()) {
                continue;
            }
            String masterBody = fetchText(master);
            if (masterBody == null) {
                continue;
            }
            String variantPath = null;
            for (String l : masterBody.split("\n")) {
                if (!l.startsWith("#") && !l.isBlank()) {
                    variantPath = l;
                    break;
                }
            }
            if (variantPath == null) {
                continue;
            }
            String base = master.substring(0, master.lastIndexOf('/') + 1);
            String variantUrl = variantPath.startsWith("http") ? variantPath : base + variantPath;
            String variantBody = fetchText(variantUrl);
            if (variantBody == null) {
                continue;
            }
            String variantBase = variantUrl.substring(0, variantUrl.lastIndexOf('/') + 1);
            String newSegUrl = null;
            for (String l : variantBody.split("\n")) {
                if (!l.startsWith("#") && !l.isBlank() && l.substring(l.lastIndexOf('/') + 1).equals(segName)) {
                    newSegUrl = l.startsWith("http") ? l : variantBase + l;
                    break;
                }
            }
            if (newSegUrl == null) {
                return null;
            }
            byte[] bytes = fetchBytes(newSegUrl);
            if (bytes != null) {
                synchronized (cacheLock) {
                    cache.put(failedUrl, bytes);
                }
                return bytes;
            }
            onSessionExpired.run();
            return null;
        }
        return null;
    }

    private String fetchText(String url) {
        try (Response resp = client.newCall(buildRequest(url)).execute()) {
            if (!resp.isSuccessful()) {
                Log.w(TAG, "fetchText HTTP " + resp.code() + " for " + head(url));
                if (resp.code() == 403) {
                    connectionPool.evictAll();
                }
                // Retry once on 500/503 (CDN node hiccup)
                if (resp.code() == 500 || resp.code() == 503) {
                    connectionPool.evictAll();
                    Request retry = buildRequest(url).newBuilder().header("Connection", "close").build();
                    try (Response rr = client.newCall(retry).execute()) {
                        if (!rr.isSuccessful()) {
                            Log.w(TAG, "fetchText retry also HTTP " + rr.code() + " for " + head(url));
                            // stream-balancer keeps 500 -> force session restart
                            if (url != null && url.contains("stream-balancer")) onSessionExpired.run();
                            return null;
                        }
                        return rr.body() != null ? rr.body().string() : null;
                    }
                }
                return null;
            }
            return resp.body() != null ? resp.body().string() : null;
        } catch (Exception e) {
            Log.w(TAG, "fetchText exception: " + e.getMessage());
            return null;
        }
    }

    private byte[] fetchBytes(String url) {
        try (Response resp = client.newCall(buildRequest(url)).execute()) {
            if (resp.code() == 403) {
                Log.w(TAG, "fetchBytes 403 for " + tail(url) + ", evicting and retrying");
                connectionPool.evictAll();
                Request retry = buildRequest(url).newBuilder().header("Connection", "close").build();
                try (Response rr = client.newCall(retry).execute()) {
                    if (!rr.isSuccessful()) Log.w(TAG, "fetchBytes retry also " + rr.code() + " for " + tail(url));
                    return rr.isSuccessful() && rr.body() != null ? rr.body().bytes() : null;
                }
            }
            if (!resp.isSuccessful()) {
                Log.w(TAG, "fetchBytes HTTP " + resp.code() + " for " + tail(url));
                // Retry once on 500/503
                if (resp.code() == 500 || resp.code() == 503) {
                    connectionPool.evictAll();
                    Request retry = buildRequest(url).newBuilder().header("Connection", "close").build();
                    try (Response rr = client.newCall(retry).execute()) {
                        if (!rr.isSuccessful()) {
                            Log.w(TAG, "fetchBytes retry also " + rr.code() + " for " + tail(url));
                            if (url != null && url.contains("stream-balancer")) onSessionExpired.run();
                            return null;
                        }
                        return rr.body() != null ? rr.body().bytes() : null;
                    }
                }
            }
            return resp.isSuccessful() && resp.body() != null ? resp.body().bytes() : null;
        } catch (Exception e) {
            Log.w(TAG, "fetchBytes exception: " + e.getMessage());
            return null;
        }
    }

    private static String head(String s) {
        if (s == null) return "";
        return s.length() <= 80 ? s : s.substring(0, 80);
    }

    private static String tail(String s) {
        if (s == null) return "";
        return s.length() <= 60 ? s : s.substring(s.length() - 60);
    }

    private static int indexOf(ArrayDeque<String> dq, String value) {
        int i = 0;
        for (String v : dq) {
            if (value.equals(v)) return i;
            i++;
        }
        return -1;
    }

    @Nullable
    private static String getAt(ArrayDeque<String> dq, int index) {
        if (index < 0) return null;
        int i = 0;
        for (String v : dq) {
            if (i == index) return v;
            i++;
        }
        return null;
    }

    private Request buildRequest(String url) {
        Request.Builder builder = new Request.Builder().url(url)
                .header("User-Agent", activeHeaders.getOrDefault("user-agent", "Mozilla/5.0"))
                .header("Origin", activeHeaders.getOrDefault("origin", ""))
                .header("Referer", activeHeaders.getOrDefault("referer", ""))
                .header("Accept", "*/*");
        // Prevent stale keep-alive connections to CDN after pause / balancer hiccups
        if (url != null && url.contains("stream-balancer")) {
            builder.header("Connection", "close");
        }
        try {
            String cookie = CookieManager.getInstance().getCookie(url);
            if (cookie != null && !cookie.isBlank()) {
                builder.header("Cookie", cookie);
            }
        } catch (Exception ignored) {
        }
        addHeaderIfPresent(builder, "accepts-controls");
        addHeaderIfPresent(builder, "authorizations");
        addHeaderIfPresent(builder, "sec-fetch-dest");
        addHeaderIfPresent(builder, "sec-fetch-mode");
        addHeaderIfPresent(builder, "sec-fetch-site");
        addHeaderIfPresent(builder, "accept-language");
        return builder.build();
    }

    private void addHeaderIfPresent(Request.Builder builder, String key) {
        String value = activeHeaders.get(key);
        if (value != null && !value.isBlank()) {
            builder.header(key, value);
        }
    }

    private OkHttpClient buildClient(ConnectionPool pool) {
        return new OkHttpClient.Builder()
                .connectionPool(pool)
                .followRedirects(true)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    private void send404(OutputStream out) throws IOException {
        out.write("HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".getBytes());
        out.flush();
    }

    private void send503(OutputStream out) throws IOException {
        out.write("HTTP/1.1 503 Service Unavailable\r\nContent-Length: 0\r\nRetry-After: 2\r\nConnection: close\r\n\r\n".getBytes());
        out.flush();
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
