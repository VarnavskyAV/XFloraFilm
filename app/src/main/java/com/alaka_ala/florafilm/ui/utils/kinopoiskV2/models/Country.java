package com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models;

import com.google.gson.annotations.SerializedName;

/**
 * Оптимизированная модель страны
 * Использует аннотации Gson для автоматической сериализации/десериализации
 */
public class Country extends BaseModel {
    
    @SerializedName("country")
    private String name;
    
    // Конструктор по умолчанию для Gson
    public Country() {}
    
    // Конструктор с параметром
    public Country(String name) {
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
        Country country = (Country) obj;
        return getName().equals(country.getName());
    }
    
    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
