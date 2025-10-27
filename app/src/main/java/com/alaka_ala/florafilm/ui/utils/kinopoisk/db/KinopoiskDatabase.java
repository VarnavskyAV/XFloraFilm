
package com.alaka_ala.florafilm.ui.utils.kinopoisk.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.*;

/**
 * The Room database for caching Kinopoisk API responses.
 */
@Database(entities = {FilmCollection.class, FilmCountryOrGenresResponse.class, FilmDetails.class, FilmSimilarResponse.class, FilmImagesResponse.class, FilmSequelsAndPrequelsResponse.class, FilmSearchResponse.class, Staff.class, StaffResponse.class, Person.class, PersonSearchResponse.class, FilmAwardsResponse.class, FilmFactsResponse.class}, version = 10, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class KinopoiskDatabase extends RoomDatabase {

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

    private static volatile KinopoiskDatabase INSTANCE;

    /**
     * Returns the singleton instance of the database.
     * @param context The context.
     * @return The singleton instance of the database.
     */
    public static KinopoiskDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (KinopoiskDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            KinopoiskDatabase.class, "kinopoisk_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
