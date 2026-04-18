package com.alaka_ala.florafilm.utils.balancers.hdvb.models;

import androidx.annotation.Keep;

/** Модель данных из HTML страницы HDVB*/
@Keep
public class PlayerConfig {
    private String key;
    private String file;
    private String href;
    private String cuid;

    // Getters and setters
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getFile() { return file; }
    public void setFile(String file) { this.file = file; }

    public String getHref() { return href; }
    public void setHref(String href) { this.href = href; }

    public String getCuid() { return cuid; }
    public void setCuid(String cuid) { this.cuid = cuid; }
}