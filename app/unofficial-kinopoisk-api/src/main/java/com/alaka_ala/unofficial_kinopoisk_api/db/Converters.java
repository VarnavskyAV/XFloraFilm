package com.alaka_ala.unofficial_kinopoisk_api.db;

import androidx.annotation.Keep;
import androidx.room.TypeConverter;

import com.alaka_ala.unofficial_kinopoisk_api.models.Country;
import com.alaka_ala.unofficial_kinopoisk_api.models.CountryResponse;
import com.alaka_ala.unofficial_kinopoisk_api.models.FilmItem;
import com.alaka_ala.unofficial_kinopoisk_api.models.Genre;
import com.alaka_ala.unofficial_kinopoisk_api.models.GenreResponse;
import com.google.gson.Gson;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Конвертеры типов для базы данных Room.
 */
@Keep
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
        // Для Map оставляем TypeToken, т.к. с массивами тут не работает
        return gson.fromJson(json, new com.google.gson.reflect.TypeToken<Map<String, Long>>(){}.getType());
    }

    // --- List<Genre> Converter ---
    @TypeConverter
    public static String fromGenreList(List<Genre> genres) {
        if (genres == null) {
            return null;
        }
        return gson.toJson(genres);
    }

    @TypeConverter
    public static List<Genre> toGenreList(String json) {
        if (json == null) {
            return Collections.emptyList();
        }
        Genre[] items = gson.fromJson(json, Genre[].class);
        return items != null ? Arrays.asList(items) : Collections.emptyList();
    }

    // --- List<Country> Converter ---
    @TypeConverter
    public static String fromCountryList(List<Country> countries) {
        if (countries == null) {
            return null;
        }
        return gson.toJson(countries);
    }

    @TypeConverter
    public static List<Country> toCountryList(String json) {
        if (json == null) {
            return Collections.emptyList();
        }
        Country[] items = gson.fromJson(json, Country[].class);
        return items != null ? Arrays.asList(items) : Collections.emptyList();
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
        FilmItem[] items = gson.fromJson(json, FilmItem[].class);
        return items != null ? Arrays.asList(items) : Collections.emptyList();
    }

    // -- Для List<GenreResponse> --
    @TypeConverter
    public static String fromGenreResponseList(List<GenreResponse> countries) {
        if (countries == null) {
            return null;
        }
        return gson.toJson(countries);
    }

    @TypeConverter
    public static List<GenreResponse> toGenreResponseList(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        GenreResponse[] items = gson.fromJson(json, GenreResponse[].class);
        return items != null ? Arrays.asList(items) : Collections.emptyList();
    }

    // -- Для List<CountryResponse> --
    @TypeConverter
    public static String fromCountryResponseList(List<CountryResponse> countries) {
        if (countries == null) {
            return null;
        }
        return gson.toJson(countries);
    }

    @TypeConverter
    public static List<CountryResponse> toCountryResponseList(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        CountryResponse[] items = gson.fromJson(json, CountryResponse[].class);
        return items != null ? Arrays.asList(items) : Collections.emptyList();
    }

    // -- Для List<Integer> selectableIndexPath --
    @TypeConverter
    public static String fromIndexPathList(List<Integer> selectedIndexPath) {
        if (selectedIndexPath == null) {
            return null;
        }
        return gson.toJson(selectedIndexPath);
    }

    @TypeConverter
    public static List<Integer> toSelectableIndexPath(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        Integer[] items = gson.fromJson(json, Integer[].class);
        return items != null ? Arrays.asList(items) : Collections.emptyList();
    }
}