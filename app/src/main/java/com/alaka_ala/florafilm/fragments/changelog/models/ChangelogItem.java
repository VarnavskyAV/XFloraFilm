// ChangelogItem.java
package com.alaka_ala.florafilm.fragments.changelog.models;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.graphics.Typeface;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Упрощённая модель для ChangelogFragment.
 * <p>
 * Форматирует текст с поддержкой:
 * <ul>
 *   <li># Заголовки — акцентный цвет, жирный</li>
 *   <li>Все тире/минусы заменяются на ⚪ для единообразия</li>
 *   <li>Остальной текст — нейтральный цвет</li>
 * </ul>
 * Используется максимум 2 цвета.
 * </p>
 * @author Alaka_Ala
 */
public class ChangelogItem {
    private final String version;
    private final String date;
    private final String description;

    // Цвета: акцент для заголовков, нейтральный для текста
    private static final int COLOR_ACCENT = 0xFF2196F3;  // синий для # заголовков
    private static final int COLOR_TEXT = 0xFF888888;    // серый для остального

    // Паттерн для поиска всех видов тире и минусов
    private static final Pattern DASH_PATTERN = Pattern.compile("[-–—‑‐−]");

    public ChangelogItem(String version, String date, String description) {
        this.version = version;
        this.date = date;
        // Сразу нормализуем тире → ⚪
        this.description = DASH_PATTERN.matcher(description).replaceAll("⚪");
    }

    public SpannableStringBuilder getFormattedDescription(Context context) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String[] lines = description.split("\n");

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                builder.append("\n");
                continue;
            }

            int start = builder.length();

            // Обработка заголовков: #, ##, ###
            if (trimmed.matches("^#{1,3}\\s.*")) {
                String headerText = trimmed.replaceAll("^#{1,3}\\s+", "");
                builder.append(headerText).append("\n");
                int end = builder.length();

                // Жирный + акцентный цвет только для заголовков
                builder.setSpan(new StyleSpan(Typeface.BOLD), start, end - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new ForegroundColorSpan(COLOR_ACCENT), start, end - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            } else {
                // Обычный текст: нейтральный цвет, без дополнительного форматирования
                builder.append(trimmed).append("\n");
                int end = builder.length();
                // При необходимости можно раскомментировать для единого цвета всего текста:
                // builder.setSpan(new ForegroundColorSpan(COLOR_TEXT), start, end - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        return builder;
    }

    // Геттеры
    public String getVersion() { return version; }
    public String getDate() { return date; }
    public String getDescription() { return description; }
}