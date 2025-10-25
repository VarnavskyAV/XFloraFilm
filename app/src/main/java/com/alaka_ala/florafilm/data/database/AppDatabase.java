package com.alaka_ala.florafilm.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.alaka_ala.florafilm.data.database.converters.FilmItemConverter;
import com.alaka_ala.florafilm.data.database.dao.FilmCollectionDao;
import com.alaka_ala.florafilm.data.database.entities.FilmCollectionEntity;

@Database(entities = {FilmCollectionEntity.class}, version = 1, exportSchema = false)
@TypeConverters({FilmItemConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract FilmCollectionDao filmCollectionDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "film_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
