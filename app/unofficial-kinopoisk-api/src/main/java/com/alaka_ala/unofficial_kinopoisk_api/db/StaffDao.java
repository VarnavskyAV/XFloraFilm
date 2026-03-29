package com.alaka_ala.unofficial_kinopoisk_api.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface StaffDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(com.alaka_ala.unofficial_kinopoisk_api.models.StaffResponse staffResponse);

    @Query("SELECT * FROM staff_response WHERE id = :id")
    com.alaka_ala.unofficial_kinopoisk_api.models.StaffResponse getById(String id);
}
