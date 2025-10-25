package com.alaka_ala.florafilm.ui.utils.kinopoisk;

import androidx.annotation.NonNull;

import com.alaka_ala.florafilm.ui.utils.kinopoisk.constants.Constants;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Оптимизированный клиент для работы с Kinopoisk API в виде Singleton.
 */
public class KinopoiskApiClient {
    
    private static final String BASE_URL = "https://kinopoiskapiunofficial.tech/api/";
    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";

    private static volatile KinopoiskApiClient instance;
    
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final String apiKey;



    private static String getInternalApiKey() {
        // Ключ \"920aaf6a-9f64-46f7-bda7-209fb1069440\", записанный задом наперед.
        String reversedKey = "0449601bf902-7adb-7f64-46f9-a6faa029";
        return new StringBuilder(reversedKey).reverse().toString();
    }

    /**
     * Приватный конструктор с кастомным API ключом и кастомным OkHttpClient.
     */
    private KinopoiskApiClient(String apiKey, OkHttpClient httpClient) {
        this.apiKey = apiKey;
        this.gson = new GsonBuilder()
                .setLenient()
                .create();
        this.httpClient = httpClient;
    }

    /**
     * Инициализирует Singleton с внутренним ключом.
     */
    public static void initialize() {
        if (instance == null) {
            synchronized (KinopoiskApiClient.class) {
                if (instance == null) {
                    instance = new KinopoiskApiClient(getInternalApiKey(), new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .build());
                }
            }
        }
    }

    /**
     * Инициализирует Singleton с кастомным ключом.
     */
    public static void initialize(String apiKey) {
        if (instance == null) {
            synchronized (KinopoiskApiClient.class) {
                if (instance == null) {
                    instance = new KinopoiskApiClient(apiKey, new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .build());
                }
            }
        }
    }

    /**
     * Возвращает единственный экземпляр клиента.
     * @return экземпляр KinopoiskApiClient.
     * @throws IllegalStateException если клиент не был инициализирован.
     */
    public static KinopoiskApiClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("KinopoiskApiClient must be initialized in Application class first.");
        }
        return instance;
    }
    
    /**
     * Интерфейс для колбэков успешного результата
     */
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(ApiException error);
    }
    
    /**
     * Кастомное исключение для API ошибок
     */
    public static class ApiException extends Exception {
        private final int code;
        private final String message;
        
        public ApiException(int code, String message) {
            super(message);
            this.code = code;
            this.message = message;
        }
        
        public int getCode() {
            return code;
        }
        
        @Override
        public String getMessage() {
            return message;
        }
    }

    /**
     * Получает указанную коллекцию фильмов.
     * @param type Тип коллекции (из FilmCollectionType) -> {@link Constants.FilmCollectionType}.
     * @param page Номер страницы.
     * @param callback Колбэк для обработки результата.
     */
    public void getFilmCollection(Constants.FilmCollectionType type, int page, ApiCallback<FilmCollection> callback) {
        String url = BASE_URL + "v2.2/films/collections?type=" + type.getTypeName() + "&page=" + page;
        makeRequest(url, FilmCollection.class, new ApiCallback<FilmCollection>() {
            @Override
            public void onSuccess(FilmCollection result) {
                if (result != null) {
                    result.setTitle(type.getTypeName());
                }
                callback.onSuccess(result);
            }

            @Override
            public void onError(ApiException error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Получение фильмов по жанру
     */
    public void getFilmsByGenre(int genreId, int page, ApiCallback<FilmCollection> callback) {
        String url = BASE_URL + "v2.2/films?genres=" + genreId +
                "&order=RATING&type=ALL&ratingFrom=0&ratingTo=10&yearFrom=1000&yearTo=3000&page=" + page;
        makeRequest(url, FilmCollection.class, callback);
    }

    /**
     * Получение фильмов по стране
     */
    public void getFilmsByCountry(int countryId, int page, ApiCallback<FilmCollection> callback) {
        String url = BASE_URL + "v2.2/films?countries=" + countryId +
                "&order=RATING&type=FILM&ratingFrom=0&ratingTo=10&yearFrom=1000&yearTo=3000&page=" + page;
        makeRequest(url, FilmCollection.class, callback);
    }

    /**
     * Поиск фильмов по ключевому слову
     */
    public void searchFilms(String keyword, int page, ApiCallback<FilmCollection> callback) {
        try {
            String encodedKeyword = java.net.URLEncoder.encode(keyword, "UTF-8");
            String url = BASE_URL + "v2.1/films/search-by-keyword?keyword=" + encodedKeyword + "&page=" + page;
            makeRequest(url, FilmCollection.class, callback);
        } catch (Exception e) {
            callback.onError(new ApiException(0, "Ошибка кодирования поискового запроса: " + e.getMessage()));
        }
    }

    /**
     * Получение похожих фильмов
     */
    public void getSimilarFilms(int kinopoiskId, ApiCallback<FilmCollection> callback) {
        String url = BASE_URL + "v2.2/films/" + kinopoiskId + "/similars";
        makeRequest(url, FilmCollection.class, callback);
    }

    /**
     * Получение детальной информации о фильме
     */
    public void getFilmDetails(int kinopoiskId, ApiCallback<FilmDetails> callback) {
        String url = BASE_URL + "v2.2/films/" + kinopoiskId;
        makeRequest(url, FilmDetails.class, callback);
    }

    /**
     * Получение трейлеров фильма
     */
    public void getFilmTrailers(int kinopoiskId, ApiCallback<List<FilmTrailer>> callback) {
        String url = BASE_URL + "v2.2/films/" + kinopoiskId + "/videos";
        makeRequest(url, new TypeToken<List<FilmTrailer>>(){}.getType(), callback);
    }

    /**
     * Получение изображений фильма
     */
    public void getFilmImages(int kinopoiskId, String type, int page, ApiCallback<FilmImages> callback) {
        String url = BASE_URL + "v2.2/films/" + kinopoiskId + "/images?type=" + type + "&page=" + page;
        makeRequest(url, FilmImages.class, callback);
    }

    /**
     * Универсальный метод для выполнения HTTP запросов
     */
    private <T> void makeRequest(String url, Class<T> responseClass, ApiCallback<T> callback) {
        makeRequest(url, (Type) responseClass, callback);
    }

    private <T> void makeRequest(String url, Type responseType, ApiCallback<T> callback) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader(API_KEY_HEADER, this.apiKey)
                .addHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON)
                .build();
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(new ApiException(0, "Ошибка сети: " + e.getMessage()));
            }
            
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        callback.onError(new ApiException(response.code(), 
                            "HTTP ошибка: " + response.code() + " - " + response.message()));
                        return;
                    }
                    
                    String responseBody = response.body().string();
                    if (responseBody == null || responseBody.trim().isEmpty()) {
                        callback.onError(new ApiException(response.code(), "Пустой ответ от сервера"));
                        return;
                    }
                    
                    if (!JsonParser.parseString(responseBody).isJsonObject() && 
                        !JsonParser.parseString(responseBody).isJsonArray()) {
                        callback.onError(new ApiException(response.code(), "Неверный формат JSON"));
                        return;
                    }
                    
                    T result = gson.fromJson(responseBody, responseType);
                    callback.onSuccess(result);
                    
                } catch (Exception e) {
                    callback.onError(new ApiException(0, "Ошибка парсинга: " + e.getMessage()));
                } finally {
                    if (response.body() != null) {
                        response.body().close();
                    }
                }
            }
        });
    }
    
    /**
     * Закрытие HTTP клиента
     */
    public void close() {
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
    }
}
