package com.alaka_ala.unofficial_kinopoisk_api.models;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Keep
@Entity(
        tableName = "watch_position",
        indices = {@Index("kinopoiskId")}
)
public class WatchPosition {

    @PrimaryKey
    @NonNull
    private String idPosition;      // "12345" для фильма, "12345:s1:e4" для серии

    private int kinopoiskId;
    private String balancer;        // "HDVB" / "ALLOHA"
    private long position;          // мс

    // Для фильма = -1
    private int lastSeason;
    private int lastEpisode;

    // Чтобы кнопка Resume собрала полный путь
    private int voice;
    private int quality;

    private long updatedAt;

    public WatchPosition(@NonNull String idPosition, int kinopoiskId, String balancer,
                         long position, int lastSeason, int lastEpisode,
                         int voice, int quality, long updatedAt) {
        this.idPosition = idPosition;
        this.kinopoiskId = kinopoiskId;
        this.balancer = balancer;
        this.position = position;
        this.lastSeason = lastSeason;
        this.lastEpisode = lastEpisode;
        this.voice = voice;
        this.quality = quality;
        this.updatedAt = updatedAt;
    }

    @NonNull public String getIdPosition() { return idPosition; }
    public int getKinopoiskId() { return kinopoiskId; }
    public String getBalancer() { return balancer; }
    public long getPosition() { return position; }
    public int getLastSeason() { return lastSeason; }
    public int getLastEpisode() { return lastEpisode; }
    public int getVoice() { return voice; }
    public int getQuality() { return quality; }
    public long getUpdatedAt() { return updatedAt; }

    public void setPosition(long position) { this.position = position; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}