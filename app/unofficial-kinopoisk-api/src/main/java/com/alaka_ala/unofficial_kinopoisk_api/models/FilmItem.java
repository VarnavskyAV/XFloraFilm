package com.alaka_ala.unofficial_kinopoisk_api.models;

import android.annotation.SuppressLint;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;

/**
 * Оптимизированная модель фильма для списков
 * Содержит только необходимые поля для отображения в списках
 */
@Keep
public class FilmItem extends BaseModel {
    
    @SerializedName("kinopoiskId")
    private Integer kinopoiskId;
    
    @SerializedName("imdbId")
    private String imdbId;
    
    @SerializedName("nameRu")
    private String nameRu;
    
    @SerializedName("nameEn")
    private String nameEn;
    
    @SerializedName("nameOriginal")
    private String nameOriginal;
    
    @SerializedName("countries")
    private List<Country> countries;
    
    @SerializedName("genres")
    private List<Genre> genres;
    
    @SerializedName("ratingKinopoisk")
    private Double ratingKinopoisk;
    
    @SerializedName("ratingImdb")
    private String ratingImdb;
    
    @SerializedName("year")
    private Integer year;
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("posterUrl")
    private String posterUrl;
    
    @SerializedName("posterUrlPreview")
    private String posterUrlPreview;
    
    //@SerializedName("coverUrl")
    private String coverUrl;
    
    //@SerializedName("logoUrl")
    private String logoUrl;
    
    //@SerializedName("description")
    private String description;
    
    //@SerializedName("ratingAgeLimits")
    private String ratingAgeLimits;
    
    // Конструктор по умолчанию для Gson
    public FilmItem() {
        this.countries = new ArrayList<>();
        this.genres = new ArrayList<>();
    }
    
    // Getters
    public int getKinopoiskId() {
        return safeInt(kinopoiskId);
    }
    
    public String getImdbId() {
        return safeString(imdbId);
    }
    
    public String getNameRu() {
        return safeString(nameRu);
    }
    
    public String getNameEn() {
        return safeString(nameEn);
    }
    
    public String getNameOriginal() {
        return safeString(nameOriginal);
    }
    
    public List<Country> getCountries() {
        return countries != null ? countries : new ArrayList<>();
    }
    
    public List<Genre> getGenres() {
        return genres != null ? genres : new ArrayList<>();
    }
    
    public double getRatingKinopoisk() {
        return safeDouble(ratingKinopoisk);
    }
    
    public String getRatingImdb() {
        return safeString(ratingImdb);
    }
    
    public int getYear() {
        return safeInt(year);
    }
    
    public String getType() {
        return safeString(type);
    }
    
    public String getPosterUrl() {
        return safeString(posterUrl);
    }
    
    public String getPosterUrlPreview() {
        return safeString(posterUrlPreview);
    }
    
    public String getCoverUrl() {
        return safeString(coverUrl);
    }
    
    public String getLogoUrl() {
        return safeString(logoUrl);
    }
    
    public String getDescription() {
        return safeString(description);
    }
    
    public String getRatingAgeLimits() {
        return safeString(ratingAgeLimits);
    }
    
    // Setters
    public void setKinopoiskId(Integer kinopoiskId) {
        this.kinopoiskId = kinopoiskId;
    }
    
    public void setImdbId(String imdbId) {
        this.imdbId = safeString(imdbId);
    }
    
    public void setNameRu(String nameRu) {
        this.nameRu = safeString(nameRu);
    }
    
    public void setNameEn(String nameEn) {
        this.nameEn = safeString(nameEn);
    }
    
    public void setNameOriginal(String nameOriginal) {
        this.nameOriginal = safeString(nameOriginal);
    }
    
    public void setCountries(List<Country> countries) {
        this.countries = countries != null ? countries : new ArrayList<>();
    }
    
    public void setGenres(List<Genre> genres) {
        this.genres = genres != null ? genres : new ArrayList<>();
    }
    
    public void setRatingKinopoisk(Double ratingKinopoisk) {
        this.ratingKinopoisk = ratingKinopoisk;
    }
    
    public void setRatingImdb(String ratingImdb) {
        this.ratingImdb = safeString(ratingImdb);
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public void setType(String type) {
        this.type = safeString(type);
    }
    
    public void setPosterUrl(String posterUrl) {
        this.posterUrl = safeString(posterUrl);
    }
    
    public void setPosterUrlPreview(String posterUrlPreview) {
        this.posterUrlPreview = safeString(posterUrlPreview);
    }
    
    public void setCoverUrl(String coverUrl) {
        this.coverUrl = safeString(coverUrl);
    }
    
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = safeString(logoUrl);
    }
    
    public void setDescription(String description) {
        this.description = safeString(description);
    }
    
    public void setRatingAgeLimits(String ratingAgeLimits) {
        this.ratingAgeLimits = safeString(ratingAgeLimits);
    }
    
    /**
     * Возвращает лучшее доступное название фильма
     */
    public String getBestName() {
        if (!isEmpty(nameRu)) return nameRu;
        if (!isEmpty(nameEn)) return nameEn;
        if (!isEmpty(nameOriginal)) return nameOriginal;
        return "Без названия";
    }
    
    /**
     * Возвращает лучший доступный постер
     */
    public String getBestPoster() {
        if (!isEmpty(posterUrl)) return posterUrl;
        if (!isEmpty(posterUrlPreview)) return posterUrlPreview;
        return "";
    }
    
    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("FilmItem{id=%d, name='%s', year=%d, rating=%.1f}", 
                           getKinopoiskId(), getBestName(), getYear(), getRatingKinopoisk());
    }
}
