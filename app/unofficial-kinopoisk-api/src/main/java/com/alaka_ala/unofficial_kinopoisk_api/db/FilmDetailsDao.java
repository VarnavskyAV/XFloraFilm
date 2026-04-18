package com.alaka_ala.unofficial_kinopoisk_api.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.TypeConverters;

import com.alaka_ala.unofficial_kinopoisk_api.models.FilmDetails;

import java.util.List;
import java.util.Map;

/**
 * Data Access Object for the FilmDetails class.
 */
@Dao
@TypeConverters(Converters.class) // Указываем, что DAO использует конвертеры
public interface FilmDetailsDao {

    /**
     * Вставляет объект FilmDetails. Если фильм уже существует, он будет заменен.
     * Используйте insertAndPreservePositions для безопасного обновления.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FilmDetails filmDetails);

    /**
     * Вставляет новый FilmDetails, но сохраняет карту позиций просмотра от старой записи.
     * @param filmDetails Новый объект с данными о фильме.
     */
    @Transaction
    default void insertAndPreservePositions(FilmDetails filmDetails) {
        com.alaka_ala.unofficial_kinopoisk_api.models.FilmDetails oldDetails = getById(filmDetails.getKinopoiskId());
        if (oldDetails != null) {
            filmDetails.setLastPositionPlayerView(oldDetails.getLastPositionPlayerView());
        }
        insert(filmDetails);
    }

    /**
     * Эффективно обновляет только поле с позициями просмотра для указанного фильма.
     * Этот метод предназначен для использования плеером.
     * @param kinopoiskId ID фильма.
     * @param positions Новая карта позиций.
     */
    @Query("UPDATE film_details SET lastPositionPlayerView = :positions WHERE kinopoiskId = :kinopoiskId")
    void updatePositions(int kinopoiskId, Map<String, Long> positions);

    /**Возвращает фильм по его ID*/
    @Query("SELECT * FROM film_details WHERE kinopoiskId = :kinopoiskId")
    FilmDetails getById(int kinopoiskId);

    /**Возвращает только список фильмов которые были добавлены в историю просмотра */
    @Query("SELECT * FROM film_details WHERE isView = 1 ORDER BY timestampAddedHistory DESC")
    LiveData<List<FilmDetails>> getByView();

    /**Обновляет время просмотра фильма (Этот метод автоматически обновляет время просмотра при загрузке данных)*/
    @Query("UPDATE film_details SET timestampAddedHistory = :ts WHERE kinopoiskId = :kinopoiskId")
    void updateTimeStampView(long ts, int kinopoiskId);

    /**Возвращает только список фильмов которые ранее был начат просмотр */
    @Query("SELECT * FROM film_details WHERE isStartView = 1")
    LiveData<List<FilmDetails>> getByIsStartView();
    /**Возвращает только список фильмов которые были добавлены в закладки */
    @Query("SELECT * FROM film_details WHERE isBookmark = 1")
    LiveData<List<FilmDetails>> getByBookmark();
    /**Возвращает только список фильмов которые были добавлены в список отслеживаемых озвучек */
    @Query("SELECT * FROM film_details WHERE observeUpdateVoice = 1")
    LiveData<List<FilmDetails>> getFilmByObserveVoice();

    @Query("DELETE FROM film_details WHERE kinopoiskId = :kinopoiskId")
    void removeByKinopoiskId(int kinopoiskId);

    @Query("UPDATE film_details SET isView = 0 WHERE isView = 1")
    void clearHistory();

    @Query("UPDATE film_details SET isBookmark = 0 WHERE isBookmark = 1")
    void clearBookmarks();

    @Query("UPDATE film_details SET isStartView = 0 WHERE isStartView = 1")
    void clearResume();

    @Query("DELETE FROM film_details WHERE isView = 0 AND isBookmark = 0 AND isStartView = 0")
    void deleteOrphans();

    @Transaction
    default void removeByHistory() {
        clearHistory();
        deleteOrphans();
    }

    @Transaction
    default void removeByBookmark() {
        clearBookmarks();
        deleteOrphans();
    }

    @Transaction
    default void removeByResume() {
        clearResume();
        deleteOrphans();
    }

    /**Удаляет закладки и историю просмотра*/
    @Query("DELETE FROM film_details")
    void removeAll();
}
