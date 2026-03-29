package com.alaka_ala.unofficial_kinopoisk_api.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * DAO для работы с кэшированными ответами изображений фильмов.
 */
@Dao
public interface FilmImagesResponseDao {

    /**
     * Вставляет или обновляет ответ с изображениями фильма в базе данных.
     * @param response Ответ для кэширования.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(com.alaka_ala.unofficial_kinopoisk_api.models.FilmImagesResponse response);

    /**
     * Получает кэшированный ответ по ID.
     * @param id Уникальный идентификатор ответа.
     * @return Кэшированный объект FilmImagesResponse или null, если не найден.
     */
    @Query("SELECT * FROM film_images_response WHERE id = :id")
    com.alaka_ala.unofficial_kinopoisk_api.models.FilmImagesResponse getById(String id);
}
