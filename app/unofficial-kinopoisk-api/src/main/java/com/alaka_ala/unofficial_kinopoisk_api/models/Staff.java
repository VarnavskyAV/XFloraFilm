package com.alaka_ala.unofficial_kinopoisk_api.models;

import androidx.annotation.Keep;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Keep
@Entity(tableName = "staff")
public class Staff {
    @PrimaryKey
    @SerializedName("staffId")
    private int staffId;

    @SerializedName("nameRu")
    private String nameRu;

    @SerializedName("nameEn")
    private String nameEn;

    @SerializedName("description")
    private String description;

    @SerializedName("posterUrl")
    private String posterUrl;

    @SerializedName("professionText")
    private String professionText;

    @SerializedName("professionKey")
    private String professionKey;

    // Getters and setters

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
}
