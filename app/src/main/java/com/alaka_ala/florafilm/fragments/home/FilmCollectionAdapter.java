package com.alaka_ala.florafilm.fragments.home;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.unofficial_kinopoisk_api.models.FilmCollection;

import java.util.HashMap;
import java.util.List;

public class FilmCollectionAdapter extends RecyclerView.Adapter<FilmCollectionAdapter.FilmCollectionViewHolder> {

    // Use FilmAdapter's OnFilmClickListener
    // public interface OnFilmClickListener {
    //     void onFilmClick(FilmItem film);
    // }

    public interface OnCollectionClickListener {
        void onCollectionClick(FilmCollectionItem collection);
    }

    public static class FilmCollectionItem {
        private final String title;
        private final FilmCollection filmCollection;

        public FilmCollectionItem(String title, FilmCollection filmCollection) {
            this.title = title;
            this.filmCollection = filmCollection;
        }

        public String getTitle() {
            return title;
        }

        public FilmCollection getFilmCollection() {
            return filmCollection;
        }
    }

    private final List<FilmCollectionItem> filmCollections;
    private FilmAdapter.OnFilmClickListener onFilmClickListener; // Changed to FilmAdapter.OnFilmClickListener
    private OnCollectionClickListener onCollectionClickListener;
    private final HashMap<String, Parcelable> nestedRecyclerViewStates;

    public FilmCollectionAdapter(List<FilmCollectionItem> filmCollections, HashMap<String, Parcelable> nestedRecyclerViewStates) {
        this.filmCollections = filmCollections;
        this.nestedRecyclerViewStates = nestedRecyclerViewStates;
    }

    public void setOnFilmClickListener(FilmAdapter.OnFilmClickListener listener) { // Changed to FilmAdapter.OnFilmClickListener
        this.onFilmClickListener = listener;
    }

    public void setOnCollectionClickListener(OnCollectionClickListener listener) {
        this.onCollectionClickListener = listener;
    }

    @NonNull
    @Override
    public FilmCollectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_film_collection, parent, false);
        return new FilmCollectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmCollectionViewHolder holder, int position) {
        FilmCollectionItem collectionItem = filmCollections.get(position);
        holder.bind(collectionItem, onFilmClickListener, onCollectionClickListener, nestedRecyclerViewStates);
    }

    @Override
    public int getItemCount() {
        return filmCollections.size();
    }

    static class FilmCollectionViewHolder extends RecyclerView.ViewHolder {

        private final TextView collectionTitle;
        private final RecyclerView filmsRecyclerView;

        public FilmCollectionViewHolder(@NonNull View itemView) {
            super(itemView);
            collectionTitle = itemView.findViewById(R.id.collection_title);
            filmsRecyclerView = itemView.findViewById(R.id.films_recyclerview);
        }

        public void bind(FilmCollectionItem collectionItem, FilmAdapter.OnFilmClickListener onFilmClickListener, OnCollectionClickListener onCollectionClickListener, HashMap<String, Parcelable> nestedRecyclerViewStates) { // Changed to FilmAdapter.OnFilmClickListener
            collectionTitle.setText(getNameColletion(collectionItem.getTitle()));

            FilmAdapter filmAdapter = new FilmAdapter(collectionItem.getFilmCollection().getItems(), FilmAdapter.VIEW_TYPE_HORIZONTAL);
            filmAdapter.setOnFilmClickListener(onFilmClickListener); // This should work now
            LinearLayoutManager layoutManager = new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false);
            filmsRecyclerView.setLayoutManager(layoutManager);
            filmsRecyclerView.setAdapter(filmAdapter);

            filmsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        nestedRecyclerViewStates.put(collectionItem.getTitle(), recyclerView.getLayoutManager().onSaveInstanceState());
                    }
                }
            });

            Parcelable savedState = nestedRecyclerViewStates.get(collectionItem.getTitle());
            if (savedState != null) {
                filmsRecyclerView.getLayoutManager().onRestoreInstanceState(savedState);
            }

            itemView.setOnClickListener(v -> {
                if (onCollectionClickListener != null) {
                    onCollectionClickListener.onCollectionClick(collectionItem);
                }
            });
        }

        private String getNameColletion(String title) {
            switch (title) {
                case "TOP_POPULAR_ALL":
                    return "Популярные фильмы и сериалы";
                case "TOP_POPULAR_MOVIES":
                    return "Популярные фильмы";
                case "POPULAR_SERIES":
                    return "Популярные сериалы";
                case "TOP_250_TV_SHOWS":
                    return "Топ 250 сериалов";
                case "TOP_250_MOVIES":
                    return "Топ 250 фильмов";
                case "VAMPIRE_THEME":
                    return "Про вампиров";
                case "COMICS_THEME":
                    return "По комиксам";
                case "CLOSES_RELEASES":
                    return "Закратые релизы";
                case "FAMILY":
                    return "Семейные";
                case "OSKAR_WINNERS_2021":
                    return "Оскар 2021";
                case "LOVE_THEME":
                    return "Про любовь";
                case "ZOMBIE_THEME":
                    return "Зомби";
                case "CATASTROPHE_THEME":
                    return "Катастрофы";
                case "KIDS_ANIMATION_THEME":
                    return "Мультфильмы";
                default:
                    return title;
            }
        }

    }
}
