package com.alaka_ala.florafilm.utils.settings;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.alaka_ala.florafilm.fragments.settings.SettingsFragment;

public class AppPreferences {
    private static final String preferencesName = "AppPreferences";

    /**
     * Для настроек сервисов предоставляемых видео-контент
     */
    public static class CDNSettings {

        public static class HDVB {
            private static final boolean cdn_active_default = true; // Def value
            private static final String key_cdn_param = "cdn_hdvb";


            /**
             * Получает пользовательский параметр на включение балансера HDVB <br> используется тут -> {@link SettingsFragment}
             */
            public static boolean isHDVBActive(Context context) {
                return getSharedPreferences(context).getBoolean(key_cdn_param, cdn_active_default);
            }

            /**
             * Отключает балансер HDVB <br> используется тут -> {@link SettingsFragment}
             */
            public static void disableHDVB(Context context) {
                getSharedPreferences(context).edit().putBoolean(key_cdn_param, true).apply();
            }

            /**
             * Включает балансер HDVB <br> используется тут -> {@link SettingsFragment}
             */
            public static void enableHDVB(Context context) {
                getSharedPreferences(context).edit().putBoolean(key_cdn_param, true).apply();
            }
        }

        public static class Alloha {
            private static final boolean cdn_active_default = true; // default
            private static final String key_cdn_param = "cdn_alloha";

            /**
             * Получает пользовательский параметр на включение балансера ALLOHA
             * <br> используется тут -> {@link SettingsFragment}
             */
            public static boolean isAllohaActive(Context context) {
                return getSharedPreferences(context).getBoolean(key_cdn_param, cdn_active_default);
            }

            /**
             * Отключает балансер ALLOHA <br> используется тут -> {@link SettingsFragment}
             */
            public static void disableAlloha(Context context) {
                getSharedPreferences(context).edit().putBoolean(key_cdn_param, false).apply();
            }

            /**
             * Включает балансер ALLOHA <br> используется тут -> {@link SettingsFragment}
             */
            public static void enableAlloha(Context context) {
                getSharedPreferences(context).edit().putBoolean(key_cdn_param, true).apply();
            }

        }

    }

    public static class PlayerSettings {

        public static class GestureListenerSettings {
            private static final boolean active_default = false; // Def value
            private static final String key_param = "gesture_listener";

            /**
             * Упрощеный метод для Switch.
             */
            public static void setStatGestureListener(Context context, boolean isActive) {
                getSharedPreferences(context).edit().putBoolean(key_param, isActive).apply();
            }

            public static boolean onIsGestureListener(Context context) {
                return getSharedPreferences(context).getBoolean(key_param, active_default);
            }

            /**
             * Метод включает жесты в плеере
             */
            public static void setOnGestureListener(Context context) {
                getSharedPreferences(context).edit().putBoolean(key_param, true).apply();
            }

            /**
             * Метод отключает жесчты в плеере
             */
            public static void setOffGestureListener(Context context) {
                getSharedPreferences(context).edit().putBoolean(key_param, false).apply();
            }

            /**
             * Метод инвертирует текущее сохраненное значение.
             * Удобно в использовании кнопки без лишних написаний условий if и
             * возвращает уже инвертированное значение которое было сохранено в параметры
             */
            public static boolean setInversiveCurrentStatsGestureListener(Context context) {
                SharedPreferences preferences = getSharedPreferences(context);
                boolean currentState = preferences.getBoolean(key_param, active_default);
                preferences.edit().putBoolean(key_param, !currentState).apply();
                return !currentState;
            }

        }

        public static class PlayerButtonsControlSettings {
            private static String key_param_forward = "player_buttons_control_settings_forward";
            private static String key_param_rewind = "player_buttons_control_settings_rewind";
            private static boolean def_active_forward_button = false;
            private static boolean def_active_rewind_button = false;

            /**
             * Метод включения кнопки быстрой перемотки вперед
             */
            public static void setOnActiveButtonFastForward(Context context, boolean active) {
                getSharedPreferences(context).edit().putBoolean(key_param_forward, active).apply();
            }

            /**
             * Метод включения кнопки быстрой перемотки назад
             */
            public static void setOnActiveButtonFastRewind(Context context, boolean active) {
                getSharedPreferences(context).edit().putBoolean(key_param_rewind, active).apply();
            }

            /**
             * Метод получает пользовательский параметр, на то, включена ли кнопка в настройках. True - да, False нет
             */
            public static boolean isOnActiveButtonFastForward(Context context) {
                return getSharedPreferences(context).getBoolean(key_param_forward, def_active_forward_button);
            }

            /**
             * Метод получает пользовательский параметр, на то, включена ли кнопка в настройках. True - да, False нет
             */
            public static boolean isOnActiveButtonFastRewind(Context context) {
                return getSharedPreferences(context).getBoolean(key_param_rewind, def_active_rewind_button);
            }
        }

    }


    /**
     * Класс предназначен для настроек на странице активности
     */
    public static class ActivityListViewAdapters {
        private static final boolean DEF_VALUE_ACTIVITY_LIST_VIEW_ADAPTERS_ENABLED = true; //Def Value // По умолчанию показываем списки

        private static final String key_activity_list_view_adapter_enabled = "activity-list-view-adapter";

        /**
         * Устанавливает видимость адаптера
         *
         * @param nameListFilter Указываются список фильтра на что нужно отключить список.
         *                       К примеру: закладки (bookmark), продолжить просмотр (resumeView) и т.д.
         *                       используй enum {@link ListNames}
         * @param visible        true - видимость списка адаптера будет включена, False напротив.
         * @param ctx            Контекст приложения. Нужен, что бы получить SharedPreferences экземпляр класса
         * @- Доступные параметры listName ( bookmark, resumeView, historyView )
         */
        public static void setVisibleAdapter(ListNames nameListFilter, Context ctx, boolean visible) {
            SharedPreferences prefs = ctx.getSharedPreferences(preferencesName, MODE_PRIVATE);
            prefs.edit().putBoolean(nameListFilter + "_" + key_activity_list_view_adapter_enabled, visible).apply();
        }

        public static Boolean getVisibleAdapter(ListNames nameListFilter, Context ctx) {
            SharedPreferences prefs = ctx.getSharedPreferences(preferencesName, MODE_PRIVATE);
            return prefs.getBoolean(nameListFilter + "_" + key_activity_list_view_adapter_enabled, DEF_VALUE_ACTIVITY_LIST_VIEW_ADAPTERS_ENABLED);
        }


        /**
         * Список ключей названий категорий на экране "Активность"
         *
         * @- BOOKMARK
         * @- RESUMEVIEW
         * @- HISTORYVIEW
         */
        public enum ListNames {
            BOOKMARK,
            RESUMEVIEW,
            HISTORYVIEW;
        }


    }

    private static SharedPreferences getSharedPreferences(Context context) {
        if (context == null) return null;
        return context.getSharedPreferences(preferencesName, MODE_PRIVATE);
    }

}
