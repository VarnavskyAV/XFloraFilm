package com.alaka_ala.florafilm;

import android.app.Application;

import com.alaka_ala.unofficial_kinopoisk_api.api.KinopoiskApiClientV2;

public class App extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        //KinopoiskApiClient.initialize(this);

        KinopoiskApiClientV2.initialize(this);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        KinopoiskApiClientV2.getInstance().close();
    }
}
