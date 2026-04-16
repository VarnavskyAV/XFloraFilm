package com.alaka_ala.florafilm.utils.balancers.alloha.models.serial;

// Класс TranslationIframe (такой же как для фильмов, но с полем date)
public class TranslationIframe {
    private String name;
    private String iframe;
    private String quality;
    private boolean adv;
    private String date;
    private Boolean uhd;  // может отсутствовать
    private Boolean lgbt; // может отсутствовать

    public TranslationIframe() {}

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

    public Boolean getUhd() { return uhd; }
    public void setUhd(Boolean uhd) { this.uhd = uhd; }

    public Boolean getLgbt() { return lgbt; }
    public void setLgbt(Boolean lgbt) { this.lgbt = lgbt; }
}
