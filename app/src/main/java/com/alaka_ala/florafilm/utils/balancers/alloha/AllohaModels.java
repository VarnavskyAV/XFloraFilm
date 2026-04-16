package com.alaka_ala.florafilm.utils.balancers.alloha;

import java.util.List;

public final class AllohaModels {
    private AllohaModels() {
    }

    public static final class TranslationInfo {
        public final String id;
        public final String name;
        public final String iframeUrl;

        public TranslationInfo(String id, String name, String iframeUrl) {
            this.id = id;
            this.name = name;
            this.iframeUrl = iframeUrl;
        }
    }

    public static final class EpisodeInfo {
        public final String num;
        public final List<TranslationInfo> translations;

        public EpisodeInfo(String num, List<TranslationInfo> translations) {
            this.num = num;
            this.translations = translations;
        }
    }

    public static final class SeasonInfo {
        public final String num;
        public final List<EpisodeInfo> episodes;

        public SeasonInfo(String num, List<EpisodeInfo> episodes) {
            this.num = num;
            this.episodes = episodes;
        }
    }

    public static final class AllohaApiResult {
        public final String title;
        public final boolean isSerial;
        public final String movieIframe;
        public final List<SeasonInfo> seasons;

        public AllohaApiResult(String title, boolean isSerial, String movieIframe, List<SeasonInfo> seasons) {
            this.title = title;
            this.isSerial = isSerial;
            this.movieIframe = movieIframe;
            this.seasons = seasons;
        }
    }
}
