package com.alaka_ala.florafilm.ui.fragments.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.alaka_ala.florafilm.App;
import com.alaka_ala.florafilm.data.database.entities.FilmCollectionEntity;
import com.alaka_ala.florafilm.data.repository.FilmRepository;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.KinopoiskApiClient;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final FilmRepository repository;
    private final LiveData<List<FilmCollectionEntity>> filmCollections;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new FilmRepository(App.getAppDatabase(), KinopoiskApiClient.getInstance());
        filmCollections = repository.getFilmCollections();
    }

    public LiveData<List<FilmCollectionEntity>> getFilmCollections() {
        return filmCollections;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.close();
    }
}
