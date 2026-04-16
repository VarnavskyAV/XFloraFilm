package com.alaka_ala.florafilm.utils.balancers.alloha.models.serial;


import java.util.Map;

// Основной класс с данными сериала
public class SeriesData {
    private String name;
    private String original_name;
    private String alternative_name;
    private int year;
    private int category;
    private long id_kp;
    private String alternative_id_kp;
    private String id_imdb;
    private Long id_tmdb;  // может быть null
    private String id_world_art;
    private String token_movie;
    private String country;
    private String genre;
    private String actors;
    private String directors;
    private String producers;
    private String premiere_ru;
    private String premiere;
    private int age_restrictions;
    private String rating_mpaa;
    private Double rating_kp;  // может быть null
    private Double rating_imdb;  // может быть null
    private String time;
    private String tagline;
    private String poster;
    private String description;
    private int seasons_count;
    private Map<String, Season> seasons;
    private int last_episode;
    private String quality;
    private String translation;
    private Map<String, TranslationIframe> translation_iframe;
    private String iframe;
    private String iframe_trailer;
    private boolean lgbt;
    private boolean uhd;
    private boolean available_directors_cut;

    public SeriesData() {}

    // Геттеры и сеттеры
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOriginal_name() { return original_name; }
    public void setOriginal_name(String original_name) { this.original_name = original_name; }

    public String getAlternative_name() { return alternative_name; }
    public void setAlternative_name(String alternative_name) { this.alternative_name = alternative_name; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getCategory() { return category; }
    public void setCategory(int category) { this.category = category; }

    public long getId_kp() { return id_kp; }
    public void setId_kp(long id_kp) { this.id_kp = id_kp; }

    public String getAlternative_id_kp() { return alternative_id_kp; }
    public void setAlternative_id_kp(String alternative_id_kp) { this.alternative_id_kp = alternative_id_kp; }

    public String getId_imdb() { return id_imdb; }
    public void setId_imdb(String id_imdb) { this.id_imdb = id_imdb; }

    public Long getId_tmdb() { return id_tmdb; }
    public void setId_tmdb(Long id_tmdb) { this.id_tmdb = id_tmdb; }

    public String getId_world_art() { return id_world_art; }
    public void setId_world_art(String id_world_art) { this.id_world_art = id_world_art; }

    public String getToken_movie() { return token_movie; }
    public void setToken_movie(String token_movie) { this.token_movie = token_movie; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getActors() { return actors; }
    public void setActors(String actors) { this.actors = actors; }

    public String getDirectors() { return directors; }
    public void setDirectors(String directors) { this.directors = directors; }

    public String getProducers() { return producers; }
    public void setProducers(String producers) { this.producers = producers; }

    public String getPremiere_ru() { return premiere_ru; }
    public void setPremiere_ru(String premiere_ru) { this.premiere_ru = premiere_ru; }

    public String getPremiere() { return premiere; }
    public void setPremiere(String premiere) { this.premiere = premiere; }

    public int getAge_restrictions() { return age_restrictions; }
    public void setAge_restrictions(int age_restrictions) { this.age_restrictions = age_restrictions; }

    public String getRating_mpaa() { return rating_mpaa; }
    public void setRating_mpaa(String rating_mpaa) { this.rating_mpaa = rating_mpaa; }

    public Double getRating_kp() { return rating_kp; }
    public void setRating_kp(Double rating_kp) { this.rating_kp = rating_kp; }

    public Double getRating_imdb() { return rating_imdb; }
    public void setRating_imdb(Double rating_imdb) { this.rating_imdb = rating_imdb; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getSeasons_count() { return seasons_count; }
    public void setSeasons_count(int seasons_count) { this.seasons_count = seasons_count; }

    public Map<String, Season> getSeasons() { return seasons; }
    public void setSeasons(Map<String, Season> seasons) { this.seasons = seasons; }

    public int getLast_episode() { return last_episode; }
    public void setLast_episode(int last_episode) { this.last_episode = last_episode; }

    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }

    public String getTranslation() { return translation; }
    public void setTranslation(String translation) { this.translation = translation; }

    public Map<String, TranslationIframe> getTranslation_iframe() { return translation_iframe; }
    public void setTranslation_iframe(Map<String, TranslationIframe> translation_iframe) { this.translation_iframe = translation_iframe; }

    public String getIframe() { return iframe; }
    public void setIframe(String iframe) { this.iframe = iframe; }

    public String getIframe_trailer() { return iframe_trailer; }
    public void setIframe_trailer(String iframe_trailer) { this.iframe_trailer = iframe_trailer; }

    public boolean isLgbt() { return lgbt; }
    public void setLgbt(boolean lgbt) { this.lgbt = lgbt; }

    public boolean isUhd() { return uhd; }
    public void setUhd(boolean uhd) { this.uhd = uhd; }

    public boolean isAvailable_directors_cut() { return available_directors_cut; }
    public void setAvailable_directors_cut(boolean available_directors_cut) { this.available_directors_cut = available_directors_cut; }
}