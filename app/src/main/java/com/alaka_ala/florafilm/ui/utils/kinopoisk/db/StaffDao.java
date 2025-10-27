
package com.alaka_ala.florafilm.ui.utils.kinopoisk.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.Staff;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.StaffResponse;

import java.util.List;

@Dao
public interface StaffDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StaffResponse staffResponse);

    @Query("SELECT * FROM staff_response WHERE id = :id")
    StaffResponse getById(String id);
}
