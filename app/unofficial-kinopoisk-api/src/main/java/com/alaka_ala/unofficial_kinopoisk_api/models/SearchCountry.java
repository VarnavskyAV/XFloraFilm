package com.alaka_ala.unofficial_kinopoisk_api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Модель страны в результатах поиска.
 * Извлечен из FilmSearchResponse для лучшей структуры.
 */
public class SearchCountry {
    @SerializedName("country")
    private String country;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
