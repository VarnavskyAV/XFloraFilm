package com.alaka_ala.florafilm.fragments.find;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentFindBinding;
import com.alaka_ala.florafilm.activities.MainActivity;
import com.alaka_ala.florafilm.fragments.find.adapters.FilmSearchAdapter;
import com.alaka_ala.unofficial_kinopoisk_api.api.KinopoiskApiClientV2;
import com.alaka_ala.unofficial_kinopoisk_api.models.FilmSearchResponse;
import com.alaka_ala.unofficial_kinopoisk_api.models.SearchResultFilm;

import java.util.Collections;
import java.util.List;

public class FindFragment extends Fragment implements FilmSearchAdapter.OnItemClickListener {
    private FragmentFindBinding binding;
    private KinopoiskApiClientV2 kinopoiskApiClientV2;
    private FilmSearchAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFindBinding.inflate(inflater, container, false);
        // Показываем нижнюю навигацию при выходе из этого фрагмента.
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideBottomNavigationView();
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        kinopoiskApiClientV2 = KinopoiskApiClientV2.getInstance();
        setupRecyclerView();
        setupSearch();
    }

    /**
     * Настраивает RecyclerView для отображения результатов поиска.
     */
    private void setupRecyclerView() {
        adapter = new FilmSearchAdapter();
        adapter.setOnItemClickListener(this);
        binding.recyclerViewFilms.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewFilms.setAdapter(adapter);
    }

    /**
     * Настраивает логику работы SearchBar и SearchView.
     */
    private void setupSearch() {
        binding.searchView.getEditText().setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = binding.searchView.getText().toString();
                if (!query.isEmpty()) {
                    searchFilms(query);
                    binding.searchBar.setText(query);
                    binding.searchView.hide();
                }
                return true;
            }
            return false;
        });
    }

    /**
     * Выполняет поиск фильмов по ключевому слову.
     * @param keyword Ключевое слово для поиска.
     */
    private void searchFilms(String keyword) {
        showLoading(true);

        kinopoiskApiClientV2.searchFilmByKeyword(keyword, 1, false, new KinopoiskApiClientV2.ApiCallback<>() {
            @Override
            public void onSuccess(FilmSearchResponse result) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    showLoading(false);
                    if (result != null && result.getFilms() != null && !result.getFilms().isEmpty()) {
                        updateUiWithResults(result.getFilms());
                    } else {
                        showEmptyResult();
                    }
                });

            }

            @Override
            public void onError(KinopoiskApiClientV2.ApiException error) {
                showLoading(false);
                showEmptyResult(); // Или показать специфичную ошибку
            }
        });
    }

    /**
     * Обновляет UI, отображая список найденных фильмов.
     * @param films Список фильмов.
     */
    private void updateUiWithResults(List<SearchResultFilm> films) {
        binding.textViewEmpty.setVisibility(View.GONE);
        binding.recyclerViewFilms.setVisibility(View.VISIBLE);
        adapter.setFilms(films);
    }

    /**
     * Показывает состояние, когда ничего не найдено.
     */
    private void showEmptyResult() {
        binding.recyclerViewFilms.setVisibility(View.GONE);
        binding.textViewEmpty.setVisibility(View.VISIBLE);
        adapter.setFilms(Collections.emptyList());
    }

    /**
     * Управляет видимостью индикатора загрузки.
     * @param isLoading true, если нужно показать индикатор, иначе false.
     */
    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            // Скрываем другие элементы на время загрузки
            binding.recyclerViewFilms.setVisibility(View.GONE);
            binding.textViewEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Очистка binding для избежания утечек памяти
    }

    @Override
    public void onItemClick(SearchResultFilm film) {
        // Обработка нажатия на элемент списка
        Bundle bundle = new Bundle();
        bundle.putInt("kinopoiskId", film.getFilmId());
        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_findFragment_to_filmDetailFragment, bundle);
    }
}