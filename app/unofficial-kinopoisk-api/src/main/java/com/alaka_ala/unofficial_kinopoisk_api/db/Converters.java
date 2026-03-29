package com.alaka_ala.unofficial_kinopoisk_api.db;

import androidx.room.TypeConverter;

import com.alaka_ala.unofficial_kinopoisk_api.models.FilmItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Конвертеры типов для базы данных Room.
 */
public class Converters {

    private static final Gson gson = new Gson();

    // --- Map<String, Long> Converter for Player Positions ---
    @TypeConverter
    public static String fromStringLongMap(Map<String, Long> map) {
        if (map == null) {
            return null;
        }
        return gson.toJson(map);
    }

    @TypeConverter
    public static Map<String, Long> toStringLongMap(String json) {
        if (json == null) {
            return Collections.emptyMap();
        }
        Type mapType = new TypeToken<Map<String, Long>>() {}.getType();
        return gson.fromJson(json, mapType);
    }

    // --- List<Genre> Converter ---
    @TypeConverter
    public static String fromGenreList(List<com.alaka_ala.unofficial_kinopoisk_api.models.Genre> genres) {
        if (genres == null) {
            return null;
        }
        return gson.toJson(genres);
    }

    @TypeConverter
    public static List<com.alaka_ala.unofficial_kinopoisk_api.models.Genre> toGenreList(String json) {
        if (json == null) {
            return Collections.emptyList();
        }
        Type listType = new TypeToken<List<com.alaka_ala.unofficial_kinopoisk_api.models.Genre>>() {}.getType();
        return gson.fromJson(json, listType);
    }

    // --- List<Country> Converter ---
    @TypeConverter
    public static String fromCountryList(List<com.alaka_ala.unofficial_kinopoisk_api.models.Country> countries) {
        if (countries == null) {
            return null;
        }
        return gson.toJson(countries);
    }

    @TypeConverter
    public static List<com.alaka_ala.unofficial_kinopoisk_api.models.Country> toCountryList(String json) {
        if (json == null) {
            return Collections.emptyList();
        }
        Type listType = new TypeToken<List<com.alaka_ala.unofficial_kinopoisk_api.models.Country>>() {}.getType();
        return gson.fromJson(json, listType);
    }


    // --- List<FilmItem> Converter ---
    @TypeConverter
    public static String fromFilmItemList(List<FilmItem> items) {
        if (items == null) {
            return null;
        }
        return gson.toJson(items);
    }

    @TypeConverter
    public static List<FilmItem> toFilmItemList(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        Type listType = new TypeToken<List<FilmItem>>() {}.getType();
        return gson.fromJson(json, listType);
    }


    // -- Для List<GenreResponse> --
    @TypeConverter
    public static String fromGenreResponseList(List<com.alaka_ala.unofficial_kinopoisk_api.models.GenreResponse> countries) {
        if (countries == null) {
            return null;
        }
        return gson.toJson(countries);
    }

    @TypeConverter
    public static List<com.alaka_ala.unofficial_kinopoisk_api.models.GenreResponse> toGenreResponseList(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        Type type = new TypeToken<List<com.alaka_ala.unofficial_kinopoisk_api.models.GenreResponse>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // -- Для List<GenreResponse> --
    @TypeConverter
    public static String fromCountryResponseList(List<com.alaka_ala.unofficial_kinopoisk_api.models.CountryResponse> countries) {
        if (countries == null) {
            return null;
        }
        return gson.toJson(countries);
    }

    @TypeConverter
    public static List<com.alaka_ala.unofficial_kinopoisk_api.models.CountryResponse> toCountryResponseList(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        Type type = new TypeToken<List<com.alaka_ala.unofficial_kinopoisk_api.models.CountryResponse>>() {}.getType();
        return gson.fromJson(json, type);
    }
}
