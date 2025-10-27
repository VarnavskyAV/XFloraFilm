
package com.alaka_ala.florafilm.ui.utils.kinopoisk.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmFactsResponse;

@Dao
public interface FilmFactsResponseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FilmFactsResponse filmFactsResponse);

    @Query("SELECT * FROM film_facts_response WHERE id = :id")
    FilmFactsResponse getById(String id);
}
