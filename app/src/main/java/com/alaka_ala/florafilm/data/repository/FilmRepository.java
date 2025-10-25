package com.alaka_ala.florafilm.data.repository;

import androidx.lifecycle.LiveData;

import com.alaka_ala.florafilm.data.database.AppDatabase;
import com.alaka_ala.florafilm.data.database.dao.FilmCollectionDao;
import com.alaka_ala.florafilm.data.database.entities.FilmCollectionEntity;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.KinopoiskApiClient;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.constants.Constants;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmCollection;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FilmRepository {

    private final FilmCollectionDao filmCollectionDao;
    private final KinopoiskApiClient kinopoiskApiClient;
    private final ExecutorService executorService;

    public FilmRepository(AppDatabase appDatabase, KinopoiskApiClient kinopoiskApiClient) {
        filmCollectionDao = appDatabase.filmCollectionDao();
        this.kinopoiskApiClient = kinopoiskApiClient;
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<FilmCollectionEntity>> getFilmCollections() {
        for (Constants.FilmCollectionType type : Constants.FilmCollectionType.values()) {
            executorService.execute(() -> {
                Long lastUpdated = filmCollectionDao.getLastUpdated(type.name());
                if (lastUpdated == null || System.currentTimeMillis() - lastUpdated > 24 * 60 * 60 * 1000) { // 24 hours
                    kinopoiskApiClient.getFilmCollection(type, 1, new KinopoiskApiClient.ApiCallback<FilmCollection>() {
                        @Override
                        public void onSuccess(FilmCollection result) {
                            if (result != null) {
                                FilmCollectionEntity entity = new FilmCollectionEntity(
                                        type.name(),
                                        result.getTitle(),
                                        result.getItems(),
                                        System.currentTimeMillis()
                                );
                                executorService.execute(() -> filmCollectionDao.insert(entity));
                            }
                        }

                        @Override
                        public void onError(KinopoiskApiClient.ApiException error) {
                            // Handle error
                        }
                    });
                }
            });
        }
        return filmCollectionDao.getAll();
    }

    public void close() {
        executorService.shutdown();
    }
}
