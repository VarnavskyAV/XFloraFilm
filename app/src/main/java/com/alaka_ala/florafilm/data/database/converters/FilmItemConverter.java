package com.alaka_ala.florafilm.data.database.converters;

import androidx.room.TypeConverter;

import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class FilmItemConverter {

    @TypeConverter
    public static String fromFilmItemList(List<FilmItem> filmItems) {
        if (filmItems == null) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<FilmItem>>() {}.getType();
        return gson.toJson(filmItems, type);
    }

    @TypeConverter
    public static List<FilmItem> toFilmItemList(String filmItemsString) {
        if (filmItemsString == null) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<FilmItem>>() {}.getType();
        return gson.fromJson(filmItemsString, type);
    }
}
