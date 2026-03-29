package com.alaka_ala.unofficial_kinopoisk_api.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.alaka_ala.unofficial_kinopoisk_api.models.FilmCollection;

/**
 * Data Access Object for the FilmCollection class.
 */
@Dao
public interface FilmCollectionDao {

    /**
     * Inserts a film collection into the database. If the collection already exists,
     * it will be replaced.
     * @param filmCollection The film collection to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FilmCollection filmCollection);

    /**
     * Retrieves a film collection by its ID.
     * @param id The ID of the film collection to retrieve.
     * @return The film collection with the specified ID, or null if not found.
     */
    @Query("SELECT * FROM film_collection WHERE id = :id")
    FilmCollection getById(String id);

    /**
     * Deletes a film collection from the database.
     */
    @Query("DELETE FROM film_collection WHERE id = :id")
    void delete(String id);
}
