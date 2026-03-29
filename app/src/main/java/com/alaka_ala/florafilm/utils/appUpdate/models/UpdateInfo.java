package com.alaka_ala.florafilm.utils.appUpdate.models;

import com.google.gson.annotations.SerializedName;

public class UpdateInfo {
    @SerializedName("versionCode")
    private int versionCode;

    @SerializedName("versionName")
    private String versionName;

    @SerializedName("downloadUrl")
    private String downloadUrl;

    @SerializedName("releaseNotes")
    private String releaseNotes;

    @SerializedName("fileSize")
    private long fileSize;

    public UpdateInfo(int versionCode, String versionName, String downloadUrl, String releaseNotes, long fileSize) {
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.downloadUrl = downloadUrl;
        this.releaseNotes = releaseNotes;
        this.fileSize = fileSize;
    }

    // Getters
    public int getVersionCode() { return versionCode; }
    public String getVersionName() { return versionName; }
    public String getDownloadUrl() { return downloadUrl; }
    public String getReleaseNotes() { return releaseNotes; }
    public long getFileSize() { return fileSize; }
}


