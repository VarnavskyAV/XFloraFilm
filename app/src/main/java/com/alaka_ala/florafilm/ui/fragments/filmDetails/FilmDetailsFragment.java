package com.alaka_ala.florafilm.ui.fragments.filmDetails;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentFilmDetailsBinding;
import com.alaka_ala.florafilm.ui.activities.MainActivity;
import com.alaka_ala.florafilm.ui.media.PlayerLaunchData;
import com.alaka_ala.florafilm.ui.utils.hdvb.HDVB;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.api.KinopoiskApiClientV2;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.db.FilmDetailsDao;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.db.KinopoiskDatabaseV2;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models.Country;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models.FilmDetails;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models.Genre;
import com.alaka_ala.florafilm.ui.utils.preferences.AppPreferences;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FilmDetailsFragment extends Fragment {
    private FragmentFilmDetailsBinding binding;
    private KinopoiskApiClientV2 kinopoiskApiClientV2;
    private FilmDetailsDao filmDetailsDao;
    private FilmDetails filmDetails;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private SelectorVoiceAdapter adapter;
    private int kinopoiskId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFilmDetailsBinding.inflate(inflater, container, false);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideBottomNavigationView();
        }

        kinopoiskApiClientV2 = KinopoiskApiClientV2.getInstance();
        filmDetailsDao = KinopoiskDatabaseV2.getDatabase(getContext()).filmDetailsDao();

        assert getArguments() != null;
        kinopoiskId = getArguments().getInt("kinopoiskId");

        setupViews();
        loadFilmDetails();

        return binding.getRoot();
    }

    private void setupViews() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.contentGroup.setVisibility(View.GONE);
        binding.appBarLayout.setVisibility(View.GONE);
        binding.nstdScrollView.setVisibility(View.GONE);

        RecyclerView rvitemFilm = binding.rvItemFilm;
        rvitemFilm.setLayoutManager(new LinearLayoutManager(getContext()));

        // Всегда проверяем что бы адаптер был создан только один раз при создании фрагмента на один фильм
        if (adapter == null) {
            adapter = new SelectorVoiceAdapter(file -> {
                if (filmDetails == null) {
                    Toast.makeText(getContext(), "Данные о фильме еще не загружены", Toast.LENGTH_SHORT).show();
                    return;
                }

                PlayerLaunchData launchData = new PlayerLaunchData(
                        adapter.getRootFolders(),
                        file.getIndexPath()
                );

                Bundle bundle = new Bundle();
                bundle.putSerializable("playerLaunchData", launchData);
                bundle.putInt("kinopoiskId", kinopoiskId);

                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_filmDetailsFragment_to_playerFragment, bundle);
            });
        }
        rvitemFilm.setAdapter(adapter);

        if (adapter.getRootFolders().isEmpty()) {
            loadBalancerData();
        }
    }

    private void loadFilmDetails() {
        if (filmDetails != null) {
            populateViews(filmDetails);
            binding.progressBar.setVisibility(View.GONE);
            binding.contentGroup.setVisibility(View.VISIBLE);
            binding.appBarLayout.setVisibility(View.VISIBLE);
            binding.nstdScrollView.setVisibility(View.VISIBLE);
            return;
        }
        kinopoiskApiClientV2.getFilmDetails(kinopoiskId, false, new KinopoiskApiClientV2.ApiCallback<>() {
            @Override
            public void onSuccess(FilmDetails result) {
                filmDetails = result;
                executorService.execute(() -> filmDetailsDao.insertAndPreservePositions(filmDetails));

                new Handler(Looper.getMainLooper()).post(() -> {
                    populateViews(result);
                    binding.progressBar.setVisibility(View.GONE);
                    binding.contentGroup.setVisibility(View.VISIBLE);
                    binding.appBarLayout.setVisibility(View.VISIBLE);
                    binding.nstdScrollView.setVisibility(View.VISIBLE);
                    if (!filmDetails.isSerial()) {
                        binding.btnResumeView.setText(filmDetails.getPositionView() != 0 ? "Продолжить просмотр" : "Начать просмотр");
                    }
                    if (getActivity() != null) {
                        getActivity().invalidateOptionsMenu();
                    }
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
    }

    private void loadBalancerData() {
        adapter.clearData();
        if (AppPreferences.CDNSettings.HDVB.isHDVBActive(getContext())) {
            HDVB hdvb = new HDVB(getResources().getString(R.string.api_key_hdvb));
            hdvb.getAdapterData(kinopoiskId, new HDVB.AdapterDataCallback() {
                @Override
                public void onDataReady(HDVB.AdapterData data) {
                    adapter.addData(data.getRootFolders());
                }

                @Override
                public void onError(String error) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "HDVB: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


    }

    private void populateViews(FilmDetails film) {
        setHasOptionsMenu(true);
        if (film.getBestPoster() != null && !film.getBestPoster().isEmpty()) {
            Glide.with(binding.getRoot()).load(film.getBestPoster()).into(binding.filmDetailsPoster);
        }
        if (film.getBestName() != null && !film.getBestName().isEmpty()) {
            binding.filmDetailsTitle.setText(film.getBestName());
        }
        if (film.getSlogan() != null && !film.getSlogan().isEmpty()) {
            binding.filmDetailsSlogan.setText(film.getSlogan());
        }
        if (film.getDescription() != null && !film.getDescription().isEmpty()) {
            binding.filmDetailsDescription.setText(film.getDescription());
        }

        binding.chipsGroup.removeAllViews();
        if (film.getRatingKinopoisk() != null) {
            addChip("Кинопоиск: " + film.getRatingKinopoisk(), "ratingKinopoisk", String.valueOf(film.getRatingKinopoisk()));
        }
        if (film.getRatingImdb() != null) {
            addChip("IMDb: " + film.getRatingImdb(), null, null);
        }
        if (film.getYear() != null) {
            addChip("Год: " + film.getYear(), "year", film.getYear());
        }
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                addChip(genre.getName(), "genre", genre.getName());
            }
        }
        if (film.getCountries() != null) {
            for (Country country : film.getCountries()) {
                addChip(country.getName(), "country", country.getName());
            }
        }
    }

    private void addChip(String text, String type, String data) {
        Chip chip = new Chip(getContext());
        chip.setText(text);
        if (type != null && data != null) {
            chip.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("type", type);
                bundle.putString("data", data);
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_filmDetailsFragment_to_filtersListFragment, bundle);
            });
        } else {
            chip.setOnClickListener(v -> Toast.makeText(getContext(), "Фильтр недоступен", Toast.LENGTH_SHORT).show());
        }
        binding.chipsGroup.addView(chip);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        if (filmDetails != null) {
            if (filmDetails.isBookmark()) {
                menu.add("Удалить из избранного").setIcon(R.drawable.bookmark_added).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            } else {
                menu.add("Добавить в избранное").setIcon(R.drawable.bookmark_add).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
            if (filmDetails.isObserveUpdateVoice()) {
                menu.add("Не уведомлять о новых озвучках").setIcon(R.drawable.voice).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            } else {
                menu.add("Уведомить о новых озвучках").setIcon(R.drawable.voice2).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String title = item.getTitle().toString();
        switch (title) {
            case "Добавить в избранное":
                filmDetails.setIsBookmark(true);
                break;
            case "Удалить из избранного":
                filmDetails.setIsBookmark(false);
                break;
            case "Уведомить о новых озвучках":
                filmDetails.setObserveUpdateVoice(true);
                break;
            case "Не уведомлять о новых озвучках":
                filmDetails.setObserveUpdateVoice(false);
                break;
        }
        executorService.execute(() -> filmDetailsDao.insertAndPreservePositions(filmDetails));
        getActivity().invalidateOptionsMenu(); // Перерисовываем меню
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
