package com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models;

import com.google.gson.annotations.SerializedName;

/**
 * Модель, представляющая одно изображение в списке.
 * Извлечен из FilmImagesResponse для лучшей структуры.
 */
public class FilmImageItem {
    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("previewUrl")
    private String previewUrl;

    // Getters and Setters

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }
}
