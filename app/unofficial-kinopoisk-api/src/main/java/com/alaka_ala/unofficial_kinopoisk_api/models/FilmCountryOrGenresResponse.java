package com.alaka_ala.unofficial_kinopoisk_api.models;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Сущность для кэширования списков жанров и стран.
 */
@Keep
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
}
