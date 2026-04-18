package com.alaka_ala.unofficial_kinopoisk_api.models;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@Entity(tableName = "film_facts_response")
@TypeConverters(Converters.class)
@Keep
public class FilmFactsResponse {
    @PrimaryKey(autoGenerate = false)
    @NonNull
    private String id;

    @SerializedName("total")
    private int total;

    @SerializedName("items")
    private List<FactItem> items;

    private long lastUpdated;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<FactItem> getItems() {
        return items;
    }

    public void setItems(List<FactItem> items) {
        this.items = items;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}

