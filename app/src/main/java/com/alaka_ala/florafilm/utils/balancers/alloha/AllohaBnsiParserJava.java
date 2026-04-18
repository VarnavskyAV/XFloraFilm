package com.alaka_ala.florafilm.utils.balancers.alloha;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Java-эквивалент Kotlin-логики из onHlsLinksReceived:
 * value quality может быть вида "primaryUrl or fallbackUrl".
 */
public final class AllohaBnsiParserJava {
    private AllohaBnsiParserJava() {
    }

    public static final class QualityUrls {
        public final String primaryUrl;
        public final String fallbackUrl;

        public QualityUrls(String primaryUrl, String fallbackUrl) {
            this.primaryUrl = primaryUrl;
            this.fallbackUrl = fallbackUrl;
        }
    }

    /**
     * Парсит bnsi JSON и возвращает quality -> {primary,fallback}.
     */
    public static Map<String, QualityUrls> parseQualityUrls(String json) throws Exception {
        JSONObject jsonObj = new JSONObject(json);
        JSONArray hlsSource = jsonObj.optJSONArray("hlsSource");
        if (hlsSource == null) {
            throw new IllegalStateException("No hlsSource");
        }

        Map<String, QualityUrls> result = new LinkedHashMap<>();
        for (int i = 0; i < hlsSource.length(); i++) {
            JSONObject qualityObj = hlsSource.getJSONObject(i).optJSONObject("quality");
            if (qualityObj == null) {
                continue;
            }
            for (java.util.Iterator<String> it = qualityObj.keys(); it.hasNext(); ) {
                String quality = it.next();
                String raw = qualityObj.optString(quality, "");
                String[] parts = raw.split(" or ");

                String primary = normalize(parts.length > 0 ? parts[0] : "");
                String fallback = normalize(parts.length > 1 ? parts[1] : "");

                if (!primary.isBlank() || !fallback.isBlank()) {
                    result.put(quality, new QualityUrls(primary, fallback));
                }
            }
        }

        if (result.isEmpty()) {
            throw new IllegalStateException("No qualities found");
        }
        return result;
    }

    /**
     * Возвращает URL для quality с логикой:
     * 1) primary
     * 2) fallback (если primary пустой/недоступен снаружи вашей логики)
     */
    public static String pickWithFallback(QualityUrls urls, boolean primaryUnavailable) {
        if (urls == null) {
            return "";
        }
        if (!primaryUnavailable && !urls.primaryUrl.isBlank()) {
            return urls.primaryUrl;
        }
        if (!urls.fallbackUrl.isBlank()) {
            return urls.fallbackUrl;
        }
        return urls.primaryUrl;
    }

    private static String normalize(String url) {
        String value = url == null ? "" : url.trim();
        if (value.startsWith("//")) {
            return "https:" + value;
        }
        return value;
    }
}
