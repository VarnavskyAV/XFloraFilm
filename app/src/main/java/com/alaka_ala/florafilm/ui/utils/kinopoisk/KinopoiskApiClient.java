package com.alaka_ala.florafilm.ui.utils.kinopoisk;

import android.content.Context;
import androidx.annotation.NonNull;

import com.alaka_ala.florafilm.ui.utils.kinopoisk.constants.Constants;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.db.KinopoiskDatabase;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Оптимизированный клиент для работы с Kinopoisk API в виде Singleton с поддержкой кэширования Room.
 */
public class KinopoiskApiClient {

    private static final String BASE_URL = "https://kinopoiskapiunofficial.tech/api/";
    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final long CACHE_DURATION_MS = TimeUnit.DAYS.toMillis(1);
    private static final String GENRES_COUNTRIES_ID = "genres_countries_data";

    private static volatile KinopoiskApiClient instance;

    private final OkHttpClient httpClient;
    private final Gson gson;
    private final String apiKey;
    private final KinopoiskDatabase database;
    private final Executor executor = Executors.newSingleThreadExecutor();

    private static String getInternalApiKey() {
        String reversedKey = "0449601bf902-7adb-7f64-46f9-a6faa029";
        return new StringBuilder(reversedKey).reverse().toString();
    }

    private KinopoiskApiClient(String apiKey, OkHttpClient httpClient, Context context) {
        this.apiKey = apiKey;
        this.gson = new GsonBuilder().setLenient().create();
        this.httpClient = httpClient;
        this.database = KinopoiskDatabase.getDatabase(context);
    }

    /**
     * Инициализирует Singleton с внутренним API ключом.
     * Этот метод должен быть вызван один раз в классе Application.
     * @param context Контекст приложения для инициализации базы данных.
     */
    public static void initialize(Context context) {
        if (instance == null) {
            synchronized (KinopoiskApiClient.class) {
                if (instance == null) {
                    instance = new KinopoiskApiClient(getInternalApiKey(), new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .build(), context);
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
            throw new IllegalStateException("KinopoiskApiClient must be initialized first.");
        }
        return instance;
    }

    /**
     * Интерфейс для асинхронной обработки ответов API.
     * @param <T> Тип ожидаемого результата.
     */
    public interface ApiCallback<T> {
        /**
         * Вызывается при успешном получении и парсинге ответа.
         * @param result Результат запроса.
         */
        void onSuccess(T result);

        /**
         * Вызывается при возникновении ошибки сети или парсинга.
         * @param error Ошибка, содержащая код и сообщение.
         */
        void onError(ApiException error);
    }

    /**
     * Кастомное исключение для ошибок, связанных с API.
     */
    public static class ApiException extends Exception {
        private final int code;
        public ApiException(int code, String message) {
            super(message);
            this.code = code;
        }
        public int getCode() { return code; }
    }

    /**
     * Получает коллекцию фильмов (например, TOP_250) с поддержкой кэширования.
     * @param type Тип запрашиваемой коллекции.
     * @param page Номер страницы для пагинации.
     * @param forceRefresh Если true, данные будут принудительно загружены из сети, игнорируя кэш.
     * @param callback Колбэк для обработки результата.
     */
    public void getFilmCollection(Constants.FilmCollectionType type, int page, boolean forceRefresh, ApiCallback<FilmCollection> callback) {
        String collectionId = type.getTypeName() + "_" + page;
        executor.execute(() -> {
            if (!forceRefresh) {
                FilmCollection cached = database.filmCollectionDao().getById(collectionId);
                if (cached != null && (System.currentTimeMillis() - cached.getLastUpdated()) < CACHE_DURATION_MS) {
                    callback.onSuccess(cached);
                    return;
                }
            }
            String url = BASE_URL + "v2.2/films/collections?type=" + type.getTypeName() + "&page=" + page;
            makeRequest(url, FilmCollection.class, new ApiCallback<FilmCollection>() {
                @Override
                public void onSuccess(FilmCollection result) {
                    if (result != null) {
                        executor.execute(() -> {
                            result.setId(collectionId);
                            result.setTitle(type.getTypeName());
                            result.setLastUpdated(System.currentTimeMillis());
                            database.filmCollectionDao().insert(result);
                            callback.onSuccess(result);
                        });
                    } else {
                        callback.onSuccess(null);
                    }
                }

                @Override
                public void onError(ApiException error) {
                    executor.execute(() -> {
                        FilmCollection cached = database.filmCollectionDao().getById(collectionId);
                        if (cached != null) {
                            callback.onSuccess(cached);
                        } else {
                            callback.onError(error);
                        }
                    });
                }
            });
        });
    }

    /**
     * Получает списки всех жанров и стран. Данные кэшируются и загружаются из сети только один раз.
     * @param callback Колбэк для обработки результата.
     */
    public void getGenreOrCountryList(ApiCallback<FilmCountryOrGenresResponse> callback) {
        executor.execute(() -> {
            FilmCountryOrGenresResponse cached = database.filmCountryOrGenresResponseDao().getResponse(GENRES_COUNTRIES_ID);
            if (cached != null) {
                callback.onSuccess(cached);
                return;
            }
            String url = BASE_URL + "v2.2/films/filters";
            makeRequest(url, FilmCountryOrGenresResponse.class, new ApiCallback<FilmCountryOrGenresResponse>() {
                @Override
                public void onSuccess(FilmCountryOrGenresResponse result) {
                    if (result != null) {
                        executor.execute(() -> {
                            database.filmCountryOrGenresResponseDao().insert(result);
                            callback.onSuccess(result);
                        });
                    } else {
                        callback.onSuccess(null);
                    }
                }

                @Override
                public void onError(ApiException error) {
                    callback.onError(error);
                }
            });
        });
    }

    /**
     * Получает детальную информацию о фильме по его ID с поддержкой кэширования.
     * @param kinopoiskId ID фильма на Кинопоиске.
     * @param forceRefresh Если true, данные будут принудительно загружены из сети, игнорируя кэш.
     * @param callback Колбэк для обработки результата.
     */
    public void getFilmDetails(int kinopoiskId, boolean forceRefresh, ApiCallback<FilmDetails> callback) {
        executor.execute(() -> {
            if (!forceRefresh) {
                FilmDetails cached = database.filmDetailsDao().getById(kinopoiskId);
                if (cached != null && (System.currentTimeMillis() - cached.getLastUpdated()) < CACHE_DURATION_MS) {
                    callback.onSuccess(cached);
                    return;
                }
            }
            String url = BASE_URL + "v2.2/films/" + kinopoiskId;
            makeRequest(url, FilmDetails.class, new ApiCallback<FilmDetails>() {
                @Override
                public void onSuccess(FilmDetails result) {
                    if (result != null) {
                        executor.execute(() -> {
                            result.setLastUpdated(System.currentTimeMillis());
                            database.filmDetailsDao().insert(result);
                            callback.onSuccess(result);
                        });
                    } else {
                        callback.onSuccess(null);
                    }
                }

                @Override
                public void onError(ApiException error) {
                    executor.execute(() -> {
                        FilmDetails cached = database.filmDetailsDao().getById(kinopoiskId);
                        if (cached != null) {
                            callback.onSuccess(cached);
                        } else {
                            callback.onError(error);
                        }
                    });
                }
            });
        });
    }

    /**
     * Получает список похожих фильмов для указанного фильма с поддержкой кэширования.
     *
     * @param kinopoiskId  ID фильма на Кинопоиске.
     * @param forceRefresh Если true, данные будут принудительно загружены из сети, игнорируя кэш.
     * @param callback     Колбэк для обработки результата.
     */
    public void getFilmSimilar(int kinopoiskId, boolean forceRefresh, ApiCallback<FilmSimilarResponse> callback) {
        String similarId = "similar_" + kinopoiskId;
        executor.execute(() -> {
            if (!forceRefresh) {
                FilmSimilarResponse cached = database.filmSimilarResponseDao().getById(similarId);
                if (cached != null && (System.currentTimeMillis() - cached.getLastUpdated()) < CACHE_DURATION_MS) {
                    callback.onSuccess(cached);
                    return;
                }
            }

            String url = BASE_URL + "v2.2/films/" + kinopoiskId + "/similars";
            makeRequest(url, FilmSimilarResponse.class, new ApiCallback<FilmSimilarResponse>() {
                @Override
                public void onSuccess(FilmSimilarResponse result) {
                    if (result != null) {
                        executor.execute(() -> {
                            result.setId(similarId);
                            result.setLastUpdated(System.currentTimeMillis());
                            database.filmSimilarResponseDao().insert(result);
                            callback.onSuccess(result);
                        });
                    } else {
                        callback.onSuccess(null);
                    }
                }

                @Override
                public void onError(ApiException error) {
                    executor.execute(() -> {
                        FilmSimilarResponse cached = database.filmSimilarResponseDao().getById(similarId);
                        if (cached != null) {
                            callback.onSuccess(cached);
                        } else {
                            callback.onError(error);
                        }
                    });
                }
            });
        });
    }

    /**
     * Получает изображения для фильма с поддержкой кэширования.
     *
     * @param kinopoiskId  ID фильма на Кинопоиске.
     * @param type         Тип изображений.
     * @param page         Номер страницы для пагинации.
     * @param forceRefresh Если true, данные будут принудительно загружены из сети, игнорируя кэш.
     * @param callback     Колбэк для обработки результата.
     */
    public void getFilmImages(int kinopoiskId, Constants.ImageType type, int page, boolean forceRefresh, ApiCallback<FilmImagesResponse> callback) {
        String imageId = "images_" + kinopoiskId + "_" + type.name() + "_" + page;
        executor.execute(() -> {
            if (!forceRefresh) {
                FilmImagesResponse cached = database.filmImagesResponseDao().getById(imageId);
                if (cached != null && (System.currentTimeMillis() - cached.getLastUpdated()) < CACHE_DURATION_MS) {
                    callback.onSuccess(cached);
                    return;
                }
            }

            String url = BASE_URL + "v2.2/films/" + kinopoiskId + "/images?type=" + type.name() + "&page=" + page;
            makeRequest(url, FilmImagesResponse.class, new ApiCallback<FilmImagesResponse>() {
                @Override
                public void onSuccess(FilmImagesResponse result) {
                    if (result != null) {
                        executor.execute(() -> {
                            result.setId(imageId);
                            result.setLastUpdated(System.currentTimeMillis());
                            database.filmImagesResponseDao().insert(result);
                            callback.onSuccess(result);
                        });
                    } else {
                        callback.onSuccess(null);
                    }
                }

                @Override
                public void onError(ApiException error) {
                    executor.execute(() -> {
                        FilmImagesResponse cached = database.filmImagesResponseDao().getById(imageId);
                        if (cached != null) {
                            callback.onSuccess(cached);
                        } else {
                            callback.onError(error);
                        }
                    });
                }
            });
        });
    }

    /**
     * Получает список сиквелов и приквелов для указанного фильма с поддержкой кэширования.
     *
     * @param kinopoiskId  ID фильма на Кинопоиске.
     * @param forceRefresh Если true, данные будут принудительно загружены из сети, игнорируя кэш.
     * @param callback     Колбэк для обработки результата.
     */
    public void getFilmSequelsAndPrequels(int kinopoiskId, boolean forceRefresh, ApiCallback<List<FilmSequelsAndPrequelsResponse.FilmSequelOrPrequel>> callback) {
        String sequelsId = "sequels_" + kinopoiskId;
        executor.execute(() -> {
            if (!forceRefresh) {
                FilmSequelsAndPrequelsResponse cached = database.filmSequelsAndPrequelsResponseDao().getById(sequelsId);
                if (cached != null && (System.currentTimeMillis() - cached.getLastUpdated()) < CACHE_DURATION_MS) {
                    callback.onSuccess(cached.getItems());
                    return;
                }
            }

            String url = BASE_URL + "v2.1/films/" + kinopoiskId + "/sequels_and_prequels";
            makeRequest(url, new TypeToken<List<FilmSequelsAndPrequelsResponse.FilmSequelOrPrequel>>() {}.getType(), new ApiCallback<List<FilmSequelsAndPrequelsResponse.FilmSequelOrPrequel>>() {
                @Override
                public void onSuccess(List<FilmSequelsAndPrequelsResponse.FilmSequelOrPrequel> result) {
                    if (result != null) {
                        executor.execute(() -> {
                            FilmSequelsAndPrequelsResponse response = new FilmSequelsAndPrequelsResponse();
                            response.setId(sequelsId);
                            response.setItems(result);
                            response.setLastUpdated(System.currentTimeMillis());
                            database.filmSequelsAndPrequelsResponseDao().insert(response);
                            callback.onSuccess(result);
                        });
                    } else {
                        callback.onSuccess(null);
                    }
                }

                @Override
                public void onError(ApiException error) {
                    executor.execute(() -> {
                        FilmSequelsAndPrequelsResponse cached = database.filmSequelsAndPrequelsResponseDao().getById(sequelsId);
                        if (cached != null) {
                            callback.onSuccess(cached.getItems());
                        } else {
                            callback.onError(error);
                        }
                    });
                }
            });
        });
    }

    /**
     * Выполняет поиск фильмов по ключевому слову с поддержкой кэширования.
     *
     * @param keyword      Ключевое слово для поиска.
     * @param page         Номер страницы для пагинации.
     * @param forceRefresh Если true, данные будут принудительно загружены из сети, игнорируя кэш.
     * @param callback     Колбэк для обработки результата.
     */
    public void searchFilmByKeyword(String keyword, int page, boolean forceRefresh, ApiCallback<FilmSearchResponse> callback) {
        String searchId = keyword + "_" + page;
        executor.execute(() -> {
            if (!forceRefresh) {
                FilmSearchResponse cached = database.filmSearchResponseDao().getById(searchId);
                if (cached != null && (System.currentTimeMillis() - cached.getLastUpdated()) < CACHE_DURATION_MS) {
                    callback.onSuccess(cached);
                    return;
                }
            }

            String url = BASE_URL + "v2.1/films/search-by-keyword?keyword=" + keyword + "&page=" + page;
            makeRequest(url, FilmSearchResponse.class, new ApiCallback<FilmSearchResponse>() {
                @Override
                public void onSuccess(FilmSearchResponse result) {
                    if (result != null) {
                        executor.execute(() -> {
                            result.setId(searchId);
                            result.setLastUpdated(System.currentTimeMillis());
                            database.filmSearchResponseDao().insert(result);
                            callback.onSuccess(result);
                        });
                    } else {
                        callback.onSuccess(null);
                    }
                }

                @Override
                public void onError(ApiException error) {
                    executor.execute(() -> {
                        FilmSearchResponse cached = database.filmSearchResponseDao().getById(searchId);
                        if (cached != null) {
                            callback.onSuccess(cached);
                        } else {
                            callback.onError(error);
                        }
                    });
                }
            });
        });
    }






    /**
     * Универсальный метод для выполнения HTTP запросов.
     * @param url URL для запроса.
     * @param responseType Тип ожидаемого ответа.
     * @param callback Колбэк для обработки результата.
     * @param <T> Тип результата.
     */
    private <T> void makeRequest(String url, Type responseType, ApiCallback<T> callback) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader(API_KEY_HEADER, this.apiKey)
                .addHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(new ApiException(0, "Network error: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        callback.onError(new ApiException(response.code(), "HTTP error: " + response.code()));
                        return;
                    }
                    String body = responseBody.string();
                    if (body == null || body.trim().isEmpty()) {
                        callback.onError(new ApiException(response.code(), "Empty response from server"));
                        return;
                    }
                    T result = gson.fromJson(body, responseType);
                    callback.onSuccess(result);
                } catch (Exception e) {
                    callback.onError(new ApiException(0, "Parsing error: " + e.getMessage()));
                }
            }
        });
    }


    private <T> void makeRequest(String url, Class<T> responseClass, ApiCallback<T> callback) {
        makeRequest(url, (Type) responseClass, callback);
    }

    /**
     * Освобождает ресурсы, используемые OkHttpClient.
     * Рекомендуется вызывать при завершении работы приложения.
     */
    public void close() {
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
    }

}
