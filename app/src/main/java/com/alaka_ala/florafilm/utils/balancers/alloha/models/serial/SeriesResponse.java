package com.alaka_ala.florafilm.utils.balancers.alloha.models.serial;

// Главный корневой класс (такой же как для фильмов)
public class SeriesResponse {
    private String status;
    private SeriesData data;

    public SeriesResponse() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public SeriesData getData() { return data; }
    public void setData(SeriesData data) { this.data = data; }
}
