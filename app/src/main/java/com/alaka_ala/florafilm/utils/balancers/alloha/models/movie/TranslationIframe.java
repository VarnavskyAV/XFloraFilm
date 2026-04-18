package com.alaka_ala.florafilm.utils.balancers.alloha.models.movie;

import androidx.annotation.Keep;

// Класс для переводов
@Keep
public class TranslationIframe {
    private String name;
    private String iframe;
    private String quality;
    private boolean adv;
    private String date;
    private boolean uhd;
    private boolean lgbt;

    public TranslationIframe() {}

    // Геттеры и сеттеры
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIframe() { return iframe; }
    public void setIframe(String iframe) { this.iframe = iframe; }

    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }

    public boolean isAdv() { return adv; }
    public void setAdv(boolean adv) { this.adv = adv; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public boolean isUhd() { return uhd; }
    public void setUhd(boolean uhd) { this.uhd = uhd; }

    public boolean isLgbt() { return lgbt; }
    public void setLgbt(boolean lgbt) { this.lgbt = lgbt; }
}