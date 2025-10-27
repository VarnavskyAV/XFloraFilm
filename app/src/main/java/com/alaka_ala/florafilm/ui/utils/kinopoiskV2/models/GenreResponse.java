package com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models;

import com.google.gson.annotations.SerializedName;

/**
 * Вложенный класс для хранения данных о жанре.
 * Извлечен из FilmCountryOrGenresResponse для лучшей структуры.
 */
public class GenreResponse {
    @SerializedName("id")
    private int id;
    @SerializedName("genre")
    private String genre;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
