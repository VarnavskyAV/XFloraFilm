package com.alaka_ala.florafilm.ui.utils.kinopoisk.models;

import androidx.room.Entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FilmCountryOrGenresResponse {


    public List<GenreResponse> getGenres() {
        return genres;
    }

    public List<CountryResponse> getCountries() {
        return countries;
    }

    @SerializedName("genres")
    private List<GenreResponse> genres;


    @SerializedName("countries")
    private List<CountryResponse> countries;


    public static class GenreResponse {
        @SerializedName("id")
        private int id;
        @SerializedName("genre")
        private String genre;

        public int getId() {
            return id;
        }

        public String getGenre() {
            return genre;
        }
    }


    public static class CountryResponse {
        @SerializedName("id")
        private int id;
        @SerializedName("country")
        private String country;

        public int getId() {
            return id;
        }

        public String getCountry() {
            return country;
        }
    }


}
