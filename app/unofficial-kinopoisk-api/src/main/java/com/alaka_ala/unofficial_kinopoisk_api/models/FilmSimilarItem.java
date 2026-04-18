package com.alaka_ala.unofficial_kinopoisk_api.models;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

/**
 * Модель, представляющая один похожий фильм в списке.
 * Извлечен из FilmSimilarResponse для лучшей структуры.
 */
@Keep
public class FilmSimilarItem {
    @SerializedName("filmId")
    private int filmId;

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

    @SerializedName("relationType")
    private String relationType;

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

    public String getNameOriginal() {
        return nameOriginal;
    }

    public void setNameOriginal(String nameOriginal) {
        this.nameOriginal = nameOriginal;
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

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }
}
