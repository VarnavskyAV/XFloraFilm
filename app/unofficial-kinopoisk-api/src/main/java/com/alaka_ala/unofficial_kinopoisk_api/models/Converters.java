package com.alaka_ala.unofficial_kinopoisk_api.models;

import androidx.annotation.Keep;
import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Type converters to allow Room to reference complex data types.
 */
@Keep
public class Converters {
    private static Gson gson = new Gson();

    // --- FilmItem Converters ---
    @TypeConverter
    public static List<FilmItem> fromStringToFilmItemList(String data) {
        if (data == null) return Collections.emptyList();
        FilmItem[] items = gson.fromJson(data, FilmItem[].class);
        return items != null ? Arrays.asList(items) : Collections.emptyList();
    }

    @TypeConverter
    public static String fromFilmItemList(List<FilmItem> filmItems) {
        return gson.toJson(filmItems);
    }

    // --- GenreResponse Converters ---
    @TypeConverter
    public static List<GenreResponse> fromStringToGenreResponseList(String data) {
        if (data == null) return Collections.emptyList();
        GenreResponse[] items = gson.fromJson(data, GenreResponse[].class);
        return items != null ? Arrays.asList(items) : Collections.emptyList();
    }

    @TypeConverter
    public static String fromGenreResponseList(List<GenreResponse> genres) {
        return gson.toJson(genres);
    }

    // --- CountryResponse Converters ---
    @TypeConverter
    public static List<CountryResponse> fromStringToCountryResponseList(String data) {
        if (data == null) return Collections.emptyList();
        CountryResponse[] items = gson.fromJson(data, CountryResponse[].class);
        return items != null ? Arrays.asList(items) : Collections.emptyList();
    }

    @TypeConverter
    public static String fromCountryResponseList(List<CountryResponse> countries) {
        return gson.toJson(countries);
    }

    // --- Genre Converters ---
    @TypeConverter
    public static List<Genre> fromStringToGenreList(String data) {
        if (data == null) return Collections.emptyList();
        Genre[] items = gson.fromJson(data, Genre[].class);
        return items != null ? Arrays.asList(items) : Collections.emptyList();
    }

    @TypeConverter
    public static String fromGenreList(List<Genre> genres) {
        return gson.toJson(genres);
    }

    // --- Country Converters ---
    @TypeConverter
    public static List<Country> fromStringToCountryList(String data) {
        if (data == null) return Collections.emptyList();
        // Используем массив вместо TypeToken
        Country[] countries = gson.fromJson(data, Country[].class);
        return countries != null ? Arrays.asList(countries) : Collections.emptyList();
    }

    @TypeConverter
    public static String fromCountryList(List<Country> countries) {
        return gson.toJson(countries);
    }

    // --- FilmSimilarItem Converters ---
    @TypeConverter
    public static List<FilmSimilarItem> fromStringToFilmSimilarItemList(String data) {
        if (data == null) return null;
        FilmSimilarItem[] items = gson.fromJson(data, FilmSimilarItem[].class);
        return items != null ? Arrays.asList(items) : null;
    }

    @TypeConverter
    public static String fromFilmSimilarItemList(List<FilmSimilarItem> items) {
        return gson.toJson(items);
    }

    // --- FilmImageItem Converters ---
    @TypeConverter
    public static List<FilmImageItem> fromStringToFilmImageItemList(String data) {
        if (data == null) {
            return null;
        }
        FilmImageItem[] items = gson.fromJson(data, FilmImageItem[].class);
        return items != null ? Arrays.asList(items) : null;
    }

    @TypeConverter
    public static String fromFilmImageItemList(List<FilmImageItem> items) {
        return gson.toJson(items);
    }

    // --- FilmSequelOrPrequel Converters ---
    @TypeConverter
    public static List<FilmSequelOrPrequel> fromStringToFilmSequelOrPrequelList(String data) {
        if (data == null) {
            return null;
        }
        FilmSequelOrPrequel[] items = gson.fromJson(data, FilmSequelOrPrequel[].class);
        return items != null ? Arrays.asList(items) : null;
    }

    @TypeConverter
    public static String fromFilmSequelOrPrequelList(List<FilmSequelOrPrequel> items) {
        return gson.toJson(items);
    }

    // --- SearchResultFilm Converters ---
    @TypeConverter
    public static List<SearchResultFilm> fromStringToSearchResultFilmList(String data) {
        if (data == null) {
            return null;
        }
        SearchResultFilm[] items = gson.fromJson(data, SearchResultFilm[].class);
        return items != null ? Arrays.asList(items) : null;
    }

    @TypeConverter
    public static String fromSearchResultFilmList(List<SearchResultFilm> items) {
        return gson.toJson(items);
    }

    // --- Staff Converters ---
    @TypeConverter
    public static List<Staff> fromStringToStaffList(String value) {
        if (value == null) {
            return null;
        }
        Staff[] items = new Gson().fromJson(value, Staff[].class);
        return items != null ? Arrays.asList(items) : null;
    }

    @TypeConverter
    public static String fromStaffList(List<Staff> list) {
        return gson.toJson(list);
    }

    // --- Person Converters ---
    @TypeConverter
    public static List<Person> fromStringToPersonList(String value) {
        if (value == null) {
            return null;
        }
        Person[] items = new Gson().fromJson(value, Person[].class);
        return items != null ? Arrays.asList(items) : null;
    }

    @TypeConverter
    public static String fromPersonList(List<Person> list) {
        return gson.toJson(list);
    }

    // --- AwardItem Converters ---
    @TypeConverter
    public static List<AwardItem> fromStringToAwardItemList(String value) {
        if (value == null) {
            return null;
        }
        AwardItem[] items = new Gson().fromJson(value, AwardItem[].class);
        return items != null ? Arrays.asList(items) : null;
    }

    @TypeConverter
    public static String fromAwardItemList(List<AwardItem> list) {
        return gson.toJson(list);
    }

    // --- FactItem Converters ---
    @TypeConverter
    public static List<FactItem> fromStringToFactItemList(String value) {
        if (value == null) {
            return null;
        }
        FactItem[] items = new Gson().fromJson(value, FactItem[].class);
        return items != null ? Arrays.asList(items) : null;
    }

    @TypeConverter
    public static String fromFactItemList(List<FactItem> list) {
        return gson.toJson(list);
    }
}
