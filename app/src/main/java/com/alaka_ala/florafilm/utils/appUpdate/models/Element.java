package com.alaka_ala.florafilm.utils.appUpdate.models;

import com.google.gson.annotations.SerializedName;

public class Element {
    @SerializedName("versionCode")
    private int versionCode;

    @SerializedName("versionName")
    private String versionName;

    @SerializedName("outputFile")
    private String outputFile;

    public int getVersionCode() { return versionCode; }
    public String getVersionName() { return versionName; }
    public String getOutputFile() { return outputFile; }
}