package com.alaka_ala.florafilm.ui.utils.kinopoisk.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Модель-обертка для кэширования списка сиквелов и приквелов.
 */
@Entity(tableName = "film_sequels_and_prequels_response")
@TypeConverters(Converters.class)
public class FilmSequelsAndPrequelsResponse {

    /**
     * Уникальный идентификатор для записи в базе данных.
     * Формируется как "sequels_" + ID фильма.
     */
    @PrimaryKey
    @NonNull
    private String id;

    private List<FilmSequelOrPrequel> items;

    private long lastUpdated;

    // Getters and Setters

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public List<FilmSequelOrPrequel> getItems() {
        return items;
    }

    public void setItems(List<FilmSequelOrPrequel> items) {
        this.items = items;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * Модель, представляющая один сиквел или приквел в списке.
     */
    public static class FilmSequelOrPrequel {
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

}


