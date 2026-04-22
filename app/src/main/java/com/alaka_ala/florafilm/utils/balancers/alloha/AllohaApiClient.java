package com.alaka_ala.florafilm.utils.balancers.alloha;

import static com.alaka_ala.florafilm.utils.balancers.Balancer.ALLOHA_ID;

import com.alaka_ala.florafilm.fragments.filmDetails.SelectorVoiceAdapter;
import com.alaka_ala.florafilm.utils.balancers.alloha.models.movie.MovieData;
import com.alaka_ala.florafilm.utils.balancers.alloha.models.movie.MovieResponse;
import com.alaka_ala.florafilm.utils.balancers.alloha.models.movie.TranslationIframe;
import com.alaka_ala.florafilm.utils.balancers.alloha.models.serial.Episode;
import com.alaka_ala.florafilm.utils.balancers.alloha.models.serial.EpisodeTranslation;
import com.alaka_ala.florafilm.utils.balancers.alloha.models.serial.Season;
import com.alaka_ala.florafilm.utils.balancers.alloha.models.serial.SeriesData;
import com.alaka_ala.florafilm.utils.balancers.alloha.models.serial.SeriesResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public final class AllohaApiClient {


    public AllohaApiClient(String token) {
        this.token = token;
    }

    private String token;

    public void fetch(int kpId, SelectorVoiceAdapter.AdapterData.AdapterDataCallback callback) throws Exception {
        fetch(String.valueOf(kpId), callback);
    }

    public void fetch(String kpId, SelectorVoiceAdapter.AdapterData.AdapterDataCallback callback) {
        try {
            String apiUrl = String.format(
                    Locale.ROOT,
                    "https://api.alloha.tv/?token=%s&kp=%s",
                    URLEncoder.encode(token.trim(), "UTF-8"),
                    URLEncoder.encode(kpId.trim(), "UTF-8")
            );

            String jsonStr = requestUnsafe(apiUrl);
            JSONObject dataObj = new JSONObject(jsonStr).getJSONObject("data");
            JSONObject seasonsObj = dataObj.optJSONObject("seasons");

            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

            if (seasonsObj == null) {
                // Фильм
                MovieResponse response = gson.fromJson(jsonStr, MovieResponse.class);
                callback.onDataReady(createFilmAdapterDataFromAlloha(response.getData()));
                return; // КРИТИЧНО: иначе ниже будет NPE
            }

            // Сериал
            SeriesResponse response = gson.fromJson(jsonStr, SeriesResponse.class);
            callback.onDataReady(createSerialAdapterDataFromAlloha(response.getData()));

        } catch (Exception e) {
            callback.onError(e.getMessage() != null ? e.getMessage() : "Alloha fetch failed");
        }
    }

    private int toIntSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private SelectorVoiceAdapter.AdapterData createFilmAdapterDataFromAlloha(MovieData movieData) {
        // Получаем данные фильма
        String posterUrl = movieData.getPoster();
        String title = movieData.getName();

        List<SelectorVoiceAdapter.Item> translationFolders = new ArrayList<>();
        List<Integer> allohaPath = new ArrayList<>(Arrays.asList(ALLOHA_ID));

        int translationIndex = 0;
        // Получаем карту переводов из translation_iframe
        Map<String, TranslationIframe> translations = movieData.getTranslation_iframe();

        if (translations != null && !translations.isEmpty()) {
            for (Map.Entry<String, TranslationIframe> entry : translations.entrySet()) {
                TranslationIframe translation = entry.getValue();

                List<SelectorVoiceAdapter.Item> qualityFiles = new ArrayList<>();
                List<Integer> translationPath = new ArrayList<>(allohaPath);
                translationPath.add(translationIndex);

                // Качество берем из объекта перевода
                String[] availableQualities = {translation.getQuality()};
                int qualityIndex = 0;
                for (String quality : availableQualities) {
                    List<Integer> qualityPath = new ArrayList<>(translationPath);
                    qualityPath.add(qualityIndex);

                    // Используем iframe из перевода как URL видео
                    String translationVideoUrl = translation.getIframe();
                    qualityFiles.add(new SelectorVoiceAdapter.File(quality, qualityPath, translationVideoUrl));
                    qualityIndex++;
                }

                SelectorVoiceAdapter.Folder translationFolder = new SelectorVoiceAdapter.Folder(
                        translation.getName(),
                        translationPath,
                        qualityFiles
                );
                translationFolders.add(translationFolder);
                translationIndex++;
            }
        } else {
            // Если нет переводов в translation_iframe, используем основной iframe
            List<SelectorVoiceAdapter.Item> qualityFiles = new ArrayList<>();
            List<Integer> translationPath = new ArrayList<>(allohaPath);
            translationPath.add(0);

            String[] availableQualities = {movieData.getQuality()};
            qualityFiles.add(new SelectorVoiceAdapter.File(
                    movieData.getQuality(),
                    translationPath,
                    movieData.getIframe()
            ));

            SelectorVoiceAdapter.Folder defaultFolder = new SelectorVoiceAdapter.Folder(
                    movieData.getTranslation(),
                    translationPath,
                    qualityFiles
            );
            translationFolders.add(defaultFolder);
        }

        SelectorVoiceAdapter.Folder allohaFolder = new SelectorVoiceAdapter.Folder("ALLOHA", allohaPath, translationFolders);
        return new SelectorVoiceAdapter.AdapterData(Arrays.asList(allohaFolder), posterUrl, title);
    }

    private SelectorVoiceAdapter.AdapterData createSerialAdapterDataFromAlloha(SeriesData serial) {
        String posterUrl = serial.getPoster();
        String title = serial.getName();

        List<SelectorVoiceAdapter.Item> seasonFolders = new ArrayList<>();
        List<Integer> allohaPath = new ArrayList<>(List.of(ALLOHA_ID));

        Map<String, Season> seasons = serial.getSeasons();
        if (seasons != null && !seasons.isEmpty()) {
            int seasonIndex = 0;

            // СОРТИРУЕМ сезоны по номеру (от меньшего к большему)
            List<Map.Entry<String, Season>> sortedSeasons = new ArrayList<>(seasons.entrySet());
            sortedSeasons.sort((a, b) -> {
                int seasonA = a.getValue().getSeason();
                int seasonB = b.getValue().getSeason();
                return Integer.compare(seasonA, seasonB);
            });

            for (Map.Entry<String, Season> seasonEntry : sortedSeasons) {
                Season season = seasonEntry.getValue();

                List<SelectorVoiceAdapter.Item> episodeFolders = new ArrayList<>();
                List<Integer> seasonPath = new ArrayList<>(allohaPath);
                seasonPath.add(seasonIndex);

                Map<String, Episode> episodes = season.getEpisodes();
                if (episodes != null && !episodes.isEmpty()) {
                    int episodeIndex = 0;

                    // СОРТИРУЕМ эпизоды по номеру (от меньшего к большему)
                    List<Map.Entry<String, Episode>> sortedEpisodes = new ArrayList<>(episodes.entrySet());
                    sortedEpisodes.sort((a, b) -> {
                        int episodeA = a.getValue().getEpisode();
                        int episodeB = b.getValue().getEpisode();
                        return Integer.compare(episodeA, episodeB);
                    });

                    for (Map.Entry<String, Episode> episodeEntry : sortedEpisodes) {
                        Episode episode = episodeEntry.getValue();

                        Map<String, EpisodeTranslation> translations = episode.getTranslation();
                        if (translations != null && !translations.isEmpty()) {
                            List<SelectorVoiceAdapter.Item> translationFolders = new ArrayList<>();
                            List<Integer> episodePath = new ArrayList<>(seasonPath);
                            episodePath.add(episodeIndex);

                            int translationIndex = 0;

                            // СОРТИРУЕМ переводы по названию (опционально)
                            List<Map.Entry<String, EpisodeTranslation>> sortedTranslations = new ArrayList<>(translations.entrySet());
                            sortedTranslations.sort((a, b) -> a.getKey().compareTo(b.getKey()));

                            for (Map.Entry<String, EpisodeTranslation> transEntry : sortedTranslations) {
                                EpisodeTranslation translation = transEntry.getValue();

                                List<SelectorVoiceAdapter.Item> qualityFiles = new ArrayList<>();
                                List<Integer> translationPath = new ArrayList<>(episodePath);
                                translationPath.add(translationIndex);

                                String[] availableQualities = {translation.getQuality()};
                                int qualityIndex = 0;
                                for (String quality : availableQualities) {
                                    List<Integer> qualityPath = new ArrayList<>(translationPath);
                                    qualityPath.add(qualityIndex);

                                    String videoUrl = translation.getIframe();
                                    qualityFiles.add(new SelectorVoiceAdapter.File(quality, qualityPath, videoUrl));
                                    qualityIndex++;
                                }

                                SelectorVoiceAdapter.Folder translationFolder = new SelectorVoiceAdapter.Folder(
                                        translation.getTranslation(),
                                        translationPath,
                                        qualityFiles
                                );
                                translationFolders.add(translationFolder);
                                translationIndex++;
                            }

                            String episodeTitle = "Серия " + episode.getEpisode();
                            SelectorVoiceAdapter.Folder episodeFolder = new SelectorVoiceAdapter.Folder(
                                    episodeTitle,
                                    episodePath,
                                    translationFolders
                            );
                            episodeFolders.add(episodeFolder);
                        }
                        episodeIndex++;
                    }
                }

                String seasonTitle = "Сезон " + season.getSeason();
                SelectorVoiceAdapter.Folder seasonFolder = new SelectorVoiceAdapter.Folder(
                        seasonTitle,
                        seasonPath,
                        episodeFolders
                );
                seasonFolders.add(seasonFolder);
                seasonIndex++;
            }
        }

        SelectorVoiceAdapter.Folder allohaFolder = new SelectorVoiceAdapter.Folder("ALLOHA", allohaPath, seasonFolders);
        return new SelectorVoiceAdapter.AdapterData(Arrays.asList(allohaFolder), posterUrl, title);
    }

    private String requestUnsafe(String url) throws Exception {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());
        connection.setSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        connection.setHostnameVerifier(allHostsValid);
        connection.setRequestMethod("GET");

        StringBuilder builder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }
}
