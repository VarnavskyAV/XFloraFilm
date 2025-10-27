package com.alaka_ala.florafilm;

import android.app.Application;

import com.alaka_ala.florafilm.ui.utils.kinopoisk.KinopoiskApiClient;

public class App extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        KinopoiskApiClient.initialize();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        KinopoiskApiClient.getInstance().close();
    }
}
