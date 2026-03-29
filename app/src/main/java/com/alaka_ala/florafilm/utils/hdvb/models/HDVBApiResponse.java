package com.alaka_ala.florafilm.utils.hdvb.models;

import com.google.gson.annotations.SerializedName;

public class HDVBApiResponse {
    @SerializedName("title_ru")
    private String titleRu;

    @SerializedName("title_en")
    private String titleEn;

    private int year;

    @SerializedName("kinopoisk_id")
    private int kinopoiskId;

    private String translator;
    private String type;

    @SerializedName("iframe_url")
    private String iframeUrl;

    private Object block; // Используем Object вместо конкретного типа

    private String quality;
    private String trailer;

    @SerializedName("added_date")
    private String addedDate;

    private String poster;

    // Геттер для block с проверкой типа
    public boolean hasBlocks() {
        return block instanceof java.util.List;
    }

    @SuppressWarnings("unchecked")
    public java.util.List<String> getBlocks() {
        if (block instanceof java.util.List) {
            return (java.util.List<String>) block;
        }
        return java.util.Collections.emptyList();
    }

    public boolean isBlocked() {
        return block instanceof Boolean && (Boolean) block;
    }

    // Остальные геттеры и сеттеры...
    public String getTitleRu() { return titleRu; }
    public void setTitleRu(String titleRu) { this.titleRu = titleRu; }

    public String getTitleEn() { return titleEn; }
    public void setTitleEn(String titleEn) { this.titleEn = titleEn; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getKinopoiskId() { return kinopoiskId; }
    public void setKinopoiskId(int kinopoiskId) { this.kinopoiskId = kinopoiskId; }

    public String getTranslator() { return translator; }
    public void setTranslator(String translator) { this.translator = translator; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getIframeUrl() { return iframeUrl; }
    public void setIframeUrl(String iframeUrl) { this.iframeUrl = iframeUrl; }

    public Object getBlock() { return block; }
    public void setBlock(Object block) { this.block = block; }

    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }

    public String getTrailer() { return trailer; }
    public void setTrailer(String trailer) { this.trailer = trailer; }

    public String getAddedDate() { return addedDate; }
    public void setAddedDate(String addedDate) { this.addedDate = addedDate; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }
}