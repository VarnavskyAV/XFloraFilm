package com.alaka_ala.florafilm.ui.utils.kinopoisk.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.ArrayList;

/**
 * Коллекция фильмов с метаданными
 * Оптимизированная версия для работы со списками фильмов
 */
@Entity(tableName = "film_collection")
public class FilmCollection extends BaseModel {

    @PrimaryKey
    @NonNull
    private String id = "";

    private String title;

    @SerializedName("total")
    private String total;

    @SerializedName("totalPages")
    private String totalPages;

    @SerializedName("items")
    private List<FilmItem> items;

    private long lastUpdated;

    // Конструктор по умолчанию для Room и Gson
    public FilmCollection() {
        this.items = new ArrayList<>();
    }

    @Ignore
    public FilmCollection(String title, String total, String totalPages, List<FilmItem> items) {
        this.title = safeString(title);
        this.total = safeString(total);
        this.totalPages = safeString(totalPages);
        this.items = items != null ? items : new ArrayList<>();
    }

    // Getters
    @NonNull
    public String getId() {
        return id;
    }

    public String getTitle() {
        return safeString(title);
    }

    public String getTotal() {
        return safeString(total);
    }

    public String getTotalPages() {
        return safeString(totalPages);
    }

    public List<FilmItem> getItems() {
        return items != null ? items : new ArrayList<>();
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    // Setters
    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = safeString(title);
    }

    public void setTotal(String total) {
        this.total = safeString(total);
    }

    public void setTotalPages(String totalPages) {
        this.totalPages = safeString(totalPages);
    }

    public void setItems(List<FilmItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Ignore
    public int getItemsCount() {
        return getItems().size();
    }

    @Ignore
    public boolean isEmpty() {
        return getItemsCount() == 0;
    }

    @Ignore
    public int getTotalCount() {
        try {
            return Integer.parseInt(getTotal());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Ignore
    public int getTotalPagesCount() {
        try {
            return Integer.parseInt(getTotalPages());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Ignore
    public void addItem(FilmItem item) {
        if (item != null) {
            getItems().add(item);
        }
    }

    @Ignore
    public void addItems(List<FilmItem> items) {
        if (items != null) {
            getItems().addAll(items);
        }
    }

    @Ignore
    public void clear() {
        getItems().clear();
    }

    @Override
    public String toString() {
        return String.format("FilmCollection{id='%s', title='%s', total=%s, items=%d}", getId(), getTitle(), getTotal(), getItemsCount());
    }
}
