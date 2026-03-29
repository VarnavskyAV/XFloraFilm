package com.alaka_ala.unofficial_kinopoisk_api.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * DAO для работы с кэшированными ответами похожих фильмов.
 */
@Dao
public interface FilmSimilarResponseDao {

    /**
     * Вставляет или обновляет ответ с похожими фильмами в базе данных.
     * @param response Ответ для кэширования.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(com.alaka_ala.unofficial_kinopoisk_api.models.FilmSimilarResponse response);

    /**
     * Получает кэшированный ответ по ID.
     * @param id Уникальный идентификатор ответа.
     * @return Кэшированный объект FilmSimilarResponse или null, если не найден.
     */
    @Query("SELECT * FROM film_similar_response WHERE id = :id")
    com.alaka_ala.unofficial_kinopoisk_api.models.FilmSimilarResponse getById(String id);
}
