package com.alaka_ala.unofficial_kinopoisk_api.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.alaka_ala.unofficial_kinopoisk_api.models.WatchPosition;

import java.util.List;

@Dao
public interface WatchPositionDao {

    @Query("SELECT * FROM watch_position WHERE idPosition = :idPosition LIMIT 1")
    WatchPosition getByKey(String idPosition);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(WatchPosition position);

    /** Последняя серия сериала (lastEpisode >= 0). */
    @Query("SELECT * FROM watch_position WHERE kinopoiskId = :kinopoiskId AND lastEpisode >= 0 " +
            "ORDER BY updatedAt DESC LIMIT 1")
    WatchPosition getLastWatchedSerial(int kinopoiskId);

    /** Последняя позиция фильма (lastEpisode < 0). */
    @Query("SELECT * FROM watch_position WHERE kinopoiskId = :kinopoiskId AND lastEpisode < 0 " +
            "ORDER BY updatedAt DESC LIMIT 1")
    WatchPosition getLastWatchedMovie(int kinopoiskId);

    @Query("SELECT * FROM watch_position WHERE kinopoiskId = :kinopoiskId")
    List<WatchPosition> getAllForFilm(int kinopoiskId);

    @Query("DELETE FROM watch_position WHERE kinopoiskId = :kinopoiskId")
    void deleteAllForFilm(int kinopoiskId);
}