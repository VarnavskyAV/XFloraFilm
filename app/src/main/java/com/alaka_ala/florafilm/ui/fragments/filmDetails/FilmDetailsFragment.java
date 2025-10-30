package com.alaka_ala.florafilm.ui.fragments.filmDetails;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentFilmDetailsBinding;
import com.alaka_ala.florafilm.ui.activities.MainActivity;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.api.KinopoiskApiClientV2;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.db.FilmDetailsDao;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.db.KinopoiskDatabaseV2;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models.Country;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models.FilmDetails;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models.Genre;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FilmDetailsFragment extends Fragment {
    private FragmentFilmDetailsBinding binding;
    private KinopoiskApiClientV2 kinopoiskApiClientV2;
    private FilmDetailsDao filmDetailsDao;
    private FilmDetails filmDetails;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFilmDetailsBinding.inflate(inflater, container, false);
        // скрываем нижнюю навигацию при выходе из этого фрагмента.
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideBottomNavigationView();
        }


        kinopoiskApiClientV2 = KinopoiskApiClientV2.getInstance();
        filmDetailsDao = KinopoiskDatabaseV2.getDatabase(getContext()).filmDetailsDao();

        // Получаем ID фильма из аргументов.
        assert getArguments() != null;
        int kinopoiskId = getArguments().getInt("kinopoiskId");

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.contentGroup.setVisibility(View.GONE);
        binding.appBarLayout.setVisibility(View.GONE);
        binding.nstdScrollView.setVisibility(View.GONE);

        kinopoiskApiClientV2.getFilmDetails(kinopoiskId, false, new KinopoiskApiClientV2.ApiCallback<FilmDetails>() {
            @Override
            public void onSuccess(FilmDetails result) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    populateViews(result);
                    binding.btnResumeView.setVisibility(result.isStartView() ? View.VISIBLE : View.GONE);
                    binding.btnResumeView.setText(result.isStartView() ? "Просмотр": "Прод. " + result.getFormattedViewPosition() + " мин."  );
                    binding.progressBar.setVisibility(View.GONE);
                    binding.contentGroup.setVisibility(View.VISIBLE);
                    binding.appBarLayout.setVisibility(View.VISIBLE);
                    binding.nstdScrollView.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onError(KinopoiskApiClientV2.ApiException error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                });
            }
        });


        return binding.getRoot();
    }

    /**
     * Заполняет все View данными из объекта FilmDetails.
     * Если какие-то данные отсутствуют, соответствующие View будут скрыты.
     *
     * @param film объект FilmDetails с данными о фильме.
     */
    private void populateViews(FilmDetails film) {
        this.filmDetails = film;
        setHasOptionsMenu(true);
        // Загружаем постер
        if (film.getBestPoster() != null && !film.getBestPoster().isEmpty()) {
            Glide.with(binding.getRoot()).load(film.getBestPoster()).into(binding.filmDetailsPoster);
        } else {
            binding.filmDetailsPoster.setVisibility(View.GONE);
        }

        // Устанавливаем заголовок
        if (film.getBestName() != null && !film.getBestName().isEmpty()) {
            binding.filmDetailsTitle.setText(film.getBestName());
        } else {
            binding.filmDetailsTitle.setVisibility(View.GONE);
        }

        // Устанавливаем слоган
        if (film.getSlogan() != null && !film.getSlogan().isEmpty()) {
            binding.filmDetailsSlogan.setText(film.getSlogan());
        } else {
            binding.filmDetailsSlogan.setVisibility(View.GONE);
        }

        // Устанавливаем описание
        if (film.getDescription() != null && !film.getDescription().isEmpty()) {
            binding.filmDetailsDescription.setText(film.getDescription());
        } else {
            binding.filmDetailsDescription.setVisibility(View.GONE);
            binding.filmDetailsDescriptionLabel.setVisibility(View.GONE);
        }

        int chipIndex = 0;
        // Добавляем Chip с рейтингами и годом
        if (film.getRatingKinopoisk() != null) {
            Chip chip = new Chip(getContext());
            chip.setText("Кинопоиск: " + film.getRatingKinopoisk());
            chip.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("type", "ratingKinopoisk");
                bundle.putString("data", String.valueOf(film.getRatingKinopoisk()));
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_filmDetailsFragment_to_filtersListFragment, bundle);
            });
            binding.chipsGroup.addView(chip, chipIndex++);
        }

        if (film.getRatingImdb() != null) {
            Chip chip = new Chip(getContext());
            chip.setText("IMDb: " + film.getRatingImdb());
            chip.setOnClickListener(v -> {
                /*Bundle bundle = new Bundle();
                bundle.putString("type", "ratingImdb");
                bundle.putString("data", String.valueOf(film.getRatingImdb()));
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_filmDetailsFragment_to_filtersListFragment, bundle);*/
                Toast.makeText(getContext(), "Фильтр по рейтингу IMDb не доступен", Toast.LENGTH_SHORT).show();
            });
            binding.chipsGroup.addView(chip, chipIndex++);
        }

        if (film.getYear() != null) {
            Chip chip = new Chip(getContext());
            chip.setText("Год: " + film.getYear());
            chip.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("type", "year");
                bundle.putString("data", film.getYear());
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_filmDetailsFragment_to_filtersListFragment, bundle);
            });
            binding.chipsGroup.addView(chip, chipIndex++);
        }

        // Добавляем жанры в ChipGroup
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                Chip chip = new Chip(getContext());
                chip.setText(genre.getName());
                chip.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "genre");
                    bundle.putString("data", genre.getName());
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.action_filmDetailsFragment_to_filtersListFragment, bundle);
                });
                binding.chipsGroup.addView(chip);
            }
        }

        if (film.getCountries() != null && !film.getCountries().isEmpty()) {
            for (Country country : film.getCountries()) {
                Chip chip = new Chip(getContext());
                chip.setText(country.getName());
                chip.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "country");
                    bundle.putString("data", country.getName());
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.action_filmDetailsFragment_to_filtersListFragment, bundle);
                });
                binding.chipsGroup.addView(chip);
            }
        }

        if (binding.chipsGroup.getChildCount() == 0) {
            binding.chipsScrollView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (filmDetails.isBookmark()) {
            menu.add("Удалить из избраного").setIcon(R.drawable.heart_filled).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        } else {
            menu.add("Добавить в избранное").setIcon(R.drawable.heart_unfilled).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getTitle().equals("Добавить в избранное")) {
            if (filmDetails.isBookmark()) {
                filmDetails.setIsBookmark(false);
                item.setIcon(R.drawable.heart_unfilled);
            } else {
                filmDetails.setIsBookmark(true);
                item.setIcon(R.drawable.heart_filled);
            }
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    filmDetailsDao.insert(filmDetails);
                }
            });

        }

        return super.onOptionsItemSelected(item);
    }
}
