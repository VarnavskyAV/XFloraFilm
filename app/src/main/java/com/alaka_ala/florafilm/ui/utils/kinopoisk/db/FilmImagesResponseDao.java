package com.alaka_ala.florafilm.ui.utils.kinopoisk.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmImagesResponse;

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
    void insert(FilmImagesResponse response);

    /**
     * Получает кэшированный ответ по ID.
     * @param id Уникальный идентификатор ответа.
     * @return Кэшированный объект FilmImagesResponse или null, если не найден.
     */
    @Query("SELECT * FROM film_images_response WHERE id = :id")
    FilmImagesResponse getById(String id);
}
