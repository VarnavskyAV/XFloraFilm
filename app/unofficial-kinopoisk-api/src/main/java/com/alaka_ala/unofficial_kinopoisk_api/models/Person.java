package com.alaka_ala.unofficial_kinopoisk_api.models;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

@Keep
@Entity(tableName = "persons")
public class Person {
    @PrimaryKey
    @SerializedName("kinopoiskId")
    private int kinopoiskId;

    @SerializedName("webUrl")
    private String webUrl;

    @SerializedName("nameRu")
    private String nameRu;

    @SerializedName("nameEn")
    private String nameEn;

    @SerializedName("sex")
    private String sex;

    @SerializedName("posterUrl")
    private String posterUrl;

    @Nullable
    @SerializedName("growth")
    private Integer growth;

    @Nullable
    @SerializedName("birthday")
    private String birthday;

    @Nullable
    @SerializedName("death")
    private String death;

    @Nullable
    @SerializedName("age")
    private Integer age;

    @Nullable
    @SerializedName("birthplace")
    private String birthplace;

    @Nullable
    @SerializedName("deathplace")
    private String deathplace;

    @Nullable
    @SerializedName("profession")
    private String profession;

    public int getKinopoiskId() {
        return kinopoiskId;
    }

    public void setKinopoiskId(int kinopoiskId) {
        this.kinopoiskId = kinopoiskId;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
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

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    @Nullable
    public Integer getGrowth() {
        return growth;
    }

    public void setGrowth(@Nullable Integer growth) {
        this.growth = growth;
    }

    @Nullable
    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(@Nullable String birthday) {
        this.birthday = birthday;
    }

    @Nullable
    public String getDeath() {
        return death;
    }

    public void setDeath(@Nullable String death) {
        this.death = death;
    }

    @Nullable
    public Integer getAge() {
        return age;
    }

    public void setAge(@Nullable Integer age) {
        this.age = age;
    }

    @Nullable
    public String getBirthplace() {
        return birthplace;
    }

    public void setBirthplace(@Nullable String birthplace) {
        this.birthplace = birthplace;
    }

    @Nullable
    public String getDeathplace() {
        return deathplace;
    }

    public void setDeathplace(@Nullable String deathplace) {
        this.deathplace = deathplace;
    }

    @Nullable
    public String getProfession() {
        return profession;
    }

    public void setProfession(@Nullable String profession) {
        this.profession = profession;
    }
}
