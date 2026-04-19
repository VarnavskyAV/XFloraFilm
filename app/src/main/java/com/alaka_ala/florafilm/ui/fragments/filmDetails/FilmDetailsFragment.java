package com.alaka_ala.florafilm.ui.fragments.filmDetails;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentFilmDetailsBinding;
import com.alaka_ala.florafilm.ui.activities.MainActivity;
import com.alaka_ala.florafilm.data.media.PlayerLaunchData;
import com.alaka_ala.florafilm.utils.balancers.Balancer;
import com.alaka_ala.florafilm.utils.balancers.alloha.AllohaApiClient;
import com.alaka_ala.florafilm.utils.balancers.hdvb.HDVB;
import com.alaka_ala.florafilm.utils.settings.AppPreferences;
import com.alaka_ala.unofficial_kinopoisk_api.api.KinopoiskApiClientV2;
import com.alaka_ala.unofficial_kinopoisk_api.db.FilmDetailsDao;
import com.alaka_ala.unofficial_kinopoisk_api.db.KinopoiskDatabaseV2;
import com.alaka_ala.unofficial_kinopoisk_api.models.Country;
import com.alaka_ala.unofficial_kinopoisk_api.models.FilmDetails;
import com.alaka_ala.unofficial_kinopoisk_api.models.Genre;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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

    // Счетчик загруженных балансеров
    private final AtomicInteger loadedBalancersCount = new AtomicInteger(0);
    private int totalBalancersToLoad = 0;

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

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideBottomNavigationView();
        }
        // Пытаемся показать кнопку, если данные уже загружены
        tryShowResumeButton();
    }

    private void tryShowResumeButton() {
        // Ждем загрузки filmDetails и данных адаптера
        if (filmDetails != null && !adapter.getRootFolders().isEmpty()) {
            checkAndShowResumeButton();
        } else {
            binding.resumeButtonRootLayout.setVisibility(GONE);
        }
    }

    private void checkAndShowResumeButton() {
        if (getContext() == null) return;
        if (filmDetails == null || filmDetails.getSelectedIndexPath() == null || filmDetails.getSelectedIndexPath().isEmpty()) {
            binding.resumeButtonRootLayout.setVisibility(GONE);
            return;
        }

        List<Integer> selectedPath = filmDetails.getSelectedIndexPath();
        if (selectedPath.isEmpty()) {
            binding.resumeButtonRootLayout.setVisibility(GONE);
            return;
        }

        int selectedBalancer = selectedPath.get(0);

        // Проверяем, доступен ли выбранный балансер
        boolean isBalancerAvailable = false;
        if (selectedBalancer == Balancer.ALLOHA_ID) {
            isBalancerAvailable = AppPreferences.CDNSettings.Alloha.isAllohaActive(getContext());
        } else if (selectedBalancer == Balancer.HDVB_ID) {
            isBalancerAvailable = AppPreferences.CDNSettings.HDVB.isHDVBActive(getContext());
        }

        // Проверяем, есть ли выбранный балансер в адаптере
        boolean isBalancerInAdapter = selectedBalancer < adapter.getRootFolders().size();

        if (isBalancerAvailable && isBalancerInAdapter && !adapter.getRootFolders().isEmpty()) {
            resumePopulateButton();
        } else {
            binding.resumeButtonRootLayout.setVisibility(GONE);
        }
    }

    private void setupViews() {
        binding.progressBar.setVisibility(VISIBLE);
        binding.contentGroup.setVisibility(GONE);
        binding.appBarLayout.setVisibility(GONE);
        binding.nstdScrollView.setVisibility(GONE);

        RecyclerView rvitemFilm = binding.rvItemFilm;
        rvitemFilm.setLayoutManager(new LinearLayoutManager(getContext()));

        // Всегда проверяем что бы адаптер был создан только один раз при создании фрагмента на один фильм
        if (adapter == null) {

            adapter = new SelectorVoiceAdapter(file -> {
                if (filmDetails == null) {
                    Toast.makeText(getContext(), "Данные о фильме еще не загружены", Toast.LENGTH_SHORT).show();
                    return;
                }

                int sourceType = file.getIndexPath().get(0); // balancer
                PlayerLaunchData launchData = new PlayerLaunchData(
                        sourceType,
                        adapter.getRootFolders(),
                        file.getIndexPath()
                );

                // Сохраняем выбранную позицию просмотра.
                executorService.execute(() -> {
                    filmDetails.setSelectedIndexPath(launchData.getSelectedIndexPath());
                    filmDetails.setIsStartView(true);
                    filmDetailsDao.insertAndPreservePositions(filmDetails);
                });

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
            binding.progressBar.setVisibility(GONE);
            binding.contentGroup.setVisibility(VISIBLE);
            binding.appBarLayout.setVisibility(VISIBLE);
            binding.nstdScrollView.setVisibility(VISIBLE);
            return;
        }
        kinopoiskApiClientV2.getFilmDetails(kinopoiskId, false, new KinopoiskApiClientV2.ApiCallback<>() {
            @Override
            public void onSuccess(FilmDetails result) {
                filmDetails = result;
                executorService.execute(() -> filmDetailsDao.insertAndPreservePositions(filmDetails));

                new Handler(Looper.getMainLooper()).post(() -> {
                    populateViews(result);
                    binding.progressBar.setVisibility(GONE);
                    binding.contentGroup.setVisibility(VISIBLE);
                    binding.appBarLayout.setVisibility(VISIBLE);
                    binding.nstdScrollView.setVisibility(VISIBLE);
                    // После загрузки деталей фильма пробуем показать кнопку
                    tryShowResumeButton();
                });
            }

            @Override
            public void onError(KinopoiskApiClientV2.ApiException error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    binding.progressBar.setVisibility(GONE);
                    Toast.makeText(getContext(), "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadBalancerData() {
        adapter.clearData();
        loadedBalancersCount.set(0);
        totalBalancersToLoad = 0;

        if (getContext() == null) return;
        // Считаем сколько балансеров будем загружать
        if (AppPreferences.CDNSettings.HDVB.isHDVBActive(getContext())) {
            totalBalancersToLoad++;
        }
        if (AppPreferences.CDNSettings.Alloha.isAllohaActive(getContext())) {
            totalBalancersToLoad++;
        }

        // hdvb
        if (AppPreferences.CDNSettings.HDVB.isHDVBActive(getContext())) {
            HDVB hdvb = new HDVB(getResources().getString(R.string.api_key_hdvb));
            hdvb.fetch(kinopoiskId, new SelectorVoiceAdapter.AdapterData.AdapterDataCallback() {
                @Override
                public void onDataReady(SelectorVoiceAdapter.AdapterData data) {
                    adapter.addData(data.getRootFolders());
                    onBalancerLoaded();
                }

                @Override
                public void onError(String error) {
                    if (getContext() != null) {
                        AlertDialog alert = new AlertDialog.Builder(getContext()).create();
                        alert.setTitle("HDVB");
                        alert.setMessage(error);
                        alert.show();
                    }
                    onBalancerLoaded();
                }
            });
        }

        // alloha
        if (AppPreferences.CDNSettings.Alloha.isAllohaActive(getContext())) {
            AllohaApiClient allohaApiClient = new AllohaApiClient("4cd98e08f1e1f0273692e35b16b690");
            executorService.execute(() -> {
                try {
                    allohaApiClient.fetch(kinopoiskId, new SelectorVoiceAdapter.AdapterData.AdapterDataCallback() {
                        @Override
                        public void onDataReady(SelectorVoiceAdapter.AdapterData data) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                adapter.addData(data.getRootFolders());
                                onBalancerLoaded();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                AlertDialog alert = new AlertDialog.Builder(getContext()).create();
                                alert.setTitle("ALLOHA");
                                alert.setMessage(error);
                                alert.show();
                                onBalancerLoaded();
                            });
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        AlertDialog alert = new AlertDialog.Builder(getContext()).create();
                        alert.setTitle("ALLOHA");
                        alert.setMessage(e.getMessage());
                        alert.show();
                        onBalancerLoaded();
                    });
                }
            });
        }
    }

    private void onBalancerLoaded() {
        int loaded = loadedBalancersCount.incrementAndGet();
        // Когда все балансеры загружены, пробуем показать кнопку
        if (loaded == totalBalancersToLoad) {
            new Handler(Looper.getMainLooper()).post(() -> tryShowResumeButton());
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

    private boolean isValidSelection(int balancerIndex, int seasonIndex, int episodeIndex, int voiceIndex, int qualityIndex) {
        // Проверяем балансер
        if (balancerIndex >= adapter.getRootFolders().size()) {
            return false;
        }

        SelectorVoiceAdapter.Folder balancerFolder = adapter.getRootFolders().get(balancerIndex);

        // Для сериала проверяем сезоны, серии и озвучки
        if (filmDetails.isSerial()) {
            // Проверяем сезон
            if (seasonIndex >= balancerFolder.children.size()) {
                return false;
            }

            SelectorVoiceAdapter.Folder seasonFolder = (SelectorVoiceAdapter.Folder) balancerFolder.children.get(seasonIndex);

            // Проверяем серию
            if (episodeIndex >= seasonFolder.children.size()) {
                return false;
            }

            // Проверяем озвучку
            if (voiceIndex >= balancerFolder.children.size()) {
                return false;
            }

            // Проверяем качество
            if (qualityIndex >= balancerFolder.children.size()) {
                return false;
            }
        } else {
            // Для фильма проверяем озвучку и качество
            if (voiceIndex >= balancerFolder.children.size()) {
                return false;
            }

            SelectorVoiceAdapter.Folder voiceFolder = (SelectorVoiceAdapter.Folder) balancerFolder.children.get(voiceIndex);

            if (qualityIndex >= voiceFolder.children.size()) {
                return false;
            }
        }

        return true;
    }

    @SuppressLint("SetTextI18n")
    @OptIn(markerClass = UnstableApi.class)
    private void resumePopulateButton() {
        if (filmDetails == null) return;
        List<Integer> selectableIndexPath = filmDetails.getSelectedIndexPath();
        if (selectableIndexPath == null || selectableIndexPath.isEmpty()) return;

        if (adapter.getRootFolders().isEmpty()) {
            binding.resumeButtonRootLayout.setVisibility(GONE);
            return;
        }

        final int INDEX_BALANCER = 0;
        final int INDEX_SEASON = 1;
        final int INDEX_EPISODES = 2;
        final int INDEX_VOICE = 3;
        final int INDEX_QUALITY = 4;

        int balancer = selectableIndexPath.get(INDEX_BALANCER);

        if (balancer >= adapter.getRootFolders().size()) {
            binding.resumeButtonRootLayout.setVisibility(GONE);
            return;
        }

        if (filmDetails.isSerial()) {
            if (selectableIndexPath.size() <= INDEX_QUALITY) {
                binding.resumeButtonRootLayout.setVisibility(GONE);
                return;
            }

            int season = selectableIndexPath.get(INDEX_SEASON);
            int episode = selectableIndexPath.get(INDEX_EPISODES);
            int voice = selectableIndexPath.get(INDEX_VOICE);
            int quality = selectableIndexPath.get(INDEX_QUALITY);

            if (!isValidSelection(balancer, season, episode, voice, quality)) {
                binding.resumeButtonRootLayout.setVisibility(GONE);
                return;
            }

            // Безопасное получение данных
            SelectorVoiceAdapter.Folder balancerFolder = adapter.getRootFolders().get(balancer);

            if (season >= balancerFolder.children.size()) {
                binding.resumeButtonRootLayout.setVisibility(GONE);
                return;
            }

            SelectorVoiceAdapter.Folder seasonFolder = (SelectorVoiceAdapter.Folder) balancerFolder.children.get(season);

            if (episode >= seasonFolder.children.size()) {
                binding.resumeButtonRootLayout.setVisibility(GONE);
                return;
            }

            if (voice >= balancerFolder.children.size()) {
                binding.resumeButtonRootLayout.setVisibility(GONE);
                return;
            }

            if (quality >= balancerFolder.children.size()) {
                binding.resumeButtonRootLayout.setVisibility(GONE);
                return;
            }

            String titleBalancer = balancerFolder.name;
            String titleSeason = seasonFolder.name;
            String titleEpisode = ((SelectorVoiceAdapter.Folder) seasonFolder.children.get(episode)).name;
            String titleVoice = ((SelectorVoiceAdapter.Folder) balancerFolder.children.get(voice)).name;
            String titleQuality = ((SelectorVoiceAdapter.Folder) balancerFolder.children.get(quality)).name;

            binding.resumeButtonRootLayout.setVisibility(VISIBLE);
            binding.textViewResumeWatch.setText(titleBalancer + " / Сезон - " + titleSeason + " / Серия - " + titleEpisode + " / Озвучка - " + titleVoice + " / Качество - " + titleQuality);

        } else {
            if (selectableIndexPath.size() <= INDEX_QUALITY) {
                binding.resumeButtonRootLayout.setVisibility(GONE);
                return;
            }

            int voice = selectableIndexPath.get(INDEX_VOICE);
            int quality = selectableIndexPath.get(INDEX_QUALITY);

            if (!isValidSelection(balancer, 0, 0, voice, quality)) {
                binding.resumeButtonRootLayout.setVisibility(GONE);
                return;
            }

            SelectorVoiceAdapter.Folder balancerFolder = adapter.getRootFolders().get(balancer);

            if (voice >= balancerFolder.children.size()) {
                binding.resumeButtonRootLayout.setVisibility(GONE);
                return;
            }

            String balancerTitle = balancer == 0 ? "HDVB" : "ALLOHA";
            String titleVoice = ((SelectorVoiceAdapter.Folder) balancerFolder.children.get(voice)).name;

            SelectorVoiceAdapter.Folder voiceFolder = (SelectorVoiceAdapter.Folder) balancerFolder.children.get(voice);
            if (quality >= voiceFolder.children.size()) {
                binding.resumeButtonRootLayout.setVisibility(GONE);
                return;
            }

            String titleQuality = ((SelectorVoiceAdapter.File) voiceFolder.children.get(quality)).name;

            binding.resumeButtonRootLayout.setVisibility(VISIBLE);
            binding.textViewResumeWatch.setText(balancerTitle + " / Озвучка - " + titleVoice + " / Качество - " + titleQuality);
        }

        binding.buttonResumeWatch.setOnClickListener(view -> {
            PlayerLaunchData launchData = null;

            if (AppPreferences.CDNSettings.Alloha.isAllohaActive(getContext())) {
                if (filmDetails.getSelectedIndexPath().get(0).equals(Balancer.ALLOHA_ID)) {
                    launchData = new PlayerLaunchData(
                            Balancer.ALLOHA_ID,
                            adapter.getRootFolders(),
                            filmDetails.getSelectedIndexPath()
                    );
                }
            }

            if (launchData == null && AppPreferences.CDNSettings.HDVB.isHDVBActive(getContext())) {
                if (filmDetails.getSelectedIndexPath().get(0).equals(Balancer.HDVB_ID)) {
                    launchData = new PlayerLaunchData(
                            Balancer.HDVB_ID,
                            adapter.getRootFolders(),
                            filmDetails.getSelectedIndexPath()
                    );
                }
            }

            if (launchData == null) {
                Toast.makeText(getContext(), "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putSerializable("playerLaunchData", launchData);
            bundle.putInt("kinopoiskId", kinopoiskId);

            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_filmDetailsFragment_to_playerFragment, bundle);
        });
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
        requireActivity().invalidateOptionsMenu();
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