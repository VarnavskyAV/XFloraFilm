package com.alaka_ala.unofficial_kinopoisk_api.models;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Модель результата поиска фильма.
 * Извлечен из FilmSearchResponse для лучшей структуры.
 */
@Keep
public class SearchResultFilm {
    @SerializedName("filmId")
    private int filmId;
    @SerializedName("nameRu")
    private String nameRu;
    @SerializedName("nameEn")
    private String nameEn;
    @SerializedName("type")
    private String type;
    @SerializedName("year")
    private String year;
    @SerializedName("description")
    private String description;
    @SerializedName("filmLength")
    private String filmLength;
    @SerializedName("countries")
    private List<SearchCountry> countries;
    @SerializedName("genres")
    private List<SearchGenre> genres;
    @SerializedName("rating")
    private String rating;
    @SerializedName("ratingVoteCount")
    private int ratingVoteCount;
    @SerializedName("posterUrl")
    private String posterUrl;
    @SerializedName("posterUrlPreview")
    private String posterUrlPreview;

    // Getters and Setters
    public int getFilmId() {
        return filmId;
    }

    public void setFilmId(int filmId) {
        this.filmId = filmId;
    }

    public String getNameRu() {
        return nameRu;
    }

    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilmLength() {
        return filmLength;
    }

    public void setFilmLength(String filmLength) {
        this.filmLength = filmLength;
    }

    public List<SearchCountry> getCountries() {
        return countries;
    }

    public void setCountries(List<SearchCountry> countries) {
        this.countries = countries;
    }

    public List<SearchGenre> getGenres() {
        return genres;
    }

    public void setGenres(List<SearchGenre> genres) {
        this.genres = genres;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public int getRatingVoteCount() {
        return ratingVoteCount;
    }

    public void setRatingVoteCount(int ratingVoteCount) {
        this.ratingVoteCount = ratingVoteCount;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getPosterUrlPreview() {
        return posterUrlPreview;
    }

    public void setPosterUrlPreview(String posterUrlPreview) {
        this.posterUrlPreview = posterUrlPreview;
    }
}
