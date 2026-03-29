package com.alaka_ala.unofficial_kinopoisk_api.models;

import com.google.gson.annotations.SerializedName;

public class FactItem {
    @SerializedName("text")
    private String text;

    @SerializedName("type")
    private String type; // "FACT" or "BLOOPER"

    @SerializedName("spoiler")
    private boolean spoiler;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSpoiler() {
        return spoiler;
    }

    public void setSpoiler(boolean spoiler) {
        this.spoiler = spoiler;
    }
}
