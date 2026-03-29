package com.alaka_ala.florafilm.utils;

/**
 * Общая модель данных для видео, используемая всеми балансерами.
 */
public class VideoData {
    private final String videoUrl;
    private final String quality;
    private final String translationName;

    /**
     * Конструктор для создания объекта VideoData.
     * @param videoUrl URL видеофайла или плейлиста.
     * @param quality Качество видео (например, "720p", "1080p", "AUTO").
     * @param translationName Название озвучки или перевода.
     */
    public VideoData(String videoUrl, String quality, String translationName) {
        this.videoUrl = videoUrl;
        this.quality = quality;
        this.translationName = translationName;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getQuality() {
        return quality;
    }

    public String getTranslationName() {
        return translationName;
    }
}
