package com.alaka_ala.florafilm;

import android.app.Application;

import com.alaka_ala.florafilm.ui.utils.kinopoisk.KinopoiskApiClient;

/**
 * Основной класс приложения для инициализации глобальных компонентов.
 */
public class App extends Application {

    /**
     * Вызывается при создании приложения.
     * Идеальное место для инициализации Singleton'ов.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // Инициализируем API клиент с внутренним ключом
        KinopoiskApiClient.initialize();
    }

    /**
     * Вызывается при завершении работы приложения.
     * Не гарантируется вызов на реальных устройствах, но является хорошей практикой.
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
        // Освобождаем ресурсы API клиента
        KinopoiskApiClient.getInstance().close();
    }
}
