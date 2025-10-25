package com.alaka_ala.florafilm;

import android.app.Application;

import com.alaka_ala.florafilm.data.database.AppDatabase;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.KinopoiskApiClient;

public class App extends Application {

    private static AppDatabase appDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        KinopoiskApiClient.initialize();
        appDatabase = AppDatabase.getDatabase(this);
    }

    public static AppDatabase getAppDatabase() {
        return appDatabase;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        KinopoiskApiClient.getInstance().close();
    }
}
