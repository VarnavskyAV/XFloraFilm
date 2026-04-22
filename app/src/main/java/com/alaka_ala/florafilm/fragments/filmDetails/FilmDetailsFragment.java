package com.alaka_ala.florafilm.fragments.filmDetails;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentFilmDetailsBinding;
import com.alaka_ala.florafilm.activities.MainActivity;
import com.alaka_ala.florafilm.data.media.PlayerLaunchData;
import com.alaka_ala.florafilm.utils.balancers.Balancer;
import com.alaka_ala.florafilm.utils.balancers.alloha.AllohaApiClient;
import com.alaka_ala.florafilm.utils.balancers.hdvb.HDVB;
import com.alaka_ala.florafilm.utils.settings.AppPreferences;
import com.alaka_ala.unofficial_kinopoisk_api.api.KinopoiskApiClientV2;
import com.alaka_ala.unofficial_kinopoisk_api.db.*;
import com.alaka_ala.unofficial_kinopoisk_api.models.*;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FilmDetailsFragment extends Fragment {

    private FragmentFilmDetailsBinding binding;
    private FilmDetails filmDetails;
    private SelectorVoiceAdapter adapter;
    private int kinopoiskId;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicInteger loadedBalancers = new AtomicInteger(0);
    private int totalBalancers = 0;

    private FilmDetailsDao dao;
    private KinopoiskApiClientV2 api;

    // ===================== LIFECYCLE =====================

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFilmDetailsBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        kinopoiskId = requireArguments().getInt("kinopoiskId");

        api = KinopoiskApiClientV2.getInstance();
        dao = KinopoiskDatabaseV2.getDatabase(getContext()).filmDetailsDao();

        setupRecycler();
        loadFilmDetails();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        tryShowResumeButton();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideBottomNavigationView();
        }
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
        executor.execute(() -> dao.insertAndPreservePositions(filmDetails));
        getActivity().invalidateOptionsMenu(); // Перерисовываем меню
        return super.onOptionsItemSelected(item);
    }

    // ===================== RESUME LOGIC =====================

    private static class ResumeData {
        String title;
        PlayerLaunchData launchData;
    }

    private ResumeData buildResumeData() {
        if (filmDetails == null) return null;

        List<Integer> path = filmDetails.getSelectedIndexPath();
        if (path == null || path.isEmpty()) return null;

        if (adapter == null || adapter.getRootFolders().isEmpty()) return null;

        int balancer = path.get(0);
        if (balancer >= adapter.getRootFolders().size()) return null;

        // Проверка активности балансера
        if (balancer == Balancer.ALLOHA_ID &&
                !AppPreferences.CDNSettings.Alloha.isAllohaActive(getContext())) return null;

        if (balancer == Balancer.HDVB_ID &&
                !AppPreferences.CDNSettings.HDVB.isHDVBActive(getContext())) return null;

        SelectorVoiceAdapter.Folder balancerFolder = adapter.getRootFolders().get(balancer);

        try {
            ResumeData data = new ResumeData();

            if (filmDetails.isSerial()) {
                if (path.size() < 5) return null;

                int season = path.get(1);
                int episode = path.get(2);
                int voice = path.get(3);
                int quality = path.get(4);

                SelectorVoiceAdapter.Folder seasonFolder =
                        (SelectorVoiceAdapter.Folder) balancerFolder.children.get(season);

                SelectorVoiceAdapter.Folder episodeFolder =
                        (SelectorVoiceAdapter.Folder) seasonFolder.children.get(episode);

                SelectorVoiceAdapter.Folder voiceFolder =
                        (SelectorVoiceAdapter.Folder) episodeFolder.children.get(voice);

                SelectorVoiceAdapter.File file =
                        (SelectorVoiceAdapter.File) voiceFolder.children.get(quality);

                data.title = balancerFolder.name + " / "
                        + seasonFolder.name + " / "
                        + episodeFolder.name;

                data.launchData = new PlayerLaunchData(balancer, adapter.getRootFolders(), path);

            } else {
                if (path.size() < 3) return null;

                int voice = path.get(1);
                int quality = path.get(2);

                SelectorVoiceAdapter.Folder voiceFolder =
                        (SelectorVoiceAdapter.Folder) balancerFolder.children.get(voice);

                SelectorVoiceAdapter.File file =
                        (SelectorVoiceAdapter.File) voiceFolder.children.get(quality);

                data.title = balancerFolder.name + " / "
                        + voiceFolder.name + " / "
                        + file.name;

                data.launchData = new PlayerLaunchData(balancer, adapter.getRootFolders(), path);
            }

            return data;

        } catch (Exception e) {
            return null;
        }
    }

    private void tryShowResumeButton() {
        ResumeData data = buildResumeData();

        if (data == null) {
            binding.resumeButtonRootLayout.setVisibility(GONE);
            return;
        }

        binding.resumeButtonRootLayout.setVisibility(VISIBLE);
        binding.textViewResumeWatch.setText(data.title);

        binding.buttonResumeWatch.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("playerLaunchData", data.launchData);
            bundle.putInt("kinopoiskId", kinopoiskId);

            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_filmDetailsFragment_to_playerFragment, bundle);
        });
    }

    // ===================== DATA =====================

    private void loadFilmDetails() {
        api.getFilmDetails(kinopoiskId, false, new KinopoiskApiClientV2.ApiCallback<>() {
            @Override
            public void onSuccess(FilmDetails result) {
                filmDetails = result;
                executor.execute(() -> dao.insertAndPreservePositions(result));

                new Handler(Looper.getMainLooper()).post(() -> {
                    populate(result);
                    tryShowResumeButton();
                });
            }

            @Override
            public void onError(KinopoiskApiClientV2.ApiException error) {
                Toast.makeText(getContext(), "Ошибка", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBalancers() {
        adapter.clearData();
        loadedBalancers.set(0);
        totalBalancers = 0;

        if (AppPreferences.CDNSettings.HDVB.isHDVBActive(getContext())) totalBalancers++;
        if (AppPreferences.CDNSettings.Alloha.isAllohaActive(getContext())) totalBalancers++;

        if (AppPreferences.CDNSettings.HDVB.isHDVBActive(getContext())) {
            new HDVB(getString(R.string.api_key_hdvb))
                    .fetch(kinopoiskId, new SelectorVoiceAdapter.AdapterData.AdapterDataCallback() {
                        @Override
                        public void onDataReady(SelectorVoiceAdapter.AdapterData data) {
                            adapter.addData(data.getRootFolders());
                            onBalancerLoaded();
                        }

                        @Override
                        public void onError(String error) {
                            onBalancerLoaded();
                        }
                    });
        }

        if (AppPreferences.CDNSettings.Alloha.isAllohaActive(getContext())) {
            executor.execute(() -> {
                try {
                    new AllohaApiClient(getResources().getString(R.string.api_key_alloha))
                            .fetch(kinopoiskId, new SelectorVoiceAdapter.AdapterData.AdapterDataCallback() {
                                @Override
                                public void onDataReady(SelectorVoiceAdapter.AdapterData data) {
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        adapter.addData(data.getRootFolders());
                                        onBalancerLoaded();
                                    });
                                }

                                @Override
                                public void onError(String error) {
                                    onBalancerLoaded();
                                }
                            });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void onBalancerLoaded() {
        if (loadedBalancers.incrementAndGet() == totalBalancers) {
            new Handler(Looper.getMainLooper()).post(this::tryShowResumeButton);
        }
    }

    // ===================== UI =====================

    private void setupRecycler() {
        adapter = new SelectorVoiceAdapter(file -> {
            PlayerLaunchData data = new PlayerLaunchData(
                    file.getIndexPath().get(0),
                    adapter.getRootFolders(),
                    file.getIndexPath()
            );

            executor.execute(() -> {
                filmDetails.setSelectedIndexPath(data.getSelectedIndexPath());
                dao.insertAndPreservePositions(filmDetails);
            });

            Bundle bundle = new Bundle();
            bundle.putSerializable("playerLaunchData", data);
            bundle.putInt("kinopoiskId", kinopoiskId);

            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_filmDetailsFragment_to_playerFragment, bundle);
        });

        binding.rvItemFilm.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvItemFilm.setAdapter(adapter);

        loadBalancers();
    }

    private void populate(FilmDetails film) {
        if (film.getBestPoster() != null) {
            Glide.with(this).load(film.getBestPoster()).into(binding.filmDetailsPoster);
        }

        binding.filmDetailsTitle.setText(film.getBestName());
        binding.filmDetailsDescription.setText(film.getDescription());

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

    // ===================== DESTROY =====================

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}