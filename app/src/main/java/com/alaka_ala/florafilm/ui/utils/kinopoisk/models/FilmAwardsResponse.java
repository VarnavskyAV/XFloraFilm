
package com.alaka_ala.florafilm.ui.utils.kinopoisk.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@Entity(tableName = "film_awards_response")
@TypeConverters(Converters.class)
public class FilmAwardsResponse {
    @PrimaryKey(autoGenerate = false)
    private String id;

    @SerializedName("total")
    private int total;

    @SerializedName("items")
    private List<AwardItem> items;

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

    public List<AwardItem> getItems() {
        return items;
    }

    public void setItems(List<AwardItem> items) {
        this.items = items;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
