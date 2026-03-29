package com.alaka_ala.florafilm.ui.fragments.filters;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentFiltersListBinding;
import com.alaka_ala.florafilm.ui.activities.MainActivity;
import com.alaka_ala.florafilm.ui.fragments.activity.HistoryViewAdapter;
import com.alaka_ala.florafilm.ui.fragments.home.FilmAdapter;
import com.alaka_ala.unofficial_kinopoisk_api.api.KinopoiskApiClientV2;
import com.alaka_ala.unofficial_kinopoisk_api.constants.FilmCollectionType;
import com.alaka_ala.unofficial_kinopoisk_api.db.FilmDetailsDao;
import com.alaka_ala.unofficial_kinopoisk_api.db.KinopoiskDatabaseV2;
import com.alaka_ala.unofficial_kinopoisk_api.models.CountryResponse;
import com.alaka_ala.unofficial_kinopoisk_api.models.FilmCollection;
import com.alaka_ala.unofficial_kinopoisk_api.models.FilmCountryOrGenresResponse;
import com.alaka_ala.unofficial_kinopoisk_api.models.FilmDetails;
import com.alaka_ala.unofficial_kinopoisk_api.models.FilmItem;
import com.alaka_ala.unofficial_kinopoisk_api.models.GenreResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Фрагмент для отображения списков фильмов по различным фильтрам.
 */
public class FiltersListFragment extends Fragment implements FilmAdapter.OnFilmClickListener, HistoryViewAdapter.OnItemClickListener {

    private static final Set<String> VALID_TYPES = new HashSet<>(Arrays.asList(
            "ratingKinopoisk", "ratingImdb", "year", "genre", "country",
            "collection", "bookmark", "history", "resume"
    ));

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private FragmentFiltersListBinding binding;
    private KinopoiskApiClientV2 kinopoiskApiClientV2;
    private FilmAdapter filmAdapter;
    private HistoryViewAdapter historyAdapter;
    private FilmDetailsDao filmDetailsDao;

    private int currentPage = 1;
    private int totalPages = -1;
    private boolean isLoading = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFiltersListBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    private String type, data;
    private Bundle args;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();

        if (args != null) {
            type = args.getString("type", "");
            data = args.getString("data", "");

            printTitle(type, data);

            if (isTypeValid(type)) {
                setupRecyclerView();
                fetchData(type, args);
            }
        }
    }

    private void init() {
        filmDetailsDao = KinopoiskDatabaseV2.getDatabase(getContext()).filmDetailsDao();
        kinopoiskApiClientV2 = KinopoiskApiClientV2.getInstance();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideBottomNavigationView();
        }
        binding.progressBarLoadingContent.setVisibility(View.VISIBLE);
        args = getArguments();
    }

    private void printTitle(String type, String data) {
        String title = "";

        if (type.equals("bookmark")) {
            title = "Избранное";
        } else if (type.equals("ratingKinopoisk")) {
            title = "Фильтр по рейтингу кинопоиска";
        } else if (type.equals("ratingImdb")) {
            title = "Фильтр по рейтингу Imdb";
        } else if (type.equals("year")) {
            title = "Фильтр по году (" + data + ")";
        } else if (type.equals("genre")) {
            title = "Фильтр по жанру (" + data + ")";
        } else if (type.equals("country")) {
            title = "Фильтр по стране (" + data + ")";
        } else if (type.equals("collection")) {
            title = getNameColletion(data);
        } else if (type.equals("history")) {
            title = "История просмотра";
        } else if (type.equals("resume")) {
            title = "ранее смотрели";
        }


        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbarTitle(title);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Предотвращение утечек памяти
        executor.close();
    }

    /**
     * Настраивает RecyclerView в зависимости от типа контента.
     */
    private void setupRecyclerView() {
        RecyclerView rvFilterList = binding.rvFilterList;
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        rvFilterList.setLayoutManager(layoutManager);

        if ("collection".equals(type) || isApiFilteredType(type)) {
            filmAdapter = new FilmAdapter(new ArrayList<>(), FilmAdapter.VIEW_TYPE_GRID);
            filmAdapter.setOnFilmClickListener(this);
            rvFilterList.setAdapter(filmAdapter);
            rvFilterList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (!isLoading && (totalPages == -1 || currentPage < totalPages)) {
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                            loadMoreItems();
                        }
                    }
                }
            });
        }
        else if (isDatabaseType(type)) {
            historyAdapter = new HistoryViewAdapter(HistoryViewAdapter.ViewTypeItem.GRID_ADAPTIVE);
            historyAdapter.setOnItemClickListener(this);
            rvFilterList.setAdapter(historyAdapter);
            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (isDatabaseType(type)) {
            menu.add("Очистить").setIcon(R.drawable.delete).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals("Очистить")) {
            executor.execute(() -> {
                switch (type) {
                    case "bookmark":
                        filmDetailsDao.removeByBookmark();
                        break;
                    case "history":
                        filmDetailsDao.removeByHistory();
                        break;
                    case "resume":
                        filmDetailsDao.removeByResume();
                        break;
                }
            });
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    /**
     * Загружает следующую страницу элементов.
     */
    private void loadMoreItems() {
        isLoading = true;
        currentPage++;
        Bundle args = getArguments();
        if (args != null) {
            String type = args.getString("type", "");
            if (isTypeValid(type)) {
                fetchData(type, args);
            }
        }
    }

    /**
     * Получает данные в зависимости от типа.
     *
     * @param type Тип данных для получения.
     * @param args Аргументы для запроса.
     */
    private void fetchData(String type, Bundle args) {
        if ("collection".equals(type)) {
            fetchCollectionFilms(args);
        } else if (isDatabaseType(type)) {
            observeDataFromDb(type);
        } else if (isApiFilteredType(type)) {
            fetchFilteredFilms(type, args);
        }
    }

    /**
     * Получает коллекцию фильмов из API.
     *
     * @param args Аргументы, содержащие тип коллекции.
     */
    private void fetchCollectionFilms(Bundle args) {
        String collection = args.getString("data", "");
        try {
            FilmCollectionType typeEnum = FilmCollectionType.valueOf(collection);
            kinopoiskApiClientV2.getFilmCollection(typeEnum, currentPage, false, new FilmCollectionCallback());
        } catch (IllegalArgumentException e) {
            handleApiError(); // Обработка неверного типа коллекции
        }
    }

    /**
     * Получает отфильтрованные фильмы по жанру, стране, году или рейтингу из API.
     *
     * @param type Тип фильтра.
     * @param args Аргументы для фильтрации.
     */
    private void fetchFilteredFilms(String type, Bundle args) {
        kinopoiskApiClientV2.getGenreOrCountryList(new KinopoiskApiClientV2.ApiCallback<>() {
            @Override
            public void onSuccess(FilmCountryOrGenresResponse result) {
                FilmCollectionCallback callback = new FilmCollectionCallback();
                switch (type) {
                    case "ratingKinopoisk":
                        String rating = args.getString("data", "7");
                        kinopoiskApiClientV2.getFilmsByRating(rating, rating, currentPage, false, callback);
                        break;
                    case "year":
                        String year = args.getString("data", "2025");
                        kinopoiskApiClientV2.getFilmsByYear(year, currentPage, false, callback);
                        break;
                    case "genre":
                        String genreName = args.getString("data", "");
                        String genreId = findGenreId(result, genreName);
                        if (genreId != null) {
                            kinopoiskApiClientV2.getFilmsByGenre(genreId, currentPage, false, callback);
                        } else {
                            handleApiError();
                        }
                        break;
                    case "country":
                        String countryName = args.getString("data", "");
                        String countryId = findCountryId(result, countryName);
                        if (countryId != null) {
                            kinopoiskApiClientV2.getFilmsByCountry(countryId, currentPage, false, callback);
                        } else {
                            handleApiError();
                        }
                        break;
                }
            }

            @Override
            public void onError(KinopoiskApiClientV2.ApiException error) {
                handleApiError();
            }
        });
    }

    /**
     * Начинает наблюдение за данными из базы данных.
     *
     * @param type Тип данных для наблюдения.
     */
    private void observeDataFromDb(String type) {
        switch (type) {
            case "history":
                filmDetailsDao.getByView().observe(getViewLifecycleOwner(), filmDetails -> {
                    new Handler(Looper.getMainLooper()).post(() -> historyAdapter.submitList(filmDetails));
                });
                break;
            case "resume":
                filmDetailsDao.getByIsStartView().observe(getViewLifecycleOwner(), filmDetails -> {
                    new Handler(Looper.getMainLooper()).post(() -> historyAdapter.submitList(filmDetails));
                });
                break;
            case "bookmark":
                filmDetailsDao.getByBookmark().observe(getViewLifecycleOwner(), filmDetails -> {
                    new Handler(Looper.getMainLooper()).post(() -> historyAdapter.submitList(filmDetails));
                });
                break;
        }
        hideLoading();
    }

    private String findGenreId(FilmCountryOrGenresResponse data, String genreName) {
        for (GenreResponse genreResponse : data.getGenres()) {
            if (genreResponse.getGenre().equalsIgnoreCase(genreName)) {
                return String.valueOf(genreResponse.getId());
            }
        }
        return null;
    }

    private String findCountryId(FilmCountryOrGenresResponse data, String countryName) {
        for (CountryResponse countryResponse : data.getCountries()) {
            if (countryResponse.getCountry().equalsIgnoreCase(countryName)) {
                return String.valueOf(countryResponse.getId());
            }
        }
        return null;
    }

    /**
     * Проверяет, является ли тип допустимым.
     *
     * @param type Тип для проверки.
     * @return true, если тип допустим.
     */
    private boolean isTypeValid(String type) {
        return VALID_TYPES.contains(type);
    }

    /**
     * Проверяет, относится ли тип к данным из локальной БД.
     *
     * @param type Тип для проверки.
     * @return true, если тип относится к БД.
     */
    private boolean isDatabaseType(String type) {
        return "bookmark".equals(type) || "history".equals(type) || "resume".equals(type);
    }

    /**
     * Проверяет, относится ли тип к фильтруемым данным из API.
     *
     * @param type Тип для проверки.
     * @return true, если тип относится к фильтрам API.
     */
    private boolean isApiFilteredType(String type) {
        return "ratingKinopoisk".equals(type) || "year".equals(type) || "genre".equals(type) || "country".equals(type);
    }


    /**
     * Скрывает индикатор загрузки и устанавливает isLoading в false.
     */
    private void hideLoading() {
        if (binding != null) {
            binding.progressBarLoadingContent.setVisibility(View.GONE);
        }
        isLoading = false;
    }

    /**
     * Обрабатывает ошибки API.
     */
    private void handleApiError() {
        if (getActivity() != null) {
            requireActivity().runOnUiThread(this::hideLoading);
            // Здесь можно добавить логику для отображения сообщения об ошибке, например, через Snackbar
        }
    }

    /**
     * Осуществляет переход к деталям фильма.
     *
     * @param kinopoiskId ID фильма на Кинопоиске.
     */
    private void navigateToFilmDetails(int kinopoiskId) {
        if (binding == null) return;
        Bundle bundle = new Bundle();
        bundle.putInt("kinopoiskId", kinopoiskId);
        NavController navController = Navigation.findNavController(binding.getRoot());
        navController.navigate(R.id.action_filtersListFragment_to_filmDetailsFragment, bundle);
    }

    /**
     * Вызывается при клике на фильм в списке (FilmAdapter).
     *
     * @param film Объект FilmItem.
     */
    @Override
    public void onFilmClick(FilmItem film) {
        navigateToFilmDetails(film.getKinopoiskId());
    }

    /**
     * Вызывается при клике на фильм в списке (HistoryViewAdapter).
     *
     * @param film Объект FilmDetails.
     */
    @Override
    public void onItemClick(FilmDetails film) {
        navigateToFilmDetails(film.getKinopoiskId());
    }

    /**
     * Внутренний класс для обработки ответов от API при запросе коллекций фильмов.
     */
    private class FilmCollectionCallback implements KinopoiskApiClientV2.ApiCallback<FilmCollection> {
        @Override
        public void onSuccess(FilmCollection result) {
            if (getActivity() == null) return;
            requireActivity().runOnUiThread(() -> {
                if (totalPages == -1) {
                    totalPages = result.getTotalPagesCount();
                }
                if (filmAdapter != null) {
                    filmAdapter.addFilms(result.getItems());
                }
                hideLoading();
            });
        }

        @Override
        public void onError(KinopoiskApiClientV2.ApiException error) {
            handleApiError();
        }
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
