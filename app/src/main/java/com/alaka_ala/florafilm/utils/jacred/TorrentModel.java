package com.alaka_ala.florafilm.utils.jacred;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class TorrentModel {

    @SerializedName("tracker")
    private String tracker;

    @SerializedName("url")
    private String url;

    @SerializedName("title")
    private String title;

    @SerializedName("size")
    private long size;

    @SerializedName("sizeName")
    private String sizeName;

    @SerializedName("createTime")
    private String createTime;

    @SerializedName("updateTime")
    private String updateTime;

    @SerializedName("sid")
    private int sid;

    @SerializedName("pir")
    private int pir;

    @SerializedName("magnet")
    private String magnet;

    @SerializedName("name")
    private String name;

    @SerializedName("originalname")
    private String originalName;

    @SerializedName("relased")
    private int released;

    @SerializedName("videotype")
    private String videoType;

    @SerializedName("quality")
    private int quality;

    @SerializedName("voices")
    private List<String> voices;

    @SerializedName("seasons")
    private List<String> seasons;

    @SerializedName("types")
    private List<String> types;

    // ==================== Геттеры ====================

    public String getTracker() {
        return tracker;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public long getSize() {
        return size;
    }

    public String getSizeName() {
        return sizeName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public int getSid() {
        return sid;
    }

    public int getPir() {
        return pir;
    }

    public String getMagnet() {
        return magnet;
    }

    public String getName() {
        return name;
    }

    public String getOriginalName() {
        return originalName;
    }

    public int getReleased() {
        return released;
    }

    public String getVideoType() {
        return videoType;
    }

    public int getQuality() {
        return quality;
    }

    public List<String> getVoices() {
        return voices != null ? voices : Collections.emptyList();
    }

    public List<String> getSeasons() {
        return seasons != null ? seasons : Collections.emptyList();
    }

    public List<String> getTypes() {
        return types != null ? types : Collections.emptyList();
    }
}