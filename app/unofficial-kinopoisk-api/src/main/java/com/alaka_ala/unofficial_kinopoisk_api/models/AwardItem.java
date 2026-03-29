package com.alaka_ala.unofficial_kinopoisk_api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AwardItem {
    @SerializedName("name")
    private String name;

    @SerializedName("win")
    private boolean win;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("nominationName")
    private String nominationName;

    @SerializedName("year")
    private int year;

    @SerializedName("persons")
    private List<Person> persons;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isWin() {
        return win;
    }

    public void setWin(boolean win) {
        this.win = win;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getNominationName() {
        return nominationName;
    }

    public void setNominationName(String nominationName) {
        this.nominationName = nominationName;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<Person> getPersons() {
        return persons;
    }

    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }
}
