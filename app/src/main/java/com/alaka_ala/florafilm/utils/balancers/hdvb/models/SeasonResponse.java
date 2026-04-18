// SeasonResponse.java
package com.alaka_ala.florafilm.utils.balancers.hdvb.models;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;
import java.util.List;

@Keep
public class SeasonResponse {
    private String title;
    private String id;

    @SerializedName("files")
    private List<EpisodeResponse> episodes;

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<EpisodeResponse> getEpisodes() { return episodes; }
    public void setEpisodes(List<EpisodeResponse> episodes) { this.episodes = episodes; }
}