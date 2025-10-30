package com.alaka_ala.florafilm.ui.utils.kinopoiskV2.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models.*;

/**
 * The Room database for caching Kinopoisk API responses V2.
 * Optimized version with extracted nested classes and improved structure.
 */
@Database(entities = {
        FilmCollection.class,
        FilmCountryOrGenresResponse.class,
        FilmDetails.class,
        FilmSimilarResponse.class,
        FilmImagesResponse.class,
        FilmSequelsAndPrequelsResponse.class,
        FilmSearchResponse.class,
        Staff.class,
        StaffResponse.class,
        Person.class,
        PersonSearchResponse.class,
        FilmAwardsResponse.class,
        FilmFactsResponse.class
}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class KinopoiskDatabaseV2 extends RoomDatabase {

    public abstract FilmCollectionDao filmCollectionDao();

    public abstract FilmCountryOrGenresResponseDao filmCountryOrGenresResponseDao();

    public abstract FilmDetailsDao filmDetailsDao();

    public abstract FilmSimilarResponseDao filmSimilarResponseDao();

    public abstract FilmImagesResponseDao filmImagesResponseDao();

    public abstract FilmSequelsAndPrequelsResponseDao filmSequelsAndPrequelsResponseDao();

    public abstract FilmSearchResponseDao filmSearchResponseDao();

    public abstract StaffDao staffDao();

    public abstract PersonSearchResponseDao personSearchResponseDao();

    public abstract FilmAwardsResponseDao filmAwardsResponseDao();

    public abstract FilmFactsResponseDao filmFactsResponseDao();

    private static volatile KinopoiskDatabaseV2 INSTANCE;

    /**
     * Returns the singleton instance of the database V2.
     *
     * @param context The context.
     * @return The singleton instance of the database V2.
     */
    public static KinopoiskDatabaseV2 getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (KinopoiskDatabaseV2.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    KinopoiskDatabaseV2.class, "kinopoisk_database_v2")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
