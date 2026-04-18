package com.alaka_ala.unofficial_kinopoisk_api.models;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Модель ответа для поиска фильмов по ключевому слову, адаптированная для Room.
 */
@Keep
@Entity(tableName = "film_search_response")
@TypeConverters(Converters.class)
public class FilmSearchResponse {

    /**
     * Уникальный идентификатор для записи в базе данных.
     * Формируется как "{keyword}_{page}".
     */
    @PrimaryKey
    @NonNull
    private String id;

    @SerializedName("keyword")
    private String keyword;

    @SerializedName("pagesCount")
    private int pagesCount;

    @SerializedName("searchFilmsCountResult")
    private int searchFilmsCountResult;

    @SerializedName("films")
    private List<SearchResultFilm> films;

    private long lastUpdated;

    // Getters and Setters

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getPagesCount() {
        return pagesCount;
    }

    public void setPagesCount(int pagesCount) {
        this.pagesCount = pagesCount;
    }

    public int getSearchFilmsCountResult() {
        return searchFilmsCountResult;
    }

    public void setSearchFilmsCountResult(int searchFilmsCountResult) {
        this.searchFilmsCountResult = searchFilmsCountResult;
    }

    public List<SearchResultFilm> getFilms() {
        return films;
    }

    public void setFilms(List<SearchResultFilm> films) {
        this.films = films;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
