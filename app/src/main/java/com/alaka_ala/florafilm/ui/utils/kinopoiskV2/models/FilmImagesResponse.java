package com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Модель ответа для запроса изображений фильма, адаптированная для Room.
 */
@Entity(tableName = "film_images_response")
@TypeConverters(Converters.class)
public class FilmImagesResponse {

    /**
     * Уникальный идентификатор для записи в базе данных.
     * Формируется как "images_{filmId}_{type}_{page}".
     */
    @PrimaryKey
    @NonNull
    private String id;

    @SerializedName("total")
    private int total;

    @SerializedName("totalPages")
    private int totalPages;

    @SerializedName("items")
    private List<FilmImageItem> items;

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

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<FilmImageItem> getItems() {
        return items;
    }

    public void setItems(List<FilmImageItem> items) {
        this.items = items;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
