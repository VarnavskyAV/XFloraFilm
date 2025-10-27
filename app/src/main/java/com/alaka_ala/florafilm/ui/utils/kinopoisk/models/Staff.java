package com.alaka_ala.florafilm.ui.utils.kinopoisk.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

/**
 * Модель, представляющая члена съемочной группы или актера.
 * Используется как для парсинга ответа API с помощью Gson, так и для хранения в базе данных Room.
 */
@Entity(tableName = "staff")
public class Staff {
    /**
     * Уникальный идентификатор сотрудника на Кинопоиске.
     */
    @PrimaryKey
    @SerializedName("staffId")
    private int staffId;

    /**
     * Имя на русском языке.
     */
    @SerializedName("nameRu")
    private String nameRu;

    /**
     * Имя на английском языке.
     */
    @SerializedName("nameEn")
    private String nameEn;

    /**
     * Описание (например, имя персонажа).
     */
    @SerializedName("description")
    private String description;

    /**
     * URL-адрес постера.
     */
    @SerializedName("posterUrl")
    private String posterUrl;

    /**
     * Профессия в текстовом виде (например, "Режиссеры").
     */
    @SerializedName("professionText")
    private String professionText;

    /**
     * Ключ профессии (например, "DIRECTOR").
     */
    @SerializedName("professionKey")
    private String professionKey;

    //<editor-fold desc="Getters and Setters">
    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public String getNameRu() {
        return nameRu;
    }

    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getProfessionText() {
        return professionText;
    }

    public void setProfessionText(String professionText) {
        this.professionText = professionText;
    }

    public String getProfessionKey() {
        return professionKey;
    }

    public void setProfessionKey(String professionKey) {
        this.professionKey = professionKey;
    }
    //</editor-fold>
}
