package com.alaka_ala.unofficial_kinopoisk_api.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.alaka_ala.unofficial_kinopoisk_api.models.FilmAwardsResponse;

@Dao
public interface FilmAwardsResponseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FilmAwardsResponse filmAwardsResponse);

    @Query("SELECT * FROM film_awards_response WHERE id = :id")
    FilmAwardsResponse getById(String id);
}
