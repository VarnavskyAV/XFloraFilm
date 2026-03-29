package com.alaka_ala.florafilm.ui.fragments.activity;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.unofficial_kinopoisk_api.models.FilmDetails;
import com.bumptech.glide.Glide;

import java.util.Objects;

public class HistoryViewAdapter extends ListAdapter<FilmDetails, HistoryViewAdapter.HistoryViewHolder> {
    public enum ViewTypeItem {
        HORIZONTAL,
        GRID_ADAPTIVE
    }

    private ViewTypeItem type;

    private OnItemClickListener listener;

    public HistoryViewAdapter(ViewTypeItem viewTypeItem) {
        super(DIFF_CALLBACK);
        this.type = viewTypeItem;
    }

    public HistoryViewAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<FilmDetails> DIFF_CALLBACK = new DiffUtil.ItemCallback<FilmDetails>() {
        @Override
        public boolean areItemsTheSame(@NonNull FilmDetails oldItem, @NonNull FilmDetails newItem) {
            return oldItem.getKinopoiskId().equals(newItem.getKinopoiskId());
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull FilmDetails oldItem, @NonNull FilmDetails newItem) {
            // Сравниваем только те поля, которые влияют на отображение
            /*return Objects.equals(oldItem.getNameRu(), newItem.getNameRu()) &&
                    Objects.equals(oldItem.getRatingKinopoisk(), newItem.getRatingKinopoisk()) &&
                    Objects.equals(oldItem.getYear(), newItem.getYear()) &&
                    Objects.equals(oldItem.getPosterUrl(), newItem.getPosterUrl()) &&
                    Objects.equals(oldItem.getGenres(), newItem.getGenres()) &&
                    oldItem.isBookmark() == newItem.isBookmark() &&
                    oldItem.isStartView() == newItem.isStartView();*/
            return Objects.equals(oldItem.getPosterUrl(), newItem.getPosterUrl());
        }
    };

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (type == ViewTypeItem.GRID_ADAPTIVE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_film, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_film_horizontal, parent, false);
        }
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        FilmDetails film = getItem(position);
        if (film != null) {
            holder.bind(film);
        }
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final ImageView filmPoster;
        private final TextView filmRating;
        private final TextView filmTitle;
        private final TextView filmYearGenre;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            filmPoster = itemView.findViewById(R.id.film_poster);
            filmRating = itemView.findViewById(R.id.film_rating);
            filmTitle = itemView.findViewById(R.id.film_title);
            filmYearGenre = itemView.findViewById(R.id.film_year_genre);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });
        }

        /**
         * Заполняет вью данными из объекта фильма.
         * @param film Объект FilmDetails с данными о фильме.
         */
        public void bind(FilmDetails film) {
            filmTitle.setText(film.getNameRu());
            filmRating.setText(String.valueOf(film.getRatingKinopoisk()));
            
            String yearGenre = film.getYear() + ", " + (film.getGenres().isEmpty() ? "" : film.getGenres().get(0).getName());
            filmYearGenre.setText(yearGenre);

            Glide.with(itemView.getContext())
                    .load(film.getPosterUrl())
                    .into(filmPoster);
        }
    }

    /**
     * Интерфейс для обработки нажатий на элементы списка.
     */
    public interface OnItemClickListener {
        /**
         * Вызывается при нажатии на элемент списка.
         * @param film Объект FilmDetails, соответствующий нажатому элементу.
         */
        void onItemClick(FilmDetails film);
    }

    /**
     * Устанавливает слушатель нажатий на элементы списка.
     * @param listener Реализация интерфейса OnItemClickListener.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
