package com.alaka_ala.unofficial_kinopoisk_api.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * DAO для работы с кэшированными ответами сиквелов и приквелов.
 */
@Dao
public interface FilmSequelsAndPrequelsResponseDao {

    /**
     * Вставляет или обновляет ответ с сиквелами и приквелами в базе данных.
     * @param response Ответ для кэширования.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(com.alaka_ala.unofficial_kinopoisk_api.models.FilmSequelsAndPrequelsResponse response);

    /**
     * Получает кэшированный ответ по ID.
     * @param id Уникальный идентификатор ответа.
     * @return Кэшированный объект FilmSequelsAndPrequelsResponse или null, если не найден.
     */
    @Query("SELECT * FROM film_sequels_and_prequels_response WHERE id = :id")
    com.alaka_ala.unofficial_kinopoisk_api.models.FilmSequelsAndPrequelsResponse getById(String id);
}
