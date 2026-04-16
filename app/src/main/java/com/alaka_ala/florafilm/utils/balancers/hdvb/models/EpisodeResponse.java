// EpisodeResponse.java
package com.alaka_ala.florafilm.utils.balancers.hdvb.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EpisodeResponse {
    private String episode;
    private String title;
    private String id;

    @SerializedName("files")
    private List<Object> files; // Используем Object для гибкости

    // Геттер для получения перевода
    public TranslationResponse getFirstTranslation() {
        if (files != null && !files.isEmpty()) {
            // Первый элемент массива - объект с переводом
            Object firstItem = files.get(0);
            if (firstItem instanceof TranslationResponse) {
                return (TranslationResponse) firstItem;
            }
        }
        return null;
    }

    // Getters and setters
    public String getEpisode() { return episode; }
    public void setEpisode(String episode) { this.episode = episode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<Object> getFiles() { return files; }
    public void setFiles(List<Object> files) { this.files = files; }
}