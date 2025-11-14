package com.alaka_ala.florafilm.ui.fragments.find.adapters;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.ItemFilmSearchBinding;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models.SearchResultFilm;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Адаптер для отображения результатов поиска фильмов в RecyclerView.
 */
public class FilmSearchAdapter extends RecyclerView.Adapter<FilmSearchAdapter.FilmViewHolder> {

    private List<SearchResultFilm> films = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    /**
     * Интерфейс для обработки нажатий на элементы списка.
     */
    public interface OnItemClickListener {
        /**
         * Вызывается при нажатии на элемент списка.
         * @param film Фильм, по которому кликнули.
         */
        void onItemClick(SearchResultFilm film);
    }

    /**
     * Устанавливает слушатель нажатий на элементы списка.
     * @param listener Слушатель для обработки нажатий.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    /**
     * Обновляет список фильмов в адаптере.
     * @param newFilms Новый список фильмов для отображения.
     */
    public void setFilms(List<SearchResultFilm> newFilms) {
        this.films = newFilms;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Используем ViewBinding для инфлейта макета
        ItemFilmSearchBinding binding = ItemFilmSearchBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FilmViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmViewHolder holder, int position) {
        SearchResultFilm film = films.get(position);
        holder.bind(film);
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(film);
            }
        });
    }

    @Override
    public int getItemCount() {
        return films.size();
    }

    /**
     * ViewHolder для элемента списка фильмов.
     */
    static class FilmViewHolder extends RecyclerView.ViewHolder {
        private final ItemFilmSearchBinding binding;

        public FilmViewHolder(ItemFilmSearchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Привязывает данные фильма к View.
         * @param film Объект фильма для отображения.
         */
        public void bind(SearchResultFilm film) {
            binding.textViewTitle.setText(film.getNameRu());

            // Формируем строку с годом и жанрами
            String year = film.getYear();
            String genres = film.getGenres().stream()
                    .map(g -> g.getGenre())
                    .collect(Collectors.joining(", "));
            binding.textViewDetails.setText(String.format("%s, %s", year, genres));

            // Устанавливаем рейтинг
            binding.chipRating.setText(film.getRating());
            // Устанавливаем иконку, если она нужна
            // binding.chipRating.setChipIconResource(R.drawable.ic_star);

            // Загружаем постер с помощью Glide
            // Убедитесь, что у вас добавлена зависимость Glide и есть доступ в интернет
            Glide.with(itemView.getContext())
                    .load(film.getPosterUrlPreview())
                    .placeholder(R.drawable.ic_launcher_background) // Заглушка на время загрузки
                    .error(R.drawable.ic_launcher_background) // Заглушка в случае ошибки
                    .into(binding.imageViewPoster);
        }
    }
}