
package com.alaka_ala.florafilm.ui.utils.kinopoisk.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.PersonSearchResponse;

@Dao
public interface PersonSearchResponseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PersonSearchResponse personSearchResponse);

    @Query("SELECT * FROM person_search_response WHERE id = :id")
    PersonSearchResponse getById(String id);
}
