package com.alaka_ala.unofficial_kinopoisk_api.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * DAO для работы с кэшированными ответами поиска фильмов.
 */
@Dao
public interface FilmSearchResponseDao {

    /**
     * Вставляет или обновляет ответ поиска фильмов в базе данных.
     * @param response Ответ для кэширования.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(com.alaka_ala.unofficial_kinopoisk_api.models.FilmSearchResponse response);

    /**
     * Получает кэшированный ответ по ID.
     * @param id Уникальный идентификатор ответа.
     * @return Кэшированный объект FilmSearchResponse или null, если не найден.
     */
    @Query("SELECT * FROM film_search_response WHERE id = :id")
    com.alaka_ala.unofficial_kinopoisk_api.models.FilmSearchResponse getById(String id);
}
