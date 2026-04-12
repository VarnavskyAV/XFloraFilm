package com.alaka_ala.florafilm.utils.preferences;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {
    private static final String preferencesName = "AppPreferences";


    public static class ViewTorrent {
        private static final boolean viewTorrent = false; // Def value
        private static final String key_param_view_torrent = "viewTorrent";


        public static boolean isViewTorrent(Context context) {
            SharedPreferences preferences = context.getSharedPreferences(preferencesName, MODE_PRIVATE);
            return preferences.getBoolean(key_param_view_torrent, viewTorrent);
        }

        /**
         * Вкл/Отключить использование торрентов для просмотра
         */
        public static void setViewTorrent(Context context, boolean isViewTorrent) {
            SharedPreferences preferences = context.getSharedPreferences(preferencesName, MODE_PRIVATE);
            preferences.edit().putBoolean(key_param_view_torrent, isViewTorrent).apply();
        }


    }

    public static class CDNSettings {
        public static class HDVB {
            private static final boolean cdn_hdvb_active_default = true; // Def value
            private static final String key_param_cdn = "cdn_hdvb";

            // HDVB
            public static boolean isHDVBActive(Context context) {
                SharedPreferences preferences = context.getSharedPreferences(preferencesName, MODE_PRIVATE);
                return preferences.getBoolean(key_param_cdn, cdn_hdvb_active_default);
            }

            public static void disableHDVB(Context context) {
                SharedPreferences preferences = context.getSharedPreferences(preferencesName, MODE_PRIVATE);
                preferences.edit().putBoolean(key_param_cdn, false).apply();
            }

            public static void enableHDVB(Context context) {
                SharedPreferences preferences = context.getSharedPreferences(preferencesName, MODE_PRIVATE);
                preferences.edit().putBoolean(key_param_cdn, true).apply();
            }
        }
    }


}
