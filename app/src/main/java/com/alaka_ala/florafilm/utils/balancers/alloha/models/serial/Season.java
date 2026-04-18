package com.alaka_ala.florafilm.utils.balancers.alloha.models.serial;

import androidx.annotation.Keep;

import java.util.Map;

// Класс сезона
@Keep
public class Season {
    private String iframe;
    private int season;
    private Map<String, Episode> episodes;

    public Season() {}

    public String getIframe() { return iframe; }
    public void setIframe(String iframe) { this.iframe = iframe; }

    public int getSeason() { return season; }
    public void setSeason(int season) { this.season = season; }

    public Map<String, Episode> getEpisodes() { return episodes; }
    public void setEpisodes(Map<String, Episode> episodes) { this.episodes = episodes; }
}