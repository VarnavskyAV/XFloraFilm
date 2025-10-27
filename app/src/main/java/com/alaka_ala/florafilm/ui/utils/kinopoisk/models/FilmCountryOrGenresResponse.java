package com.alaka_ala.florafilm.ui.utils.kinopoisk.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Сущность для кэширования списков жанров и стран.
 */
@Entity(tableName = "genres_countries_cache")
public class FilmCountryOrGenresResponse {

    @PrimaryKey
    @NonNull
    private String id = "genres_countries_data"; // Статический ID

    @SerializedName("genres")
    private List<GenreResponse> genres;

    @SerializedName("countries")
    private List<CountryResponse> countries;

    private long lastUpdated;

    // Getters
    @NonNull
    public String getId() {
        return id;
    }

    public List<GenreResponse> getGenres() {
        return genres;
    }

    public List<CountryResponse> getCountries() {
        return countries;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    // Setters
    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setGenres(List<GenreResponse> genres) {
        this.genres = genres;
    }

    public void setCountries(List<CountryResponse> countries) {
        this.countries = countries;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * Вложенный класс для хранения данных о жанре.
     * Не является отдельной сущностью.
     */
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

    /**
     * Вложенный класс для хранения данных о стране.
     * Не является отдельной сущностью.
     */
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
