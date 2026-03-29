package com.alaka_ala.unofficial_kinopoisk_api.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Модель ответа для запроса похожих фильмов, адаптированная для Room.
 * Хранит список похожих фильмов и метаданные.
 */
@Entity(tableName = "film_similar_response")
@TypeConverters(Converters.class)
public class FilmSimilarResponse {

    /**
     * Уникальный идентификатор для записи в базе данных.
     * Формируется как "similar_" + ID фильма, для которого запрошены похожие.
     */
    @PrimaryKey
    @NonNull
    private String id;

    @SerializedName("total")
    private int total;

    @SerializedName("items")
    private List<FilmSimilarItem> items;

    private long lastUpdated;

    // Getters and Setters

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<FilmSimilarItem> getItems() {
        return items;
    }

    public void setItems(List<FilmSimilarItem> items) {
        this.items = items;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
