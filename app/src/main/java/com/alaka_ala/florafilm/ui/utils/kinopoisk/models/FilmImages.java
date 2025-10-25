package com.alaka_ala.florafilm.ui.utils.kinopoisk.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;

/**
 * Модель для изображений фильма
 */
public class FilmImages extends BaseModel {
    
    @SerializedName("total")
    private Integer total;
    
    @SerializedName("totalPages")
    private Integer totalPages;
    
    @SerializedName("items")
    private List<FilmImage> items;
    
    // Конструктор по умолчанию для Gson
    public FilmImages() {
        this.items = new ArrayList<>();
    }
    
    // Getters
    public int getTotal() {
        return safeInt(total);
    }
    
    public int getTotalPages() {
        return safeInt(totalPages);
    }
    
    public List<FilmImage> getItems() {
        return items != null ? items : new ArrayList<>();
    }
    
    // Setters
    public void setTotal(Integer total) {
        this.total = total;
    }
    
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
    
    public void setItems(List<FilmImage> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
    
    /**
     * Возвращает количество изображений
     */
    public int getItemsCount() {
        return getItems().size();
    }
    
    /**
     * Проверяет, пуста ли коллекция изображений
     */
    public boolean isEmpty() {
        return getItemsCount() == 0;
    }
    
    @Override
    public String toString() {
        return String.format("FilmImages{total=%d, pages=%d, items=%d}", 
                           getTotal(), getTotalPages(), getItemsCount());
    }
    
    /**
     * Модель отдельного изображения
     */
    public static class FilmImage extends BaseModel {
        
        @SerializedName("imageUrl")
        private String imageUrl;
        
        @SerializedName("previewUrl")
        private String previewUrl;
        
        @SerializedName("type")
        private String type;
        
        @SerializedName("width")
        private Integer width;
        
        @SerializedName("height")
        private Integer height;
        
        // Конструктор по умолчанию для Gson
        public FilmImage() {}
        
        // Getters
        public String getImageUrl() {
            return safeString(imageUrl);
        }
        
        public String getPreviewUrl() {
            return safeString(previewUrl);
        }
        
        public String getType() {
            return safeString(type);
        }
        
        public int getWidth() {
            return safeInt(width);
        }
        
        public int getHeight() {
            return safeInt(height);
        }
        
        // Setters
        public void setImageUrl(String imageUrl) {
            this.imageUrl = safeString(imageUrl);
        }
        
        public void setPreviewUrl(String previewUrl) {
            this.previewUrl = safeString(previewUrl);
        }
        
        public void setType(String type) {
            this.type = safeString(type);
        }
        
        public void setWidth(Integer width) {
            this.width = width;
        }
        
        public void setHeight(Integer height) {
            this.height = height;
        }
        
        /**
         * Возвращает лучшее доступное изображение
         */
        public String getBestImageUrl() {
            if (!isEmpty(imageUrl)) return imageUrl;
            if (!isEmpty(previewUrl)) return previewUrl;
            return "";
        }
        
        /**
         * Возвращает соотношение сторон
         */
        public double getAspectRatio() {
            int w = getWidth();
            int h = getHeight();
            if (w > 0 && h > 0) {
                return (double) w / h;
            }
            return 1.0;
        }
        
        @Override
        public String toString() {
            return String.format("FilmImage{type='%s', size=%dx%d, url='%s'}", 
                               getType(), getWidth(), getHeight(), getBestImageUrl());
        }
    }
}
