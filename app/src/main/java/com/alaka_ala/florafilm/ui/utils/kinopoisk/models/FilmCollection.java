package com.alaka_ala.florafilm.ui.utils.kinopoisk.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.ArrayList;

/**
 * Коллекция фильмов с метаданными
 * Оптимизированная версия для работы со списками фильмов
 */

public class FilmCollection extends BaseModel {

    private String title;

    @SerializedName("total")
    private String total;

    @SerializedName("totalPages")
    private String totalPages;

    @SerializedName("items")
    private List<FilmItem> items;

    // Конструктор по умолчанию для Gson
    public FilmCollection() {
        this.items = new ArrayList<>();
    }

    // Конструктор с параметрами
    public FilmCollection(String title, String total, String totalPages, List<FilmItem> items) {
        this.title = safeString(title);
        this.total = safeString(total);
        this.totalPages = safeString(totalPages);
        this.items = items != null ? items : new ArrayList<>();
    }

    // Getters
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

    // Setters
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

    /**
     * Возвращает количество фильмов в коллекции
     */
    public int getItemsCount() {
        return getItems().size();
    }

    /**
     * Проверяет, пуста ли коллекция
     */
    public boolean isEmpty() {
        return getItemsCount() == 0;
    }

    /**
     * Возвращает общее количество фильмов (из API)
     */
    public int getTotalCount() {
        try {
            return Integer.parseInt(getTotal());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Возвращает общее количество страниц
     */
    public int getTotalPagesCount() {
        try {
            return Integer.parseInt(getTotalPages());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Добавляет фильм в коллекцию
     */
    public void addItem(FilmItem item) {
        if (item != null) {
            getItems().add(item);
        }
    }

    /**
     * Добавляет список фильмов в коллекцию
     */
    public void addItems(List<FilmItem> items) {
        if (items != null) {
            getItems().addAll(items);
        }
    }

    /**
     * Очищает коллекцию
     */
    public void clear() {
        getItems().clear();
    }

    @Override
    public String toString() {
        return String.format("FilmCollection{title='%s', total=%s, items=%d}", getTitle(), getTotal(), getItemsCount());
    }
}
