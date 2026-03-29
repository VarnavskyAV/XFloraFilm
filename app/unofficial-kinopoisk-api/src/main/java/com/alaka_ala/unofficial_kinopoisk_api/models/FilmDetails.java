package com.alaka_ala.unofficial_kinopoisk_api.models;

import android.annotation.SuppressLint;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.alaka_ala.unofficial_kinopoisk_api.db.Converters;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

/**
 * Сущность для кэширования детальной информации о фильме.
 */
@Entity(tableName = "film_details")
public class FilmDetails extends BaseModel {

    @PrimaryKey
    @SerializedName("kinopoiskId")
    private Integer kinopoiskId;

    private Long lastUpdated;

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
    private Boolean lastSync; // Это сетевой флаг который говорит о том, когда были обновлены последний раз данные в базе
    /**Флаг говорит о том, что текущий фильм добавлен в коллекцию истории, а не о том, что фильм уже просмотрен до конца.*/
    private boolean isView;
    /**Флаг на то начат ли просмотр фильма. Говорит о том, что фильм уже начали ранее просматривать, и добавлен в коллекцию начатых к просмотру*/
    private boolean isStartView;
    /**Позиция просмотра фильма (Возможно придется перенести в отдельный класс по данным для просмотра)*/
    private long positionView;

    /** Флаг предназначен для отслеживания обновлений фильма. Задел на будущее, что бы можно было добавлять фильм в список ожидаемых*/
    private boolean observeUpdateVoice;

    /**Флаг на то добавлен ли филльм в закладки (избранное)*/
    private boolean isBookmark;
    /**Флаг который нужен для того, что бы отслеживать актуальность данных.
     * Записывается обычный timestamp когда фильм был просмотрен
     * (Описание фильма было просмотрено или если другими словами - когда была открыта страница фильма)*/
    private long timestampAddedHistory;

    /**
     * Карта сохранения позиции просмотра.
     * Ключ - это строковое представление IndexPath (например, "0_1_4_0_1"),
     * Значение - позиция в плеере (long).
     */
    @TypeConverters(Converters.class)
    private Map<String, Long> lastPositionPlayerView;



    public FilmDetails() {
        this.countries = new ArrayList<>();
        this.genres = new ArrayList<>();
    }



    // Getters
    public Integer getKinopoiskId() { return safeInt(kinopoiskId); }
    public Long getLastUpdated() { return lastUpdated; }
    public String getKinopoiskHDId() { return safeString(kinopoiskHDId); }
    public String getImdbId() { return safeString(imdbId); }
    public String getNameRu() { return safeString(nameRu); }
    public String getNameEn() { return safeString(nameEn); }
    public String getNameOriginal() { return safeString(nameOriginal); }
    public String getPosterUrl() { return safeString(posterUrl); }
    public String getPosterUrlPreview() { return safeString(posterUrlPreview); }
    public String getCoverUrl() { return safeString(coverUrl); }
    public String getLogoUrl() { return safeString(logoUrl); }
    public Integer getReviewsCount() { return safeInt(reviewsCount); }
    public Integer getRatingGoodReview() { return safeInt(ratingGoodReview); }
    public Integer getRatingGoodReviewVoteCount() { return safeInt(ratingGoodReviewVoteCount); }
    public Double getRatingKinopoisk() { return safeDouble(ratingKinopoisk); }
    public Integer getRatingKinopoiskVoteCount() { return safeInt(ratingKinopoiskVoteCount); }
    public Double getRatingImdb() { return safeDouble(ratingImdb); }
    public Integer getRatingImdbVoteCount() { return safeInt(ratingImdbVoteCount); }
    public Double getRatingFilmCritics() { return safeDouble(ratingFilmCritics); }
    public Integer getRatingFilmCriticsVoteCount() { return safeInt(ratingFilmCriticsVoteCount); }
    public Integer getRatingAwait() { return safeInt(ratingAwait); }
    public Integer getRatingAwaitCount() { return safeInt(ratingAwaitCount); }
    public Integer getRatingRfCritics() { return safeInt(ratingRfCritics); }
    public Integer getRatingRfCriticsVoteCount() { return safeInt(ratingRfCriticsVoteCount); }
    public String getWebUrl() { return safeString(webUrl); }
    public String getYear() { return safeString(year); }
    public Integer getFilmLength() { return safeInt(filmLength); }
    public String getSlogan() { return safeString(slogan); }
    public String getDescription() { return safeString(description); }
    public String getShortDescription() { return safeString(shortDescription); }
    public String getEditorAnnotation() { return safeString(editorAnnotation); }
    public Boolean isTicketsAvailable() { return safeBoolean(isTicketsAvailable); }
    public String getProductionStatus() { return safeString(productionStatus); }
    public String getType() { return safeString(type); }
    public String getRatingMpaa() { return safeString(ratingMpaa); }
    public String getRatingAgeLimits() { return safeString(ratingAgeLimits); }
    public List<Country> getCountries() { return countries != null ? countries : new ArrayList<>(); }
    public List<Genre> getGenres() { return genres != null ? genres : new ArrayList<>(); }
    public String getStartYear() { return safeString(startYear); }
    public String getEndYear() { return safeString(endYear); }
    public Boolean isSerial() { return safeBoolean(serial); }
    public Boolean isShortFilm() { return safeBoolean(shortFilm); }
    public Boolean isCompleted() { return safeBoolean(completed); }
    public Boolean isHasImax() { return safeBoolean(hasImax); }
    public Boolean isHas3D() { return safeBoolean(has3D); }
    public Boolean isLastSync() { return safeBoolean(lastSync); }
    public Boolean isView() {return safeBoolean(isView);}
    public Boolean isStartView() {return safeBoolean(isStartView);}
    public Long getPositionView() {return safeLong(positionView);}
    public Boolean isBookmark() {return safeBoolean(isBookmark);}
    public long getTimestampAddedHistory() {
        return timestampAddedHistory;
    }
    public boolean isObserveUpdateVoice(){
        return observeUpdateVoice;
    }
    public Map<String, Long> getLastPositionPlayerView() {
        return lastPositionPlayerView;
    }



    // Setters for Room
    public void setKinopoiskId(Integer kinopoiskId) { this.kinopoiskId = kinopoiskId; }
    public void setLastUpdated(Long lastUpdated) { this.lastUpdated = lastUpdated; }
    public void setKinopoiskHDId(String kinopoiskHDId) { this.kinopoiskHDId = kinopoiskHDId; }
    public void setNameRu(String nameRu) { this.nameRu = nameRu; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public void setNameOriginal(String nameOriginal) { this.nameOriginal = nameOriginal; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public void setPosterUrlPreview(String posterUrlPreview) { this.posterUrlPreview = posterUrlPreview; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public void setReviewsCount(Integer reviewsCount) { this.reviewsCount = reviewsCount; }
    public void setRatingGoodReview(Integer ratingGoodReview) { this.ratingGoodReview = ratingGoodReview; }
    public void setRatingGoodReviewVoteCount(Integer ratingGoodReviewVoteCount) { this.ratingGoodReviewVoteCount = ratingGoodReviewVoteCount; }
    public void setRatingKinopoisk(Double ratingKinopoisk) { this.ratingKinopoisk = ratingKinopoisk; }
    public void setRatingKinopoiskVoteCount(Integer ratingKinopoiskVoteCount) { this.ratingKinopoiskVoteCount = ratingKinopoiskVoteCount; }
    public void setRatingImdb(Double ratingImdb) { this.ratingImdb = ratingImdb; }
    public void setRatingImdbVoteCount(Integer ratingImdbVoteCount) { this.ratingImdbVoteCount = ratingImdbVoteCount; }
    public void setRatingFilmCritics(Double ratingFilmCritics) { this.ratingFilmCritics = ratingFilmCritics; }
    public void setRatingFilmCriticsVoteCount(Integer ratingFilmCriticsVoteCount) { this.ratingFilmCriticsVoteCount = ratingFilmCriticsVoteCount; }
    public void setRatingAwait(Integer ratingAwait) { this.ratingAwait = ratingAwait; }
    public void setRatingAwaitCount(Integer ratingAwaitCount) { this.ratingAwaitCount = ratingAwaitCount; }
    public void setRatingRfCritics(Integer ratingRfCritics) { this.ratingRfCritics = ratingRfCritics; }
    public void setRatingRfCriticsVoteCount(Integer ratingRfCriticsVoteCount) { this.ratingRfCriticsVoteCount = ratingRfCriticsVoteCount; }
    public void setWebUrl(String webUrl) { this.webUrl = webUrl; }
    public void setYear(String year) { this.year = year; }
    public void setFilmLength(Integer filmLength) { this.filmLength = filmLength; }
    public void setSlogan(String slogan) { this.slogan = slogan; }
    public void setDescription(String description) { this.description = description; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
    public void setEditorAnnotation(String editorAnnotation) { this.editorAnnotation = editorAnnotation; }
    public void setTicketsAvailable(Boolean ticketsAvailable) { isTicketsAvailable = ticketsAvailable; }
    public void setProductionStatus(String productionStatus) { this.productionStatus = productionStatus; }
    public void setType(String type) { this.type = type; }
    public void setRatingMpaa(String ratingMpaa) { this.ratingMpaa = ratingMpaa; }
    public void setRatingAgeLimits(String ratingAgeLimits) { this.ratingAgeLimits = ratingAgeLimits; }
    public void setCountries(List<Country> countries) { this.countries = countries; }
    public void setGenres(List<Genre> genres) { this.genres = genres; }
    public void setStartYear(String startYear) { this.startYear = startYear; }
    public void setEndYear(String endYear) { this.endYear = endYear; }
    public void setSerial(Boolean serial) { this.serial = serial; }
    public void setShortFilm(Boolean shortFilm) { this.shortFilm = shortFilm; }
    public void setCompleted(Boolean completed) { this.completed = completed; }
    public void setHasImax(Boolean hasImax) { this.hasImax = hasImax; }
    public void setHas3D(Boolean has3D) { this.has3D = has3D; }
    public void setLastSync(Boolean lastSync) { this.lastSync = lastSync; }
    public void setIsView(Boolean isView) {
        this.isView = isView;
    }
    public void setIsStartView(Boolean isStartView) {
        this.isStartView = isStartView;
    }
    public void setPositionView(Long positionView) {
        this.positionView = positionView;            // Не понимаю для чего это нужно
    }
    public void setIsBookmark(Boolean isBookmark) {
        this.isBookmark = isBookmark;
    }
    public void setTimestampAddedHistory(long timestampAddedHistory) {
        this.timestampAddedHistory = timestampAddedHistory;
    }
    public void setObserveUpdateVoice(boolean isObserveUpdateVoice) {
        this.observeUpdateVoice = isObserveUpdateVoice;
    }
    /**Нужно для сохранения позиции просмотра фильма или сериала.*/
    public void setLastPositionPlayerView(Map<String, Long> lastPositionPlayerView) {
        this.lastPositionPlayerView = lastPositionPlayerView;
    }
    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilmDetails that = (FilmDetails) o;
        return isView == that.isView &&
                isStartView == that.isStartView &&
                positionView == that.positionView &&
                isBookmark == that.isBookmark &&
                timestampAddedHistory == that.timestampAddedHistory &&
                Objects.equals(kinopoiskId, that.kinopoiskId) &&
                Objects.equals(lastUpdated, that.lastUpdated) &&
                Objects.equals(kinopoiskHDId, that.kinopoiskHDId) &&
                Objects.equals(imdbId, that.imdbId) &&
                Objects.equals(nameRu, that.nameRu) &&
                Objects.equals(nameEn, that.nameEn) &&
                Objects.equals(nameOriginal, that.nameOriginal) &&
                Objects.equals(posterUrl, that.posterUrl) &&
                Objects.equals(posterUrlPreview, that.posterUrlPreview) &&
                Objects.equals(coverUrl, that.coverUrl) &&
                Objects.equals(logoUrl, that.logoUrl) &&
                Objects.equals(reviewsCount, that.reviewsCount) &&
                Objects.equals(ratingGoodReview, that.ratingGoodReview) &&
                Objects.equals(ratingGoodReviewVoteCount, that.ratingGoodReviewVoteCount) &&
                Objects.equals(ratingKinopoisk, that.ratingKinopoisk) &&
                Objects.equals(ratingKinopoiskVoteCount, that.ratingKinopoiskVoteCount) &&
                Objects.equals(ratingImdb, that.ratingImdb) &&
                Objects.equals(ratingImdbVoteCount, that.ratingImdbVoteCount) &&
                Objects.equals(ratingFilmCritics, that.ratingFilmCritics) &&
                Objects.equals(ratingFilmCriticsVoteCount, that.ratingFilmCriticsVoteCount) &&
                Objects.equals(ratingAwait, that.ratingAwait) &&
                Objects.equals(ratingAwaitCount, that.ratingAwaitCount) &&
                Objects.equals(ratingRfCritics, that.ratingRfCritics) &&
                Objects.equals(ratingRfCriticsVoteCount, that.ratingRfCriticsVoteCount) &&
                Objects.equals(webUrl, that.webUrl) &&
                Objects.equals(year, that.year) &&
                Objects.equals(filmLength, that.filmLength) &&
                Objects.equals(slogan, that.slogan) &&
                Objects.equals(description, that.description) &&
                Objects.equals(shortDescription, that.shortDescription) &&
                Objects.equals(editorAnnotation, that.editorAnnotation) &&
                Objects.equals(isTicketsAvailable, that.isTicketsAvailable) &&
                Objects.equals(productionStatus, that.productionStatus) &&
                Objects.equals(type, that.type) &&
                Objects.equals(ratingMpaa, that.ratingMpaa) &&
                Objects.equals(ratingAgeLimits, that.ratingAgeLimits) &&
                Objects.equals(countries, that.countries) &&
                Objects.equals(genres, that.genres) &&
                Objects.equals(startYear, that.startYear) &&
                Objects.equals(endYear, that.endYear) &&
                Objects.equals(serial, that.serial) &&
                Objects.equals(shortFilm, that.shortFilm) &&
                Objects.equals(completed, that.completed) &&
                Objects.equals(hasImax, that.hasImax) &&
                Objects.equals(has3D, that.has3D) &&
                Objects.equals(lastSync, that.lastSync);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kinopoiskId, lastUpdated, kinopoiskHDId, imdbId, nameRu, nameEn, nameOriginal, posterUrl, posterUrlPreview, coverUrl, logoUrl, reviewsCount, ratingGoodReview, ratingGoodReviewVoteCount, ratingKinopoisk, ratingKinopoiskVoteCount, ratingImdb, ratingImdbVoteCount, ratingFilmCritics, ratingFilmCriticsVoteCount, ratingAwait, ratingAwaitCount, ratingRfCritics, ratingRfCriticsVoteCount, webUrl, year, filmLength, slogan, description, shortDescription, editorAnnotation, isTicketsAvailable, productionStatus, type, ratingMpaa, ratingAgeLimits, countries, genres, startYear, endYear, serial, shortFilm, completed, hasImax, has3D, lastSync, isView, isStartView, positionView, isBookmark, timestampAddedHistory);
    }

    // Helper methods
    public String getBestName() { 
        if (!isEmpty(nameRu)) return nameRu;
        if (!isEmpty(nameEn)) return nameEn;
        if (!isEmpty(nameOriginal)) return nameOriginal;
        return "Без названия";
    }
    
    public String getBestPoster() { 
        if (!isEmpty(posterUrl)) return posterUrl;
        if (!isEmpty(posterUrlPreview)) return posterUrlPreview;
        return "";
    }
    
    public String getBestDescription() { 
        if (!isEmpty(description)) return description;
        if (!isEmpty(shortDescription)) return shortDescription;
        return "";
    }
    
    @SuppressLint("DefaultLocale")
    public String getFormattedDuration() {
        int length = getFilmLength();
        if (length <= 0) return "";
        int hours = length / 60;
        int minutes = length % 60;
        if (hours > 0) {
            return String.format("%d ч %d мин", hours, minutes);
        } else {
            return String.format("%d мин", minutes);
        }
    }

    @SuppressLint("DefaultLocale")
    public String getFormattedViewPosition() {
        int hours = (int) (positionView / 3600);
        int minutes = (int) ((positionView % 3600) / 60);
        int seconds = (int) (positionView % 60);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

}
