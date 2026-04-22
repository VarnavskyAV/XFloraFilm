package com.alaka_ala.florafilm.fragments.home;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Страницы карусели на главной (большой постер + текст).
 */
public class HomeHeroPagerAdapter extends RecyclerView.Adapter<HomeHeroPagerAdapter.PageHolder> {

    private final List<FilmItem> films = new ArrayList<>();
    private FilmAdapter.OnFilmClickListener onFilmClickListener;

    public void setFilms(List<FilmItem> items) {
        films.clear();
        if (items != null) {
            films.addAll(items);
        }
        notifyDataSetChanged();
    }

    public void setOnFilmClickListener(FilmAdapter.OnFilmClickListener listener) {
        this.onFilmClickListener = listener;
    }

    @NonNull
    @Override
    public PageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_hero_page, parent, false);
        return new PageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageHolder holder, int position) {
        holder.bind(films.get(position));
    }

    @Override
    public int getItemCount() {
        return films.size();
    }

    class PageHolder extends RecyclerView.ViewHolder {
        private final ImageView poster;
        private final TextView title;
        private final TextView subtitle;
        private final TextView rating;

        PageHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.hero_poster);
            title = itemView.findViewById(R.id.hero_title);
            subtitle = itemView.findViewById(R.id.hero_subtitle);
            rating = itemView.findViewById(R.id.hero_rating);
        }

        void bind(FilmItem film) {
            title.setText(film.getBestName());

            if (film.getRatingKinopoisk() > 0) {
                rating.setText("★ " + film.getRatingKinopoisk());
                rating.setVisibility(View.VISIBLE);
            } else {
                rating.setVisibility(View.GONE);
            }

            String year = film.getYear() != 0 ? String.valueOf(film.getYear()) : "";
            String genre = (film.getGenres() != null && !film.getGenres().isEmpty())
                    ? film.getGenres().get(0).getName() : "";
            String line = year;
            if (!year.isEmpty() && !genre.isEmpty()) {
                line = year + " · " + genre;
            } else if (!genre.isEmpty()) {
                line = genre;
            }
            subtitle.setText(line);

            Glide.with(itemView.getContext())
                    .load(film.getPosterUrlPreview())
                    .centerCrop()
                    .into(poster);

            itemView.setContentDescription(film.getBestName());

            itemView.setOnClickListener(v -> {
                if (onFilmClickListener != null) {
                    onFilmClickListener.onFilmClick(film);
                }
            });
        }
    }
}
