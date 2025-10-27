package com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models;

import com.google.gson.annotations.SerializedName;

/**
 * Вложенный класс для хранения данных о стране.
 * Извлечен из FilmCountryOrGenresResponse для лучшей структуры.
 */
public class CountryResponse {
    @SerializedName("id")
    private int id;
    @SerializedName("country")
    private String country;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
