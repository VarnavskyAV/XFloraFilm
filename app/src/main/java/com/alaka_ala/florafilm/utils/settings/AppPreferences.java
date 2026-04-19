package com.alaka_ala.florafilm.utils.settings;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {
    private static final String preferencesName = "AppPreferences";

    /**Для настроек сервисов предоставляемых видео-контент*/
    public static class CDNSettings {

        public static class HDVB {
            private static final boolean cdn_active_default = true; // Def value
            private static final String key_cdn_param = "cdn_hdvb";


            /**Получает пользовательский параметр на включение балансера HDVB <br> используется тут -> {@link com.alaka_ala.florafilm.ui.fragments.settings.SettingsFragment}*/
            public static boolean isHDVBActive(Context context) {
                SharedPreferences preferences = context.getSharedPreferences(preferencesName, MODE_PRIVATE);
                return preferences.getBoolean(key_cdn_param, cdn_active_default);
            }

            /**Отключает балансер HDVB <br> используется тут -> {@link com.alaka_ala.florafilm.ui.fragments.settings.SettingsFragment}*/
            public static void disableHDVB(Context context) {
                SharedPreferences preferences = context.getSharedPreferences(preferencesName, MODE_PRIVATE);
                preferences.edit().putBoolean(key_cdn_param, false).apply();
            }

            /**Включает балансер HDVB <br> используется тут -> {@link com.alaka_ala.florafilm.ui.fragments.settings.SettingsFragment}*/
            public static void enableHDVB(Context context) {
                SharedPreferences preferences = context.getSharedPreferences(preferencesName, MODE_PRIVATE);
                preferences.edit().putBoolean(key_cdn_param, true).apply();
            }
        }

        public static class Alloha {
            private static final boolean cdn_active_default = true; // default
            private static final String key_cdn_param = "cdn_alloha";

            /**Получает пользовательский параметр на включение балансера ALLOHA
             * <br> используется тут -> {@link com.alaka_ala.florafilm.ui.fragments.settings.SettingsFragment}*/
            public static boolean isAllohaActive(Context context) {
                SharedPreferences preferences = context.getSharedPreferences(preferencesName, MODE_PRIVATE);
                return preferences.getBoolean(key_cdn_param, cdn_active_default);
            }
            /**Отключает балансер ALLOHA <br> используется тут -> {@link com.alaka_ala.florafilm.ui.fragments.settings.SettingsFragment}*/
            public static void disableAlloha(Context context) {
                SharedPreferences preferences = context.getSharedPreferences(preferencesName, MODE_PRIVATE);
                preferences.edit().putBoolean(key_cdn_param, false).apply();
            }
            /**Включает балансер ALLOHA <br> используется тут -> {@link com.alaka_ala.florafilm.ui.fragments.settings.SettingsFragment}*/
            public static void enableAlloha(Context context) {
                SharedPreferences preferences = context.getSharedPreferences(preferencesName, MODE_PRIVATE);
                preferences.edit().putBoolean(key_cdn_param, true).apply();
            }

        }

    }


    /**Класс предназначен для настроек на странице активности */
    public static class ActivityListViewAdapters {
        private static final boolean DEF_VALUE_ACTIVITY_LIST_VIEW_ADAPTERS_ENABLED = true; //Def Value // По умолчанию показываем списки

        private static final String key_activity_list_view_adapter_enabled = "activity-list-view-adapter";

        /**Устанавливает видимость адаптера
         * @param nameListFilter Указываются список фильтра на что нужно отключить список.
         *                       К примеру: закладки (bookmark), продолжить просмотр (resumeView) и т.д.
         *                       используй enum {@link ListNames}
         * @param visible true - видимость списка адаптера будет включена, False напротив.
         * @param ctx Контекст приложения. Нужен, что бы получить SharedPreferences экземпляр класса
         * @-  Доступные параметры listName ( bookmark, resumeView, historyView )*/
        public static void setVisibleAdapter(ListNames nameListFilter, Context ctx, boolean visible) {
            SharedPreferences prefs = ctx.getSharedPreferences(preferencesName, MODE_PRIVATE);
            prefs.edit().putBoolean(nameListFilter + "_" + key_activity_list_view_adapter_enabled, visible ).apply();
        }

        public static Boolean getVisibleAdapter(ListNames nameListFilter, Context ctx) {
            SharedPreferences prefs = ctx.getSharedPreferences(preferencesName, MODE_PRIVATE);
            return prefs.getBoolean(nameListFilter + "_" + key_activity_list_view_adapter_enabled, DEF_VALUE_ACTIVITY_LIST_VIEW_ADAPTERS_ENABLED);
        }


        /**Список ключей названий категорий на экране "Активность"
         * @- BOOKMARK
         * @- RESUMEVIEW
         * @- HISTORYVIEW*/
        public enum ListNames {
            BOOKMARK,
            RESUMEVIEW,
            HISTORYVIEW;
        }


    }


}
