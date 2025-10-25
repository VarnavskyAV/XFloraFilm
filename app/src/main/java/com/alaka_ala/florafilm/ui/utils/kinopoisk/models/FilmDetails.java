package com.alaka_ala.florafilm.ui.utils.kinopoisk.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;

/**
 * Детальная информация о фильме
 * Содержит все поля для полного описания фильма
 */
public class FilmDetails extends BaseModel {
    
    @SerializedName("kinopoiskId")
    private Integer kinopoiskId;
    
    @SerializedName("kinopoiskHDId")
    private String kinopoiskHDId;
    
    @SerializedName("imdbId")
    private String imdbId;
    
    @SerializedName("nameRu")
    private String nameRu;
    
    @SerializedName("nameEn")
    private String nameEn;
    
    @SerializedName("nameOriginal")
    private String nameOriginal;
    
    @SerializedName("posterUrl")
    private String posterUrl;
    
    @SerializedName("posterUrlPreview")
    private String posterUrlPreview;
    
    @SerializedName("coverUrl")
    private String coverUrl;
    
    @SerializedName("logoUrl")
    private String logoUrl;
    
    @SerializedName("reviewsCount")
    private Integer reviewsCount;
    
    @SerializedName("ratingGoodReview")
    private Integer ratingGoodReview;
    
    @SerializedName("ratingGoodReviewVoteCount")
    private Integer ratingGoodReviewVoteCount;
    
    @SerializedName("ratingKinopoisk")
    private Double ratingKinopoisk;
    
    @SerializedName("ratingKinopoiskVoteCount")
    private Integer ratingKinopoiskVoteCount;
    
    @SerializedName("ratingImdb")
    private Double ratingImdb;
    
    @SerializedName("ratingImdbVoteCount")
    private Integer ratingImdbVoteCount;
    
    @SerializedName("ratingFilmCritics")
    private Double ratingFilmCritics;
    
    @SerializedName("ratingFilmCriticsVoteCount")
    private Integer ratingFilmCriticsVoteCount;
    
    @SerializedName("ratingAwait")
    private Integer ratingAwait;
    
    @SerializedName("ratingAwaitCount")
    private Integer ratingAwaitCount;
    
    @SerializedName("ratingRfCritics")
    private Integer ratingRfCritics;
    
    @SerializedName("ratingRfCriticsVoteCount")
    private Integer ratingRfCriticsVoteCount;
    
    @SerializedName("webUrl")
    private String webUrl;
    
    @SerializedName("year")
    private String year;
    
    @SerializedName("filmLength")
    private Integer filmLength;
    
    @SerializedName("slogan")
    private String slogan;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("shortDescription")
    private String shortDescription;
    
    @SerializedName("editorAnnotation")
    private String editorAnnotation;
    
    @SerializedName("isTicketsAvailable")
    private Boolean isTicketsAvailable;
    
    @SerializedName("productionStatus")
    private String productionStatus;
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("ratingMpaa")
    private String ratingMpaa;
    
    @SerializedName("ratingAgeLimits")
    private String ratingAgeLimits;
    
    @SerializedName("countries")
    private List<Country> countries;
    
    @SerializedName("genres")
    private List<Genre> genres;
    
    @SerializedName("startYear")
    private String startYear;
    
    @SerializedName("endYear")
    private String endYear;
    
    @SerializedName("serial")
    private Boolean serial;
    
    @SerializedName("shortFilm")
    private Boolean shortFilm;
    
    @SerializedName("completed")
    private Boolean completed;
    
    @SerializedName("hasImax")
    private Boolean hasImax;
    
    @SerializedName("has3D")
    private Boolean has3D;
    
    @SerializedName("lastSync")
    private Boolean lastSync;
    
    // Конструктор по умолчанию для Gson
    public FilmDetails() {
        this.countries = new ArrayList<>();
        this.genres = new ArrayList<>();
    }
    
    // Getters
    public int getKinopoiskId() {
        return safeInt(kinopoiskId);
    }
    
    public String getKinopoiskHDId() {
        return safeString(kinopoiskHDId);
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
    
    public int getReviewsCount() {
        return safeInt(reviewsCount);
    }
    
    public int getRatingGoodReview() {
        return safeInt(ratingGoodReview);
    }
    
    public int getRatingGoodReviewVoteCount() {
        return safeInt(ratingGoodReviewVoteCount);
    }
    
    public double getRatingKinopoisk() {
        return safeDouble(ratingKinopoisk);
    }
    
    public int getRatingKinopoiskVoteCount() {
        return safeInt(ratingKinopoiskVoteCount);
    }
    
    public double getRatingImdb() {
        return safeDouble(ratingImdb);
    }
    
    public int getRatingImdbVoteCount() {
        return safeInt(ratingImdbVoteCount);
    }
    
    public double getRatingFilmCritics() {
        return safeDouble(ratingFilmCritics);
    }
    
    public int getRatingFilmCriticsVoteCount() {
        return safeInt(ratingFilmCriticsVoteCount);
    }
    
    public int getRatingAwait() {
        return safeInt(ratingAwait);
    }
    
    public int getRatingAwaitCount() {
        return safeInt(ratingAwaitCount);
    }
    
    public int getRatingRfCritics() {
        return safeInt(ratingRfCritics);
    }
    
    public int getRatingRfCriticsVoteCount() {
        return safeInt(ratingRfCriticsVoteCount);
    }
    
    public String getWebUrl() {
        return safeString(webUrl);
    }
    
    public String getYear() {
        return safeString(year);
    }
    
    public int getFilmLength() {
        return safeInt(filmLength);
    }
    
    public String getSlogan() {
        return safeString(slogan);
    }
    
    public String getDescription() {
        return safeString(description);
    }
    
    public String getShortDescription() {
        return safeString(shortDescription);
    }
    
    public String getEditorAnnotation() {
        return safeString(editorAnnotation);
    }
    
    public boolean isTicketsAvailable() {
        return safeBoolean(isTicketsAvailable);
    }
    
    public String getProductionStatus() {
        return safeString(productionStatus);
    }
    
    public String getType() {
        return safeString(type);
    }
    
    public String getRatingMpaa() {
        return safeString(ratingMpaa);
    }
    
    public String getRatingAgeLimits() {
        return safeString(ratingAgeLimits);
    }
    
    public List<Country> getCountries() {
        return countries != null ? countries : new ArrayList<>();
    }
    
    public List<Genre> getGenres() {
        return genres != null ? genres : new ArrayList<>();
    }
    
    public String getStartYear() {
        return safeString(startYear);
    }
    
    public String getEndYear() {
        return safeString(endYear);
    }
    
    public boolean isSerial() {
        return safeBoolean(serial);
    }
    
    public boolean isShortFilm() {
        return safeBoolean(shortFilm);
    }
    
    public boolean isCompleted() {
        return safeBoolean(completed);
    }
    
    public boolean isHasImax() {
        return safeBoolean(hasImax);
    }
    
    public boolean isHas3D() {
        return safeBoolean(has3D);
    }
    
    public boolean isLastSync() {
        return safeBoolean(lastSync);
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
    
    /**
     * Возвращает лучшее доступное описание
     */
    public String getBestDescription() {
        if (!isEmpty(description)) return description;
        if (!isEmpty(shortDescription)) return shortDescription;
        if (!isEmpty(editorAnnotation)) return editorAnnotation;
        return "";
    }
    
    /**
     * Возвращает длительность в формате "XX мин" или "XX ч XX мин"
     */
    public String getFormattedDuration() {
        int minutes = getFilmLength();
        if (minutes <= 0) return "Не указано";
        
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        
        if (hours > 0) {
            return String.format("%d ч %d мин", hours, remainingMinutes);
        } else {
            return String.format("%d мин", minutes);
        }
    }
    
    @Override
    public String toString() {
        return String.format("FilmDetails{id=%d, name='%s', year='%s', rating=%.1f}", 
                           getKinopoiskId(), getBestName(), getYear(), getRatingKinopoisk());
    }
}
