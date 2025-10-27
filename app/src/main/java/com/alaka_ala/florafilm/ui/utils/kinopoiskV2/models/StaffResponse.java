package com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

@Entity(tableName = "staff_response")
@TypeConverters(Converters.class)
public class StaffResponse {
    @PrimaryKey(autoGenerate = false)
    @NonNull
    private String id;
    private List<Staff> items;
    private long lastUpdated;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Staff> getItems() {
        return items;
    }

    public void setItems(List<Staff> items) {
        this.items = items;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
