package com.alaka_ala.florafilm.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.alaka_ala.florafilm.data.database.entities.FilmCollectionEntity;

import java.util.List;

@Dao
public interface FilmCollectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FilmCollectionEntity collection);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<FilmCollectionEntity> collections);

    @Query("SELECT * FROM film_collections")
    LiveData<List<FilmCollectionEntity>> getAll();

    @Query("SELECT * FROM film_collections WHERE type = :type")
    LiveData<FilmCollectionEntity> getByType(String type);

    @Query("SELECT lastUpdated FROM film_collections WHERE type = :type")
    Long getLastUpdated(String type);
}
