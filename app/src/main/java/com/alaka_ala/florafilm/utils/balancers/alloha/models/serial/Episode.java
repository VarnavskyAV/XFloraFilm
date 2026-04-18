package com.alaka_ala.florafilm.utils.balancers.alloha.models.serial;

import androidx.annotation.Keep;

import java.util.Map;

// Класс эпизода
@Keep
public class Episode {
    private String iframe;
    private int episode;
    private Map<String, EpisodeTranslation> translation;

    public Episode() {}

    public String getIframe() { return iframe; }
    public void setIframe(String iframe) { this.iframe = iframe; }

    public int getEpisode() { return episode; }
    public void setEpisode(int episode) { this.episode = episode; }

    public Map<String, EpisodeTranslation> getTranslation() { return translation; }
    public void setTranslation(Map<String, EpisodeTranslation> translation) { this.translation = translation; }
}
