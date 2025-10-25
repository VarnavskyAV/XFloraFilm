package com.alaka_ala.florafilm.ui.utils.kinopoisk.constants;

/**
 * Константы для работы с Kinopoisk API
 */
public class Constants {

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

    /**
     * Константы жанров
     */
    public static class Genres {
        public static final int THRILLER = 1;
        public static final int DRAMA = 2;
        public static final int CRIME = 3;
        public static final int ROMANCE = 4;
        public static final int DETECTIVE = 5;
        public static final int SCI_FI = 6;
        public static final int ADVENTURE = 7;
        public static final int BIOGRAPHY = 8;
        public static final int FILM_NOIR = 9;
        public static final int WESTERN = 10;
        public static final int ACTION = 11;
        public static final int FANTASY = 12;
        public static final int COMEDY = 13;
        public static final int WAR = 14;
        public static final int HISTORY = 15;
        public static final int MUSIC = 16;
        public static final int HORROR = 17;
        public static final int ANIMATION = 18;
        public static final int FAMILY = 19;
        public static final int MUSICAL = 20;
        public static final int SPORT = 21;
        public static final int DOCUMENTARY = 22;
        public static final int SHORT_FILM = 23;
        public static final int ANIME = 24;
        public static final int EMPTY = 25;
        public static final int NEWS = 26;
        public static final int CONCERT = 27;
        public static final int ADULT = 28;
        public static final int CEREMONY = 29;
        public static final int REALITY_TV = 30;
        public static final int GAME = 31;
        public static final int TALK_SHOW = 32;
        public static final int KIDS = 33;
    }
    
    /**
     * Константы стран
     */
    public static class Countries {
        public static final int USA = 1;
        public static final int RUSSIA = 34;
        public static final int UK = 5;
        public static final int FRANCE = 3;
        public static final int GERMANY = 9;
        public static final int ITALY = 10;
        public static final int SPAIN = 8;
        public static final int JAPAN = 16;
        public static final int CHINA = 21;
        public static final int SOUTH_KOREA = 49;
        public static final int INDIA = 7;
        public static final int AUSTRALIA = 13;
        public static final int CANADA = 14;
        public static final int BRAZIL = 30;
        public static final int MEXICO = 15;
        public static final int ARGENTINA = 24;
        public static final int UKRAINE = 106;
        public static final int BELARUS = 128;
        public static final int KAZAKHSTAN = 67;
        public static final int UZBEKISTAN = 119;
        public static final int GEORGIA = 111;
        public static final int ARMENIA = 105;
        public static final int AZERBAIJAN = 130;
        public static final int MOLDOVA = 129;
        public static final int LITHUANIA = 87;
        public static final int LATVIA = 104;
        public static final int ESTONIA = 98;
        public static final int POLAND = 4;
        public static final int CZECH_REPUBLIC = 18;
        public static final int SLOVAKIA = 72;
        public static final int HUNGARY = 48;
        public static final int ROMANIA = 37;
        public static final int BULGARIA = 51;
        public static final int CROATIA = 38;
        public static final int SERBIA = 121;
        public static final int SLOVENIA = 89;
        public static final int BOSNIA = 26;
        public static final int MONTENEGRO = 170;
        public static final int MACEDONIA = 101;
        public static final int ALBANIA = 116;
        public static final int GREECE = 63;
        public static final int TURKEY = 44;
        public static final int ISRAEL = 42;
        public static final int IRAN = 59;
        public static final int IRAQ = 96;
        public static final int SAUDI_ARABIA = 81;
        public static final int UAE = 99;
        public static final int QATAR = 192;
        public static final int KUWAIT = 85;
        public static final int BAHRAIN = 173;
        public static final int OMAN = 193;
        public static final int JORDAN = 103;
        public static final int LEBANON = 84;
        public static final int SYRIA = 107;
        public static final int PALESTINE = 131;
        public static final int EGYPT = 86;
        public static final int LIBYA = 83;
        public static final int TUNISIA = 64;
        public static final int ALGERIA = 69;
        public static final int MOROCCO = 55;
        public static final int SUDAN = 175;
        public static final int ETHIOPIA = 176;
        public static final int KENYA = 57;
        public static final int TANZANIA = 82;
        public static final int UGANDA = 184;
        public static final int RWANDA = 146;
        public static final int BURUNDI = 171;
        public static final int MALAWI = 211;
        public static final int ZAMBIA = 115;
        public static final int ZIMBABWE = 78;
        public static final int BOTSWANA = 150;
        public static final int NAMIBIA = 43;
        public static final int SOUTH_AFRICA = 39;
        public static final int LESOTHO = 183;
        public static final int SWAZILAND = 205;
        public static final int MADAGASCAR = 152;
        public static final int MAURITIUS = 180;
        public static final int SEYCHELLES = 228;
        public static final int COMOROS = 213;
        public static final int DJIBOUTI = 239;
        public static final int ERITREA = 197;
        public static final int SOMALIA = 198;
        public static final int ETHIOPIA_OLD = 176;
        public static final int SUDAN_OLD = 175;
        public static final int CHAD = 154;
        public static final int NIGER = 166;
        public static final int NIGERIA = 109;
        public static final int CAMEROON = 117;
        public static final int CENTRAL_AFRICAN_REPUBLIC = 217;
        public static final int CONGO = 167;
        public static final int DEMOCRATIC_REPUBLIC_OF_CONGO = 125;
        public static final int GABON = 114;
        public static final int EQUATORIAL_GUINEA = 212;
        public static final int SAO_TOME_AND_PRINCIPE = 65870322;
        public static final int ANGOLA = 136;
        public static final int ZAIRE = 161;
        public static final int BURKINA_FASO = 118;
        public static final int MALI = 155;
        public static final int SENEGAL = 112;
        public static final int GAMBIA = 220;
        public static final int GUINEA_BISSAU = 142;
        public static final int GUINEA = 102;
        public static final int SIERRA_LEONE = 196;
        public static final int LIBERIA = 182;
        public static final int IVORY_COAST = 148;
        public static final int GHANA = 122;
        public static final int TOGO = 168;
        public static final int BENIN = 139;
        public static final int NIGER_OLD = 166;
        public static final int CHAD_OLD = 154;
        public static final int SUDAN_OLD2 = 175;
        public static final int ETHIOPIA_OLD2 = 176;
        public static final int ERITREA_OLD = 197;
        public static final int SOMALIA_OLD = 198;
        public static final int DJIBOUTI_OLD = 239;
        public static final int KENYA_OLD = 57;
        public static final int TANZANIA_OLD = 82;
        public static final int UGANDA_OLD = 184;
        public static final int RWANDA_OLD = 146;
        public static final int BURUNDI_OLD = 171;
        public static final int MALAWI_OLD = 211;
        public static final int ZAMBIA_OLD = 115;
        public static final int ZIMBABWE_OLD = 78;
        public static final int BOTSWANA_OLD = 150;
        public static final int NAMIBIA_OLD = 43;
        public static final int SOUTH_AFRICA_OLD = 39;
        public static final int LESOTHO_OLD = 183;
        public static final int SWAZILAND_OLD = 205;
        public static final int MADAGASCAR_OLD = 152;
        public static final int MAURITIUS_OLD = 180;
        public static final int SEYCHELLES_OLD = 228;
        public static final int COMOROS_OLD = 213;
        public static final int DJIBOUTI_OLD2 = 239;
        public static final int ERITREA_OLD2 = 197;
        public static final int SOMALIA_OLD2 = 198;
        public static final int ETHIOPIA_OLD3 = 176;
        public static final int SUDAN_OLD3 = 175;
        public static final int CHAD_OLD2 = 154;
        public static final int NIGER_OLD2 = 166;
        public static final int NIGERIA_OLD = 109;
        public static final int CAMEROON_OLD = 117;
        public static final int CENTRAL_AFRICAN_REPUBLIC_OLD = 217;
        public static final int CONGO_OLD = 167;
        public static final int DEMOCRATIC_REPUBLIC_OF_CONGO_OLD = 125;
        public static final int GABON_OLD = 114;
        public static final int EQUATORIAL_GUINEA_OLD = 212;
        public static final int SAO_TOME_AND_PRINCIPE_OLD = 65870322;
        public static final int ANGOLA_OLD = 136;
        public static final int ZAIRE_OLD = 161;
        public static final int BURKINA_FASO_OLD = 118;
        public static final int MALI_OLD = 155;
        public static final int SENEGAL_OLD = 112;
        public static final int GAMBIA_OLD = 220;
        public static final int GUINEA_BISSAU_OLD = 142;
        public static final int GUINEA_OLD = 102;
        public static final int SIERRA_LEONE_OLD = 196;
        public static final int LIBERIA_OLD = 182;
        public static final int IVORY_COAST_OLD = 148;
        public static final int GHANA_OLD = 122;
        public static final int TOGO_OLD = 168;
        public static final int BENIN_OLD = 139;
    }
    
    /**
     * Константы типов изображений
     */
    public static class ImageTypes {
        public static final String STILL = "STILL";
        public static final String SHOOTING = "SHOOTING";
        public static final String POSTER = "POSTER";
        public static final String FAN_ART = "FAN_ART";
        public static final String PROMO = "PROMO";
        public static final String CONCEPT = "CONCEPT";
        public static final String WALLPAPER = "WALLPAPER";
        public static final String COVER = "COVER";
        public static final String SCREENSHOT = "SCREENSHOT";
    }
    
    /**
     * Константы типов фильмов
     */
    public static class FilmTypes {
        public static final String FILM = "FILM";
        public static final String TV_SERIES = "TV_SERIES";
        public static final String TV_SHOW = "TV_SHOW";
        public static final String MINI_SERIES = "MINI_SERIES";
        public static final String VIDEO = "VIDEO";
    }
    
    /**
     * Константы статусов производства
     */
    public static class ProductionStatus {
        public static final String FILMED = "FILMED";
        public static final String PRE_PRODUCTION = "PRE_PRODUCTION";
        public static final String IN_PRODUCTION = "IN_PRODUCTION";
        public static final String POST_PRODUCTION = "POST_PRODUCTION";
        public static final String COMPLETED = "COMPLETED";
        public static final String ANNOUNCED = "ANNOUNCED";
        public static final String UNKNOWN = "UNKNOWN";
    }
}
