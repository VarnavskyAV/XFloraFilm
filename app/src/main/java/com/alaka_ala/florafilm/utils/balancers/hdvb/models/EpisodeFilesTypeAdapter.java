package com.alaka_ala.florafilm.utils.balancers.hdvb.models;

import androidx.annotation.Keep;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.JsonToken;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Keep
public class EpisodeFilesTypeAdapter extends TypeAdapter<List<Object>> {

    @Override
    public void write(JsonWriter out, List<Object> value) throws IOException {
        // Не нужно для чтения
    }

    @Override
    public List<Object> read(JsonReader in) throws IOException {
        List<Object> result = new ArrayList<>();

        if (in.peek() == JsonToken.BEGIN_ARRAY) {
            in.beginArray();

            while (in.hasNext()) {
                JsonToken token = in.peek();

                if (token == JsonToken.BEGIN_OBJECT) {
                    // Парсим объект TranslationResponse
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    TranslationResponse translation = gson.fromJson(in, TranslationResponse.class);
                    result.add(translation);
                } else if (token == JsonToken.BEGIN_ARRAY) {
                    // Пропускаем вложенные массивы (пустые массивы [])
                    in.beginArray();
                    in.endArray();
                } else {
                    // Пропускаем другие типы
                    in.skipValue();
                }
            }

            in.endArray();
        } else {
            in.skipValue();
        }

        return result;
    }
}