package com.alaka_ala.florafilm.fragments.home;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.unofficial_kinopoisk_api.models.FilmItem;
import com.bumptech.glide.Glide;

import java.util.List;

public class FilmAdapter extends RecyclerView.Adapter<FilmAdapter.FilmViewHolder> {

    public static final int VIEW_TYPE_GRID = 0;
    public static final int VIEW_TYPE_HORIZONTAL = 1;

    public interface OnFilmClickListener {
        void onFilmClick(FilmItem film);
    }

    private List<FilmItem> films;
    private OnFilmClickListener onFilmClickListener;
    private int viewType;

    public FilmAdapter(List<FilmItem> films, int viewType) {
        this.films = films;
        this.viewType = viewType;
    }

    public void setFilms(List<FilmItem> films) {
        this.films = films;
        notifyDataSetChanged();
    }

    /**
     * Добавляет список фильмов к существующему списку в адаптере.
     *
     * @param newFilms Список новых фильмов для добавления.
     */
    public void addFilms(List<FilmItem> newFilms) {
        int startPosition = films.size();
        films.addAll(newFilms);
        notifyItemRangeInserted(startPosition, newFilms.size());
    }

    public void setOnFilmClickListener(OnFilmClickListener listener) {
        this.onFilmClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_GRID) {
            // Используется для отображения фильмов в сетке
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_film, parent, false);
        } else {
            // Используется на главной странице для отображения фильмов горизонтально
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_film_horizontal, parent, false);
        }
        return new FilmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmViewHolder holder, int position) {
        FilmItem film = films.get(position);
        holder.bind(film);
    }

    @Override
    public int getItemCount() {
        return films.size();
    }

    class FilmViewHolder extends RecyclerView.ViewHolder {

        private final ImageView poster;
        private final TextView title;
        private final TextView rating;
        private final TextView yearGenre;

        public FilmViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.film_poster);
            title = itemView.findViewById(R.id.film_title);
            rating = itemView.findViewById(R.id.film_rating);
            yearGenre = itemView.findViewById(R.id.film_year_genre);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (onFilmClickListener != null && pos != RecyclerView.NO_POSITION) {
                    onFilmClickListener.onFilmClick(films.get(pos));
                }
            });
        }

        @SuppressLint("SetTextI18n")
        public void bind(FilmItem film) {
            title.setText(film.getBestName());

            if (film.getRatingKinopoisk() > 0) {
                rating.setText("★ " + film.getRatingKinopoisk());
                rating.setVisibility(View.VISIBLE);
            } else {
                rating.setVisibility(View.GONE);
            }

            String year = film.getYear() != 0 ? String.valueOf(film.getYear()) : "";
            String genre = (film.getGenres() != null && !film.getGenres().isEmpty()) ? film.getGenres().get(0).getName() : "";
            String yearGenreText = year + (!year.isEmpty() && !genre.isEmpty() ? ", " : "") + genre;
            yearGenre.setText(yearGenreText);

            Glide.with(itemView.getContext())
                    .load(film.getPosterUrlPreview())
                    .into(poster);
        }
    }
}
