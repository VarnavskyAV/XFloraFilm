package com.alaka_ala.unofficial_kinopoisk_api.models;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

/**
 * Модель жанра в результатах поиска.
 * Извлечен из FilmSearchResponse для лучшей структуры.
 */
@Keep
public class SearchGenre {
    @SerializedName("genre")
    private String genre;

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
