package com.alaka_ala.florafilm.ui.utils.kinopoisk.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmCollection;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmCountryOrGenresResponse;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmDetails;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmImagesResponse;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmSearchResponse;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmSequelsAndPrequelsResponse;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmSimilarResponse;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.Converters;

/**
 * The Room database for caching Kinopoisk API responses.
 */
@Database(entities = {FilmCollection.class, FilmCountryOrGenresResponse.class, FilmDetails.class, FilmSimilarResponse.class, FilmImagesResponse.class, FilmSequelsAndPrequelsResponse.class, FilmSearchResponse.class}, version = 5, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class KinopoiskDatabase extends RoomDatabase {

    public abstract FilmCollectionDao filmCollectionDao();
    public abstract FilmCountryOrGenresResponseDao filmCountryOrGenresResponseDao();
    public abstract FilmDetailsDao filmDetailsDao();
    public abstract FilmSimilarResponseDao filmSimilarResponseDao();
    public abstract FilmImagesResponseDao filmImagesResponseDao();
    public abstract FilmSequelsAndPrequelsResponseDao filmSequelsAndPrequelsResponseDao();
    public abstract FilmSearchResponseDao filmSearchResponseDao();

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
