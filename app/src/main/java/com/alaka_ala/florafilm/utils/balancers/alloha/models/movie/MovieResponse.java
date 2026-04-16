package com.alaka_ala.florafilm.utils.balancers.alloha.models.movie;

// Главный корневой класс
public class MovieResponse {
    private String status;
    private MovieData data;

    // Gson требует пустой конструктор
    public MovieResponse() {}

    // Геттеры и сеттеры
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public MovieData getData() { return data; }
    public void setData(MovieData data) { this.data = data; }
}
