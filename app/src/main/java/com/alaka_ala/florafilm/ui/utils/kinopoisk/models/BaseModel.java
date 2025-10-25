package com.alaka_ala.florafilm.ui.utils.kinopoisk.models;

import java.io.Serializable;

/**
 * Базовый класс для всех моделей данных
 * Содержит общие методы и интерфейсы
 */
public abstract class BaseModel implements Serializable {
    
    /**
     * Проверяет, является ли строка пустой или null
     */
    protected boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty() || "null".equals(value);
    }
    
    /**
     * Возвращает безопасное значение строки
     */
    protected String safeString(String value) {
        return isEmpty(value) ? "" : value;
    }
    
    /**
     * Возвращает безопасное значение строки с дефолтным значением
     */
    protected String safeString(String value, String defaultValue) {
        return isEmpty(value) ? defaultValue : value;
    }
    
    /**
     * Возвращает безопасное значение числа
     */
    protected int safeInt(Integer value) {
        return value != null ? value : 0;
    }
    
    /**
     * Возвращает безопасное значение числа с дефолтным значением
     */
    protected int safeInt(Integer value, int defaultValue) {
        return value != null ? value : defaultValue;
    }
    
    /**
     * Возвращает безопасное значение double
     */
    protected double safeDouble(Double value) {
        return value != null ? value : 0.0;
    }
    
    /**
     * Возвращает безопасное значение double с дефолтным значением
     */
    protected double safeDouble(Double value, double defaultValue) {
        return value != null ? value : defaultValue;
    }
    
    /**
     * Возвращает безопасное значение boolean
     */
    protected boolean safeBoolean(Boolean value) {
        return value != null ? value : false;
    }
    
    /**
     * Возвращает безопасное значение boolean с дефолтным значением
     */
    protected boolean safeBoolean(Boolean value, boolean defaultValue) {
        return value != null ? value : defaultValue;
    }
}
