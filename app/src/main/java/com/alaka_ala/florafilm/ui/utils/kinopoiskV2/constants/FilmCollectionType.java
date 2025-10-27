package com.alaka_ala.florafilm.ui.utils.kinopoiskV2.constants;

/**
 * Перечисление доступных типов коллекций фильмов.
 */
public enum FilmCollectionType {
    TOP_POPULAR_ALL("TOP_POPULAR_ALL"),
    TOP_POPULAR_MOVIES("TOP_POPULAR_MOVIES"),
    TOP_250_TV_SHOWS("TOP_250_TV_SHOWS"),
    TOP_250_MOVIES("TOP_250_MOVIES"),
    VAMPIRE_THEME("VAMPIRE_THEME"),
    COMICS_THEME("COMICS_THEME"),
    CLOSES_RELEASES("CLOSES_RELEASES"),
    FAMILY("FAMILY"),
    OSKAR_WINNERS_2021("OSKAR_WINNERS_2021"),
    LOVE_THEME("LOVE_THEME"),
    ZOMBIE_THEME("ZOMBIE_THEME"),
    CATASTROPHE_THEME("CATASTROPHE_THEME"),
    KIDS_ANIMATION_THEME("KIDS_ANIMATION_THEME"),
    POPULAR_SERIES("POPULAR_SERIES");

    private final String typeName;

    FilmCollectionType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}


