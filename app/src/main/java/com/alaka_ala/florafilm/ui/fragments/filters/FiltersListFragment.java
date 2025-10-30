package com.alaka_ala.florafilm.ui.fragments.filters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentFiltersListBinding;
import com.alaka_ala.florafilm.ui.activities.MainActivity;
import com.alaka_ala.florafilm.ui.fragments.home.FilmAdapter;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.api.KinopoiskApiClientV2;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.constants.FilmCollectionType;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models.CountryResponse;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models.FilmCollection;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models.FilmCountryOrGenresResponse;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models.FilmItem;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models.Genre;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models.GenreResponse;

import java.util.ArrayList;

public class FiltersListFragment extends Fragment implements FilmAdapter.OnFilmClickListener{
    private FragmentFiltersListBinding binding;
    private KinopoiskApiClientV2 kinopoiskApiClientV2;
    private RecyclerView rvFilterList;
    private FilmAdapter filmAdapter;
    private int currentPage = 1;
    private int totalPages = -1; // Инициализируем -1, чтобы сделать первый запрос
    private boolean isLoading = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFiltersListBinding.inflate(inflater, container, false);
        // скрываем нижнюю навигацию при выходе из этого фрагмента.
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideBottomNavigationView();
        }
        kinopoiskApiClientV2 = KinopoiskApiClientV2.getInstance();
        setupRecyclerView();

        Bundle args = getArguments();
        if (args != null) {
            String type = args.getString("type", "");
            if (isTypeValid(type)) {
                fetchFilms(type, args);
            }
        }

        return binding.getRoot();
    }

    /**
     * Настраивает RecyclerView, включая LayoutManager и адаптер.
     */
    private void setupRecyclerView() {
        rvFilterList = binding.rvFilterList;
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2, LinearLayoutManager.VERTICAL, false);
        rvFilterList.setLayoutManager(layoutManager);
        filmAdapter = new FilmAdapter(new ArrayList<>(), FilmAdapter.VIEW_TYPE_GRID);
        filmAdapter.setOnFilmClickListener(this);
        rvFilterList.setAdapter(filmAdapter);
        rvFilterList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && (totalPages == -1 || currentPage < totalPages)) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadMoreItems();
                    }
                }
            }
        });
    }

    /**
     * Загружает следующую страницу фильмов.
     */
    private void loadMoreItems() {
        isLoading = true;
        currentPage++;
        Bundle args = getArguments();
        if (args != null) {
            String type = args.getString("type", "");
            if (isTypeValid(type)) {
                fetchFilms(type, args);
            }
        }
    }

    /**
     * Выполняет сетевой запрос для получения списка фильмов в зависимости от типа фильтра.
     *
     * @param type Тип фильтра (например, "ratingKinopoisk", "year" и т.д.).
     * @param args Аргументы, содержащие параметры для фильтрации.
     */
    private void fetchFilms(String type, Bundle args) {
        if (type.equals("collection")) {
            String collection = args.getString("data", "");
            FilmCollectionType typeEnum = FilmCollectionType.valueOf(collection);
            kinopoiskApiClientV2.getFilmCollection(typeEnum, currentPage, false, new KinopoiskApiClientV2.ApiCallback<FilmCollection>() {
                @Override
                public void onSuccess(FilmCollection result) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (totalPages == -1) {
                                totalPages = result.getTotalPagesCount();
                            }
                            filmAdapter.addFilms(result.getItems());
                            isLoading = false;
                        });
                    }
                }

                @Override
                public void onError(KinopoiskApiClientV2.ApiException error) {
                    isLoading = false;
                }
            });
        } else {
            kinopoiskApiClientV2.getGenreOrCountryList(new KinopoiskApiClientV2.ApiCallback<FilmCountryOrGenresResponse>() {
                @Override
                public void onSuccess(FilmCountryOrGenresResponse result) {
                    KinopoiskApiClientV2.ApiCallback<FilmCollection> callback = new KinopoiskApiClientV2.ApiCallback<FilmCollection>() {
                        @Override
                        public void onSuccess(FilmCollection filmCollection) {
                            if (filmCollection != null && filmCollection.getItems() != null && getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (totalPages == -1) {
                                        totalPages = filmCollection.getTotalPagesCount();
                                    }
                                    filmAdapter.addFilms(filmCollection.getItems());
                                    isLoading = false;
                                });
                            }
                        }

                        @Override
                        public void onError(KinopoiskApiClientV2.ApiException error) {
                            isLoading = false;
                            // Обработка ошибки, например, показать Toast или Snackbar
                        }
                    };

                    switch (type) {
                        case "ratingKinopoisk":
                            String ratingKinopoisk = args.getString("data", "7");
                            kinopoiskApiClientV2.getFilmsByRating(ratingKinopoisk, "10", currentPage, false, callback);
                            break;
                        case "year":
                            String year = args.getString("data", "2025");
                            kinopoiskApiClientV2.getFilmsByYear(year, currentPage, false, callback);
                            break;
                        case "genre":
                            String genre = args.getString("data", "1"); // Предполагается, что передается ID жанра
                            for (GenreResponse genreResponse : result.getGenres()) {
                                if (genreResponse.getGenre().equals(genre)) {
                                    genre = String.valueOf(genreResponse.getId());
                                    break;
                                }
                            }
                            kinopoiskApiClientV2.getFilmsByGenre(genre, currentPage, false, callback);
                            break;
                        case "country":
                            String country = args.getString("data", "1"); // Предполагается, что передается ID страны
                            for (CountryResponse counrey : result.getCountries()) {
                                if (counrey.getCountry().equals(country)) {
                                    country = String.valueOf(counrey.getId());
                                    break;
                                }
                            }
                            kinopoiskApiClientV2.getFilmsByCountry(country, currentPage, false, callback);
                            break;
                    }
                }

                @Override
                public void onError(KinopoiskApiClientV2.ApiException error) {
                    isLoading = false;
                }
            });
        }
    }

    /**
     * Проверяет, является ли переданный тип фильтра допустимым.
     *
     * @param type Тип для проверки.
     * @return true, если тип допустим, иначе false.
     */
    private boolean isTypeValid(String type) {
        String[] types = new String[]{"ratingKinopoisk", "ratingImdb", "year", "genre", "country", "collection"};
        for (String t : types) {
            if (t.equals(type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onFilmClick(FilmItem film) {
        Bundle bundle = new Bundle();
        bundle.putInt("kinopoiskId", film.getKinopoiskId());
        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_filtersListFragment_to_filmDetailsFragment, bundle);
    }
}
