package com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * Type converters to allow Room to reference complex data types.
 */
public class Converters {
    private static Gson gson = new Gson();

    // --- FilmItem Converters ---
    @TypeConverter
    public static List<FilmItem> fromStringToFilmItemList(String data) {
        if (data == null) return Collections.emptyList();
        Type listType = new TypeToken<List<FilmItem>>() {}.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String fromFilmItemList(List<FilmItem> filmItems) {
        return gson.toJson(filmItems);
    }

    // --- GenreResponse Converters ---
    @TypeConverter
    public static List<GenreResponse> fromStringToGenreResponseList(String data) {
        if (data == null) return Collections.emptyList();
        Type listType = new TypeToken<List<GenreResponse>>() {}.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String fromGenreResponseList(List<GenreResponse> genres) {
        return gson.toJson(genres);
    }

    // --- CountryResponse Converters ---
    @TypeConverter
    public static List<CountryResponse> fromStringToCountryResponseList(String data) {
        if (data == null) return Collections.emptyList();
        Type listType = new TypeToken<List<CountryResponse>>() {}.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String fromCountryResponseList(List<CountryResponse> countries) {
        return gson.toJson(countries);
    }

    // --- Genre Converters ---
    @TypeConverter
    public static List<Genre> fromStringToGenreList(String data) {
        if (data == null) return Collections.emptyList();
        Type listType = new TypeToken<List<Genre>>() {}.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String fromGenreList(List<Genre> genres) {
        return gson.toJson(genres);
    }

    // --- Country Converters ---
    @TypeConverter
    public static List<Country> fromStringToCountryList(String data) {
        if (data == null) return Collections.emptyList();
        Type listType = new TypeToken<List<Country>>() {}.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String fromCountryList(List<Country> countries) {
        return gson.toJson(countries);
    }

    // --- FilmSimilarItem Converters ---
    @TypeConverter
    public static List<FilmSimilarItem> fromStringToFilmSimilarItemList(String data) {
        if (data == null) {
            return null;
        }
        Type listType = new TypeToken<List<FilmSimilarItem>>() {}.getType();
        return gson.fromJson(data, listType);
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
        Type listType = new TypeToken<List<FilmImageItem>>() {}.getType();
        return gson.fromJson(data, listType);
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
        Type listType = new TypeToken<List<FilmSequelOrPrequel>>() {}.getType();
        return gson.fromJson(data, listType);
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
        Type listType = new TypeToken<List<SearchResultFilm>>() {}.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String fromSearchResultFilmList(List<SearchResultFilm> items) {
        return gson.toJson(items);
    }

    // --- Staff Converters ---
    @TypeConverter
    public static List<Staff> fromStringToStaffList(String value) {
        Type listType = new TypeToken<List<Staff>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromStaffList(List<Staff> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    // --- Person Converters ---
    @TypeConverter
    public static List<Person> fromStringToPersonList(String value) {
        Type listType = new TypeToken<List<Person>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromPersonList(List<Person> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    // --- AwardItem Converters ---
    @TypeConverter
    public static List<AwardItem> fromStringToAwardItemList(String value) {
        Type listType = new TypeToken<List<AwardItem>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromAwardItemList(List<AwardItem> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    // --- FactItem Converters ---
    @TypeConverter
    public static List<FactItem> fromStringToFactItemList(String value) {
        Type listType = new TypeToken<List<FactItem>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromFactItemList(List<FactItem> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }
}
