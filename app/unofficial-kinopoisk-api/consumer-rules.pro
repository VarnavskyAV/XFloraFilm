# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Сохраняем всё, что нужно для Gson и Room
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# Сохраняем Gson
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-dontwarn com.google.gson.**

# Сохраняем ВСЕ модели в твоем пакете
-keep class com.alaka_ala.unofficial_kinopoisk_api.models.** { *; }

# Сохраняем Converters и все его внутренние классы
-keep class com.alaka_ala.unofficial_kinopoisk_api.db.Converters { *; }
-keepclassmembers class com.alaka_ala.unofficial_kinopoisk_api.db.Converters {
    public static <methods>;
}

# Сохраняем все, что помечено @Keep
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Специально для Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Отключаем оптимизацию для Gson (жесткий способ)
# -dontobfuscate
# -dontoptimize