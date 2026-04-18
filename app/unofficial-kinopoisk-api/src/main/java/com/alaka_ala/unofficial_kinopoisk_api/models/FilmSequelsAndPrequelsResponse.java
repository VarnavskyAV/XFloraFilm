package com.alaka_ala.unofficial_kinopoisk_api.models;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

/**
 * Модель-обертка для кэширования списка сиквелов и приквелов.
 */
@Keep
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
}
