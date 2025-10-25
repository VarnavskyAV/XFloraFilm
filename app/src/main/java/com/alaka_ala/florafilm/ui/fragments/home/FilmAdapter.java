package com.alaka_ala.florafilm.ui.fragments.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmItem;
import com.bumptech.glide.Glide;

import java.util.List;

public class FilmAdapter extends RecyclerView.Adapter<FilmAdapter.FilmViewHolder> {

    public interface OnFilmClickListener {
        void onFilmClick(FilmItem film);
    }

    private List<FilmItem> films;
    private OnFilmClickListener onFilmClickListener;

    public FilmAdapter(List<FilmItem> films) {
        this.films = films;
    }

    public void setFilms(List<FilmItem> films) {
        this.films = films;
        notifyDataSetChanged();
    }

    public void setOnFilmClickListener(OnFilmClickListener listener) {
        this.onFilmClickListener = listener;
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_film, parent, false);
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

        public void bind(FilmItem film) {
            title.setText(film.getBestName());

            if (film.getRatingKinopoisk() > 0) {
                rating.setText("⭐ " + film.getRatingKinopoisk());
                rating.setVisibility(View.VISIBLE);
            } else {
                rating.setVisibility(View.GONE);
            }

            String year = film.getYear() != 0 ? String.valueOf(film.getYear()) : "";
            String genre = (film.getGenres() != null && !film.getGenres().isEmpty()) ? film.getGenres().get(0).getName() : "";
            String yearGenreText = year + (!year.isEmpty() && !genre.isEmpty() ? ", " : "") + genre;
            yearGenre.setText(yearGenreText);

            Glide.with(itemView.getContext())
                    .load(film.getBestPoster())
                    .into(poster);
        }
    }
}
