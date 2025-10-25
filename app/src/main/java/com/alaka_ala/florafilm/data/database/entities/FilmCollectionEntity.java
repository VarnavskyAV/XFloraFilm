package com.alaka_ala.florafilm.data.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.alaka_ala.florafilm.data.database.converters.FilmItemConverter;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmItem;

import java.util.List;

@Entity(tableName = "film_collections")
public class FilmCollectionEntity {

    @PrimaryKey
    @NonNull
    private String type;

    private String title;

    @TypeConverters(FilmItemConverter.class)
    private List<FilmItem> items;

    private long lastUpdated;

    public FilmCollectionEntity(@NonNull String type, String title, List<FilmItem> items, long lastUpdated) {
        this.type = type;
        this.title = title;
        this.items = items;
        this.lastUpdated = lastUpdated;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public List<FilmItem> getItems() {
        return items;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }
}
