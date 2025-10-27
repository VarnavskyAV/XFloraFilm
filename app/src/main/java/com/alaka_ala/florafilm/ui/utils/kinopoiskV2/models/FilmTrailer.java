package com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models;

import com.google.gson.annotations.SerializedName;

/**
 * Оптимизированная модель трейлера фильма
 * Использует Builder pattern для удобного создания объектов
 */
public class FilmTrailer extends BaseModel {
    
    @SerializedName("url")
    private String url;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("site")
    private String site;
    
    // Конструктор по умолчанию для Gson
    public FilmTrailer() {}
    
    // Приватный конструктор для Builder pattern
    private FilmTrailer(Builder builder) {
        this.url = safeString(builder.url);
        this.name = safeString(builder.name);
        this.site = safeString(builder.site);
    }
    
    public String getUrl() {
        return safeString(url);
    }
    
    public String getName() {
        return safeString(name);
    }
    
    public String getSite() {
        return safeString(site);
    }
    
    public void setUrl(String url) {
        this.url = safeString(url);
    }
    
    public void setName(String name) {
        this.name = safeString(name);
    }
    
    public void setSite(String site) {
        this.site = safeString(site);
    }
    
    @Override
    public String toString() {
        return String.format("FilmTrailer{name='%s', site='%s', url='%s'}", 
                           getName(), getSite(), getUrl());
    }
    
    /**
     * Builder для создания объектов FilmTrailer
     */
    public static class Builder {
        private String url;
        private String name;
        private String site;
        
        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }
        
        public Builder setName(String name) {
            this.name = name;
            return this;
        }
        
        public Builder setSite(String site) {
            this.site = site;
            return this;
        }
        
        public FilmTrailer build() {
            return new FilmTrailer(this);
        }
    }
}
