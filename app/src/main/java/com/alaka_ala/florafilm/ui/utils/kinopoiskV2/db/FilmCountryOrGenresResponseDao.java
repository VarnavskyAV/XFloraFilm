package com.alaka_ala.florafilm.ui.utils.kinopoiskV2.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models.FilmCountryOrGenresResponse;

/**
 * Data Access Object for the FilmCountryOrGenresResponse class.
 */
@Dao
public interface FilmCountryOrGenresResponseDao {

    /**
     * Inserts the genres/countries response into the database. If it already exists,
     * it will be replaced.
     * @param response The response to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FilmCountryOrGenresResponse response);

    /**
     * Retrieves the genres/countries response from the database.
     * @param id The static ID of the response.
     * @return The FilmCountryOrGenresResponse object, or null if not found.
     */
    @Query("SELECT * FROM genres_countries_cache WHERE id = :id")
    FilmCountryOrGenresResponse getResponse(String id);
}
