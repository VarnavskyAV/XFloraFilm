package com.alaka_ala.unofficial_kinopoisk_api.models;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@Keep
@Entity(tableName = "person_search_response")
@TypeConverters(Converters.class)
public class PersonSearchResponse {
    @PrimaryKey(autoGenerate = false)
    @NonNull
    private String id;

    @SerializedName("total")
    private int total;

    @SerializedName("items")
    private List<Person> items;

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

    public List<Person> getItems() {
        return items;
    }

    public void setItems(List<Person> items) {
        this.items = items;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
