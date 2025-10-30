package com.alaka_ala.florafilm.ui.utils.kinopoiskV2.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models.FilmDetails;

import java.util.List;

/**
 * Data Access Object for the FilmDetails class.
 */
@Dao
public interface FilmDetailsDao {

    /**
     * Inserts a film details object into the database. If the film already exists,
     * it will be replaced.
     * @param filmDetails The film details to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FilmDetails filmDetails);

    /**
     * Retrieves a film details object by its Kinopoisk ID.
     * @param kinopoiskId The ID of the film to retrieve.
     * @return The FilmDetails object with the specified ID, or null if not found.
     */
    @Query("SELECT * FROM film_details WHERE kinopoiskId = :kinopoiskId")
    FilmDetails getById(int kinopoiskId);

    @Query("SELECT * FROM film_details WHERE isView = :isView")
    List<FilmDetails> getByView(boolean isView);

    @Query("SELECT * FROM film_details WHERE isStartView = :isStartView")
    List<FilmDetails> getByIsStartView(boolean isStartView);

}
