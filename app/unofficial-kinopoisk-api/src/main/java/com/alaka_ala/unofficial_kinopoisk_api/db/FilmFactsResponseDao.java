package com.alaka_ala.unofficial_kinopoisk_api.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface FilmFactsResponseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(com.alaka_ala.unofficial_kinopoisk_api.models.FilmFactsResponse filmFactsResponse);

    @Query("SELECT * FROM film_facts_response WHERE id = :id")
    com.alaka_ala.unofficial_kinopoisk_api.models.FilmFactsResponse getById(String id);
}
