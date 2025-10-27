package com.alaka_ala.florafilm.ui.utils.kinopoisk.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;

/**
 * Детальная информация о фильме
 * Содержит все поля для полного описания фильма
 */
@Entity(tableName = "films_details")
public class FilmDetails extends BaseModel {

    public void setKinopoiskId(Integer kinopoiskId) {
        this.kinopoiskId = kinopoiskId;
    }

    public void setKinopoiskHDId(String kinopoiskHDId) {
        this.kinopoiskHDId = kinopoiskHDId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }

    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public void setNameOriginal(String nameOriginal) {
        this.nameOriginal = nameOriginal;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public void setPosterUrlPreview(String posterUrlPreview) {
        this.posterUrlPreview = posterUrlPreview;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public void setReviewsCount(Integer reviewsCount) {
        this.reviewsCount = reviewsCount;
    }

    public void setRatingGoodReview(Integer ratingGoodReview) {
        this.ratingGoodReview = ratingGoodReview;
    }

    public void setRatingGoodReviewVoteCount(Integer ratingGoodReviewVoteCount) {
        this.ratingGoodReviewVoteCount = ratingGoodReviewVoteCount;
    }

    public void setRatingKinopoisk(Double ratingKinopoisk) {
        this.ratingKinopoisk = ratingKinopoisk;
    }

    public void setRatingKinopoiskVoteCount(Integer ratingKinopoiskVoteCount) {
        this.ratingKinopoiskVoteCount = ratingKinopoiskVoteCount;
    }

    public void setRatingImdb(Double ratingImdb) {
        this.ratingImdb = ratingImdb;
    }

    public void setRatingImdbVoteCount(Integer ratingImdbVoteCount) {
        this.ratingImdbVoteCount = ratingImdbVoteCount;
    }

    public void setRatingFilmCritics(Double ratingFilmCritics) {
        this.ratingFilmCritics = ratingFilmCritics;
    }

    public void setRatingFilmCriticsVoteCount(Integer ratingFilmCriticsVoteCount) {
        this.ratingFilmCriticsVoteCount = ratingFilmCriticsVoteCount;
    }

    public void setRatingAwait(Integer ratingAwait) {
        this.ratingAwait = ratingAwait;
    }

    public void setRatingAwaitCount(Integer ratingAwaitCount) {
        this.ratingAwaitCount = ratingAwaitCount;
    }

    public void setRatingRfCritics(Integer ratingRfCritics) {
        this.ratingRfCritics = ratingRfCritics;
    }

    public void setRatingRfCriticsVoteCount(Integer ratingRfCriticsVoteCount) {
        this.ratingRfCriticsVoteCount = ratingRfCriticsVoteCount;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setFilmLength(Integer filmLength) {
        this.filmLength = filmLength;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public void setEditorAnnotation(String editorAnnotation) {
        this.editorAnnotation = editorAnnotation;
    }

    public void setTicketsAvailable(Boolean ticketsAvailable) {
        isTicketsAvailable = ticketsAvailable;
    }

    public void setProductionStatus(String productionStatus) {
        this.productionStatus = productionStatus;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRatingMpaa(String ratingMpaa) {
        this.ratingMpaa = ratingMpaa;
    }

    public void setRatingAgeLimits(String ratingAgeLimits) {
        this.ratingAgeLimits = ratingAgeLimits;
    }

    public void setCountries(List<Country> countries) {
        this.countries = countries;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public void setStartYear(String startYear) {
        this.startYear = startYear;
    }

    public void setEndYear(String endYear) {
        this.endYear = endYear;
    }

    public void setSerial(Boolean serial) {
        this.serial = serial;
    }

    public void setShortFilm(Boolean shortFilm) {
        this.shortFilm = shortFilm;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public void setHasImax(Boolean hasImax) {
        this.hasImax = hasImax;
    }

    public void setHas3D(Boolean has3D) {
        this.has3D = has3D;
    }

    public void setLastSync(Boolean lastSync) {
        this.lastSync = lastSync;
    }

    @PrimaryKey
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
