package com.alaka_ala.florafilm.utils.balancers.alloha.models.serial;

import androidx.annotation.Keep;

// Класс перевода для эпизода
@Keep
public class EpisodeTranslation {
    private String translation;
    private String quality;
    private String iframe;
    private boolean uhd;
    private boolean lgbt;

    public EpisodeTranslation() {}

    public String getTranslation() { return translation; }
    public void setTranslation(String translation) { this.translation = translation; }

    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }

    public String getIframe() { return iframe; }
    public void setIframe(String iframe) { this.iframe = iframe; }

    public boolean isUhd() { return uhd; }
    public void setUhd(boolean uhd) { this.uhd = uhd; }

    public boolean isLgbt() { return lgbt; }
    public void setLgbt(boolean lgbt) { this.lgbt = lgbt; }
}
