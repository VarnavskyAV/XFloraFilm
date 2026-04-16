package com.alaka_ala.florafilm.utils.balancers.alloha;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;

public final class AllohaHttpTraceJava {
    public static final String FILE_NAME = "alloha_http_trace.jsonl";
    private static final String TAG = "AllohaHttpTrace";
    private static final AtomicInteger SEQ = new AtomicInteger(0);
    private static final ExecutorService WRITER = Executors.newSingleThreadExecutor();
    private static volatile String lastStreamAuth;
    private static volatile String lastStreamAccepts;

    private AllohaHttpTraceJava() {
    }

    public static File traceFile(Context context) {
        return new File(context.getApplicationContext().getFilesDir(), FILE_NAME);
    }

    public static void reset(Context context) {
        SEQ.set(0);
        lastStreamAuth = null;
        lastStreamAccepts = null;
        try {
            JSONObject start = new JSONObject();
            start.put("kind", "session_start");
            start.put("ts", System.currentTimeMillis());
            start.put("file", FILE_NAME);
            Files.write(
                    traceFile(context).toPath(),
                    (start + "\n").getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (Exception e) {
            Log.w(TAG, "reset failed", e);
        }
    }

    public static void logOkHttpRoundTrip(Context context, Request request, Response response, String error) {
        int id = SEQ.incrementAndGet();
        try {
            JSONObject row = new JSONObject();
            row.put("kind", "okhttp");
            row.put("id", id);
            row.put("ts", System.currentTimeMillis());
            row.put("url", request.url().toString());
            row.put("method", request.method());
            row.put("requestHeaders", headersToJson(request.headers()));
            if (error != null) {
                row.put("error", error);
            }
            if (response != null) {
                row.put("code", response.code());
                row.put("message", response.message());
                row.put("responseHeaders", headersToJson(response.headers()));
            }
            appendLineAsync(context, row.toString());
        } catch (Exception e) {
            Log.w(TAG, "trace okhttp failed", e);
        }
    }

    public static void logJsHeaders(Context context, String source, Map<String, String> headers) {
        if ("stream_push".equals(source)) {
            String a = headers.get("authorizations");
            String c = headers.get("accepts-controls");
            if (safeEquals(a, lastStreamAuth) && safeEquals(c, lastStreamAccepts)) {
                return;
            }
            lastStreamAuth = a;
            lastStreamAccepts = c;
        }

        int id = SEQ.incrementAndGet();
        try {
            JSONObject row = new JSONObject();
            row.put("kind", "js_headers");
            row.put("id", id);
            row.put("ts", System.currentTimeMillis());
            row.put("source", source);

            JSONObject rh = new JSONObject();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                rh.put(entry.getKey(), entry.getValue());
            }
            row.put("headers", rh);
            appendLineAsync(context, row.toString());
        } catch (Exception e) {
            Log.w(TAG, "trace headers failed", e);
        }
    }

    private static JSONObject headersToJson(Headers headers) throws Exception {
        JSONObject result = new JSONObject();
        for (int i = 0; i < headers.size(); i++) {
            result.put(headers.name(i), headers.value(i));
        }
        return result;
    }

    private static void appendLineAsync(Context context, String line) {
        Context app = context.getApplicationContext();
        WRITER.execute(() -> {
            try {
                Files.write(
                        traceFile(app).toPath(),
                        (line + "\n").getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );
            } catch (Exception e) {
                Log.w(TAG, "append failed", e);
            }
        });
    }

    private static boolean safeEquals(String a, String b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }
}
