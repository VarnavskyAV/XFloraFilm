package com.alaka_ala.unofficial_kinopoisk_api.models;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

/**
 * Оптимизированная модель жанра
 * Использует аннотации Gson для автоматической сериализации/десериализации
 */
@Keep
public class Genre extends BaseModel {
    
    @SerializedName("genre")
    private String name;
    
    // Конструктор по умолчанию для Gson
    public Genre() {}
    
    // Конструктор с параметром
    public Genre(String name) {
        this.name = safeString(name);
    }
    
    public String getName() {
        return safeString(name);
    }
    
    public void setName(String name) {
        this.name = safeString(name);
    }
    
    @Override
    public String toString() {
        return getName();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Genre genre = (Genre) obj;
        return getName().equals(genre.getName());
    }
    
    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
