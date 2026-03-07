package com.alaka_ala.florafilm.ui.utils.hdvb;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.alaka_ala.florafilm.ui.fragments.filmDetails.SelectorVoiceAdapter;
import com.alaka_ala.florafilm.ui.utils.Balancer;
import com.alaka_ala.florafilm.ui.utils.hdvb.models.EpisodeResponse;
import com.alaka_ala.florafilm.ui.utils.hdvb.models.HDVBApiResponse;
import com.alaka_ala.florafilm.ui.utils.hdvb.models.PlayerConfig;
import com.alaka_ala.florafilm.ui.utils.hdvb.models.SeasonResponse;
import com.alaka_ala.florafilm.ui.utils.hdvb.models.TranslationResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HDVB implements Balancer {

    public static final String TYPE_CONTENT_FILM = "movie";
    public static final String TYPE_CONTENT_SERIAL = "serial";
    private static final Map<String, String> headers = new HashMap<>();
    private final String API_KEY;
    private final Gson gson;

    private static String X_CSRF_TOKEN = "AALlPbfua1Kxj1K3Ohk$rlmBM-zm3e9ENIgU-sLEuU6K5OWgpdwEG8DyEC7FLwvV";
    private static String ORIGIN = "[url]vb17123filippaaniketos.pw[/url]";
    private static String IFRAME = "";
    private static String FILE;
    private static String HREF;
    private static String CUID;
    private static String REFERER = "[url=vb17123filippaaniketos.pw][/url]";
    private static final String HDVB_API_DOMAIN = "https://apivb.com";

    public HDVB(String apiKey) {
        API_KEY = apiKey;
        this.gson = new GsonBuilder().create();
        createMapHeaders();
    }

    public interface AdapterDataCallback {
        void onDataReady(AdapterData data);
        void onError(String error);
    }

    public static class AdapterData {
        private final List<SelectorVoiceAdapter.Folder> rootFolders;
        private final String posterUrl;
        private final String title;

        public AdapterData(List<SelectorVoiceAdapter.Folder> rootFolders, String posterUrl, String title) {
            this.rootFolders = rootFolders;
            this.posterUrl = posterUrl;
            this.title = title;
        }

        public List<SelectorVoiceAdapter.Folder> getRootFolders() {
            return rootFolders;
        }

        public String getPosterUrl() {
            return posterUrl;
        }

        public String getTitle() {
            return title;
        }
    }

    public void getAdapterData(int kinopoiskId, AdapterDataCallback callback) {
        String urlString = HDVB_API_DOMAIN + "/api/videos.json?id_kp=" + kinopoiskId + "&token=" + API_KEY;
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder();
        for (String headerKey : headers.keySet()) {
            requestBuilder.addHeader(headerKey, Objects.requireNonNull(headers.get(headerKey)));
        }
        requestBuilder.url(urlString);
        Request request = requestBuilder.build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnMainThread(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnMainThread(() -> callback.onError("Ошибка запроса: " + response.code()));
                    return;
                }
                String body = response.body().string();
                Type listType = new TypeToken<List<HDVBApiResponse>>(){}.getType();
                List<HDVBApiResponse> apiResponses = gson.fromJson(body, listType);

                if (apiResponses == null || apiResponses.isEmpty()) {
                    runOnMainThread(() -> callback.onError("Фильм отсутствует"));
                    return;
                }
                HDVBApiResponse firstResponse = apiResponses.get(0);
                try {
                    if (TYPE_CONTENT_FILM.equals(firstResponse.getType())) {
                        processFilm(apiResponses, callback);
                    } else if (TYPE_CONTENT_SERIAL.equals(firstResponse.getType())) {
                        processSerial(firstResponse, callback);
                    } else {
                        runOnMainThread(() -> callback.onError("Неизвестный тип контента: " + firstResponse.getType()));
                    }
                } catch (Exception e) {
                    runOnMainThread(() -> callback.onError("Ошибка обработки: " + e.getMessage()));
                }
            }
        });
    }

    private void processFilm(List<HDVBApiResponse> films, AdapterDataCallback callback) {
        HDVBApiResponse firstFilm = films.get(0);
        IFRAME = firstFilm.getIframeUrl();
        try {
            PlayerConfig playerConfig = parsePlayerConfig(IFRAME);
            if (playerConfig == null) {
                runOnMainThread(() -> callback.onError("Не удалось получить конфигурацию плеера"));
                return;
            }
            updateConfig(playerConfig);
            String videoUrl = getFileFilm();
            AdapterData adapterData = createFilmAdapterData(films, videoUrl);
            runOnMainThread(() -> callback.onDataReady(adapterData));
        } catch (Exception e) {
            runOnMainThread(() -> callback.onError("Ошибка обработки фильма: " + e.getMessage()));
        }
    }

    private void processSerial(HDVBApiResponse serial, AdapterDataCallback callback) {
        IFRAME = serial.getIframeUrl();
        try {
            PlayerConfig playerConfig = parsePlayerConfig(IFRAME);
            if (playerConfig == null) {
                runOnMainThread(() -> callback.onError("Не удалось получить конфигурацию плеера"));
                return;
            }
            updateConfig(playerConfig);
            List<SeasonResponse> seasons = getSeasonsData();
            AdapterData adapterData = createSerialAdapterData(serial, seasons);
            runOnMainThread(() -> callback.onDataReady(adapterData));
        } catch (Exception e) {
            runOnMainThread(() -> callback.onError("Ошибка обработки сериала: " + e.getMessage()));
        }
    }

    private AdapterData createFilmAdapterData(List<HDVBApiResponse> films, String videoUrl) {
        HDVBApiResponse firstFilm = films.get(0);
        String posterUrl = firstFilm.getPoster();
        String title = firstFilm.getTitleRu();

        List<SelectorVoiceAdapter.Item> translationFolders = new ArrayList<>();
        List<Integer> hdvbPath = new ArrayList<>(Arrays.asList(HDVB_ID));

        int translationIndex = 0;
        for (HDVBApiResponse film : films) {
            List<SelectorVoiceAdapter.Item> qualityFiles = new ArrayList<>();
            List<Integer> translationPath = new ArrayList<>(hdvbPath);
            translationPath.add(translationIndex);

            String[] availableQualities = {"AUTO"};
            int qualityIndex = 0;
            for (String quality : availableQualities) {
                List<Integer> qualityPath = new ArrayList<>(translationPath);
                qualityPath.add(qualityIndex);

                qualityFiles.add(new SelectorVoiceAdapter.File(quality, qualityPath, videoUrl));
                qualityIndex++;
            }

            SelectorVoiceAdapter.Folder translationFolder = new SelectorVoiceAdapter.Folder(film.getTranslator(), translationPath, qualityFiles);
            translationFolders.add(translationFolder);
            translationIndex++;
        }

        SelectorVoiceAdapter.Folder hdvbFolder = new SelectorVoiceAdapter.Folder("HDVB", hdvbPath, translationFolders);
        return new AdapterData(Arrays.asList(hdvbFolder), posterUrl, title);
    }

    private AdapterData createSerialAdapterData(HDVBApiResponse serial, List<SeasonResponse> seasons) {
        String posterUrl = serial.getPoster();
        String title = serial.getTitleRu();

        List<SelectorVoiceAdapter.Item> seasonFolders = new ArrayList<>();
        List<Integer> hdvbPath = new ArrayList<>(Arrays.asList(HDVB_ID));

        if (seasons != null) {
            int seasonIndex = 0;
            for (SeasonResponse season : seasons) {
                List<SelectorVoiceAdapter.Item> episodeFolders = new ArrayList<>();
                List<Integer> seasonPath = new ArrayList<>(hdvbPath);
                seasonPath.add(seasonIndex);

                if (season.getEpisodes() != null) {
                    int episodeIndex = 0;
                    for (EpisodeResponse episode : season.getEpisodes()) {
                        TranslationResponse translation = episode.getTranslation();
                        if (translation != null) {
                            List<SelectorVoiceAdapter.Item> translationFolders = new ArrayList<>();
                            List<Integer> episodePath = new ArrayList<>(seasonPath);
                            episodePath.add(episodeIndex);

                            List<SelectorVoiceAdapter.Item> qualityFiles = new ArrayList<>();
                            List<Integer> translationPath = new ArrayList<>(episodePath);
                            translationPath.add(0);

                            String[] availableQualities = {"AUTO"};
                            int qualityIndex = 0;
                            for (String quality : availableQualities) {
                                List<Integer> qualityPath = new ArrayList<>(translationPath);
                                qualityPath.add(qualityIndex);

                                String episodeToken = translation.getFile();
                                String videoUrl = "https://" + HREF + "/playlist/" + episodeToken + ".txt";
                                qualityFiles.add(new SelectorVoiceAdapter.File(quality, qualityPath, videoUrl));
                                qualityIndex++;
                            }

                            SelectorVoiceAdapter.Folder translationFolder = new SelectorVoiceAdapter.Folder(translation.getTitle(), translationPath, qualityFiles);
                            translationFolders.add(translationFolder);

                            SelectorVoiceAdapter.Folder episodeFolder = new SelectorVoiceAdapter.Folder(episode.getTitle(), episodePath, translationFolders);
                            episodeFolders.add(episodeFolder);
                            episodeIndex++;
                        }
                    }
                }
                SelectorVoiceAdapter.Folder seasonFolder = new SelectorVoiceAdapter.Folder(season.getTitle(), seasonPath, episodeFolders);
                seasonFolders.add(seasonFolder);
                seasonIndex++;
            }
        }

        SelectorVoiceAdapter.Folder hdvbFolder = new SelectorVoiceAdapter.Folder("HDVB", hdvbPath, seasonFolders);
        return new AdapterData(Arrays.asList(hdvbFolder), posterUrl, title);
    }

    private PlayerConfig parsePlayerConfig(String iframeUrl) throws IOException {
        Document html = Jsoup.connect(iframeUrl).headers(headers).get();
        String jsonString = extractJson(html.toString());
        if (!jsonString.isEmpty()) {
            return gson.fromJson(jsonString, PlayerConfig.class);
        }
        return null;
    }

    private void updateConfig(PlayerConfig config) {
        X_CSRF_TOKEN = config.getKey();
        FILE = config.getFile();
        HREF = config.getHref();
        ORIGIN = "[url]" + HREF + "[/url]";
        CUID = config.getCuid();
        createMapHeaders();
    }

    private String getFileFilm() throws IOException {
        String urlM3u8;
        if (FILE.startsWith("/playlist/") && FILE.endsWith(".txt")) {
            urlM3u8 = IFRAME.replaceAll("/movie.+", "") + FILE;
        } else {
            urlM3u8 = IFRAME.replaceAll("/movie.+", "") + "/playlist/" + FILE.replace("~", "") + ".txt";
        }

        URL url = new URL(urlM3u8);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        for (String headerKey : headers.keySet()) {
            connection.setRequestProperty(headerKey, headers.get(headerKey));
        }
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(10000);
        connection.connect();

        if (connection.getResponseCode() == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        return "";
    }

    private List<SeasonResponse> getSeasonsData() throws IOException {
        String urlStr = IFRAME.replaceAll(".com.+", ".com") + FILE + "?key=" + X_CSRF_TOKEN + "&href=" + HREF + "&cuid=" + CUID;
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        for (String headerKey : headers.keySet()) {
            connection.setRequestProperty(headerKey, headers.get(headerKey));
        }
        connection.setRequestMethod("GET");
        connection.connect();

        if (connection.getResponseCode() == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            reader.close();
            return parseSeasonsManually(response);
        }
        return new ArrayList<>();
    }

    private List<SeasonResponse> parseSeasonsManually(String jsonResponse) {
        List<SeasonResponse> seasons = new ArrayList<>();
        try {
            JSONArray seasonsArray = new JSONArray(jsonResponse);
            for (int i = 0; i < seasonsArray.length(); i++) {
                JSONObject seasonJson = seasonsArray.getJSONObject(i);
                SeasonResponse season = new SeasonResponse();
                season.setTitle(seasonJson.getString("title"));
                season.setId(seasonJson.getString("id"));
                JSONArray episodesArray = seasonJson.getJSONArray("folder");
                List<EpisodeResponse> episodes = new ArrayList<>();
                for (int j = 0; j < episodesArray.length(); j++) {
                    JSONObject episodeJson = episodesArray.getJSONObject(j);
                    EpisodeResponse episode = new EpisodeResponse();
                    episode.setEpisode(episodeJson.getString("episode"));
                    episode.setTitle(episodeJson.getString("title"));
                    episode.setId(episodeJson.getString("id"));
                    JSONArray filesArray = episodeJson.getJSONArray("folder");
                    List<Object> files = new ArrayList<>();
                    if (filesArray.length() > 0) {
                        JSONObject translationJson = filesArray.getJSONObject(0);
                        TranslationResponse translation = new TranslationResponse();
                        translation.setFile(translationJson.getString("file"));
                        translation.setTitle(translationJson.getString("title"));
                        translation.setTranslator(translationJson.getString("translator"));
                        translation.setId(translationJson.getString("id"));
                        translation.setText2(translationJson.optString("text2", ""));
                        files.add(translation);
                        if (filesArray.length() > 1) {
                            files.add(new ArrayList<>());
                        }
                    }
                    episode.setFiles(files);
                    episodes.add(episode);
                }
                season.setEpisodes(episodes);
                seasons.add(season);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return seasons;
    }

    private String extractJson(String html) {
        Pattern[] patterns = {
                Pattern.compile("playerConfigs = +(.+);{1}"),
                Pattern.compile("playerConfigs = +(.+); {1}"),
                Pattern.compile("playerConfigs = +(.+); {3}"),
                Pattern.compile("playerConfigs = +(.+);{3}"),
                Pattern.compile("playerConfigs = +(.+); {2}"),
                Pattern.compile("playerConfigs = +(.+);{2}")
        };
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(html);
            if (matcher.find() && matcher.groupCount() > 0) {
                return matcher.group(0)
                        .replaceAll("playerConfigs = ", "")
                        .replaceAll(";", "");
            }
        }
        return "";
    }

    private void runOnMainThread(Runnable action) {
        new Handler(Looper.getMainLooper()).post(action);
    }

    private static void createMapHeaders() {
        final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36";
        final String ACCEPT = "*/*";
        final String ACCEPT_ENCODING = "text, deflate, br";
        final String ACCEPT_LANGUAGE = "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7";
        final String CONTENT_TYPE = "application/x-www-form-urlencoded";
        final String SEC_CH_UA = "Not A;Brand\";v=\"99\", \"Chromium\";v=\"99\", \"Google Chrome\";v=\"99\"";
        final String SEC_CH_UA_MOBILE = "?1";
        final String SEC_CH_UA_PLATFORM = "Android";
        final String SEC_FETCH_DEST = "empty";
        final String SEC_FETCH_MODE = "cors";
        final String SEC_FETCH_SITE = "same-origin";
        REFERER = "[url=" + IFRAME + "][/url]";
        headers.clear();
        headers.put("User-Agent", USER_AGENT);
        headers.put("accept", ACCEPT);
        headers.put("accept-encoding", ACCEPT_ENCODING);
        headers.put("accept-language", ACCEPT_LANGUAGE);
        headers.put("content-type", CONTENT_TYPE);
        headers.put("origin", ORIGIN);
        headers.put("referer", REFERER);
        headers.put("sec-ch-ua", SEC_CH_UA);
        headers.put("sec-ch-ua-mobile", SEC_CH_UA_MOBILE);
        headers.put("sec-ch-ua-platform", SEC_CH_UA_PLATFORM);
        headers.put("sec-fetch-dest", SEC_FETCH_DEST);
        headers.put("sec-fetch-mode", SEC_FETCH_MODE);
        headers.put("sec-fetch-site", SEC_FETCH_SITE);
        headers.put("x-csrf-token", X_CSRF_TOKEN);
    }

    public static String getFileSerial(String episodeToken) {
        // String urlM3u8 = "https://" + HREF + "/playlist/" + episodeToken + ".txt";
        createMapHeaders();
        URL createUrl = null;
        try {
            createUrl = new URL(episodeToken);
            HttpURLConnection myURLConnection = null;
            myURLConnection = (HttpURLConnection) createUrl.openConnection();
            for (String headerKey : headers.keySet()) {
                myURLConnection.setRequestProperty(headerKey, headers.get(headerKey));
            }
            myURLConnection.setRequestMethod("GET");
            myURLConnection.connect();
            if (myURLConnection.getResponseCode() == 200) {
                // запись ответа
                BufferedReader bufferedReader;
                bufferedReader = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
                String urlsM3u8 = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
                return urlsM3u8;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
        return "undefined";
    }

}
