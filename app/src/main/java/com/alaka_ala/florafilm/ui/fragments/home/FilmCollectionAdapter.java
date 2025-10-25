package com.alaka_ala.florafilm.ui.fragments.home;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.data.database.entities.FilmCollectionEntity;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilmCollectionAdapter extends RecyclerView.Adapter<FilmCollectionAdapter.FilmCollectionViewHolder> {

    public interface OnCollectionClickListener {
        void onFilmClick(FilmItem film);
        void onCollectionTitleClick(FilmCollectionEntity collection);
    }

    private List<FilmCollectionEntity> collections;
    private OnCollectionClickListener onCollectionClickListener;
    private final RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    private final Map<String, Parcelable> scrollStates = new HashMap<>();

    public FilmCollectionAdapter(List<FilmCollectionEntity> collections) {
        this.collections = collections;
    }

    public void setCollections(List<FilmCollectionEntity> collections) {
        this.collections = collections;
        notifyDataSetChanged();
    }

    public void setOnCollectionClickListener(OnCollectionClickListener listener) {
        this.onCollectionClickListener = listener;
    }

    @NonNull
    @Override
    public FilmCollectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_film_collection, parent, false);
        FilmCollectionViewHolder viewHolder = new FilmCollectionViewHolder(view);
        viewHolder.filmsRecyclerView.setRecycledViewPool(viewPool);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FilmCollectionViewHolder holder, int position) {
        FilmCollectionEntity collection = collections.get(position);
        holder.bind(collection, onCollectionClickListener);

        Parcelable state = scrollStates.get(collection.getType());
        if (state != null) {
            holder.filmsRecyclerView.getLayoutManager().onRestoreInstanceState(state);
        } else {
            holder.filmsRecyclerView.getLayoutManager().scrollToPosition(0);
        }
    }

    @Override
    public void onViewRecycled(@NonNull FilmCollectionViewHolder holder) {
        super.onViewRecycled(holder);
        int position = holder.getAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            FilmCollectionEntity collection = collections.get(position);
            scrollStates.put(collection.getType(), holder.filmsRecyclerView.getLayoutManager().onSaveInstanceState());
        }
    }

    @Override
    public int getItemCount() {
        return collections.size();
    }

    static class FilmCollectionViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final RecyclerView filmsRecyclerView;
        private final FilmAdapter filmAdapter;

        public FilmCollectionViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.collection_title);
            filmsRecyclerView = itemView.findViewById(R.id.films_recyclerview);

            filmsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            filmAdapter = new FilmAdapter(List.of());
            filmsRecyclerView.setAdapter(filmAdapter);
        }

        public void bind(FilmCollectionEntity collection, OnCollectionClickListener listener) {
            // Устанавливаем преобразованный русский заголовок
            title.setText(getRussianTitleForType(collection.getType()));
            filmAdapter.setFilms(collection.getItems());

            // Пробрасываем клик по фильму наверх
            filmAdapter.setOnFilmClickListener(film -> {
                if (listener != null) {
                    listener.onFilmClick(film);
                }
            });

            // Устанавливаем клик на заголовок
            title.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCollectionTitleClick(collection);
                }
            });
        }

        // Новый метод для преобразования типа в заголовок
        private static String getRussianTitleForType(String type) {
            switch (type) {
                case "TOP_POPULAR_ALL":
                    return "Популярные фильмы и сериалы";
                case "TOP_POPULAR_MOVIES":
                    return "Популярные фильмы";
                case "TOP_250_TV_SHOWS":
                    return "Топ 250 сериалов";
                case "TOP_250_MOVIES":
                    return "Топ 250 фильмов";
                case "VAMPIRE_THEME":
                    return "Про вампиров";
                case "COMICS_THEME":
                    return "По комиксам";
                case "CLOSES_RELEASES":
                    return "Релизы";
                case "FAMILY":
                    return "Семейные";
                case "OSKAR_WINNERS_2021":
                    return "Оскар 2021";
                case "LOVE_THEME":
                    return "Любовь";
                case "ZOMBIE_THEME":
                    return "Зомби";
                case "CATASTROPHE_THEME":
                    return "Катастрофы";
                case "KIDS_ANIMATION_THEME":
                    return "Мультфильмы";
                case "POPULAR_SERIES":
                    return "Популярные сериалы";
                default:
                    return "Коллекция"; // Заголовок по умолчанию
            }
        }
    }
}
