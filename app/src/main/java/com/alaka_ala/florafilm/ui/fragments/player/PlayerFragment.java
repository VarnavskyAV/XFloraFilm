package com.alaka_ala.florafilm.ui.fragments.player;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.analytics.AnalyticsListener;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentPlayerBinding;
import com.alaka_ala.florafilm.ui.activities.MainActivity;
import com.alaka_ala.florafilm.ui.media.PlayerLaunchData;
import com.alaka_ala.florafilm.ui.fragments.filmDetails.SelectorVoiceAdapter.File;
import com.alaka_ala.florafilm.ui.fragments.filmDetails.SelectorVoiceAdapter.Folder;
import com.alaka_ala.florafilm.ui.fragments.filmDetails.SelectorVoiceAdapter.Item;
import com.alaka_ala.florafilm.utils.hdvb.HDVB;
import com.alaka_ala.unofficial_kinopoisk_api.db.FilmDetailsDao;
import com.alaka_ala.unofficial_kinopoisk_api.db.KinopoiskDatabaseV2;
import com.alaka_ala.unofficial_kinopoisk_api.models.FilmDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@UnstableApi
public class PlayerFragment extends Fragment {

    // Константы
    private static final int INDEX_BALANCER = 0;
    private static final int INDEX_SEASON = 1;
    private static final int INDEX_EPISODES = 2;
    private static final int INDEX_VOICE = 3;
    private static final int INDEX_QUALITY = 4;
    private static final long SAVE_POSITION_INTERVAL_MS = 5000;

    // UI элементы
    private FragmentPlayerBinding binding;
    private ImageButton btnScreenRotate;
    private ImageButton btnAspectRatio;
    private LinearLayout customControlsLayout;

    // Gesture
    private GestureDetector gestureDetector;
    private PlayerGestureListener gestureListener;

    // Обработчики
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Handler savePositionHandler = new Handler(Looper.getMainLooper());

    // Плеер и данные
    private ExoPlayer player;
    private PlayerLaunchData launchData;
    private int kinopoiskId;
    private FilmDetails filmDetails;
    private ExecutorService executorService;
    private HDVB hdvb;
    private AnalyticsListener analyticsListener;
    private FilmDetailsDao filmDetailsDao;

    // Состояние
    private boolean isSerial;
    private boolean isDestroyed = false;
    private Map<String, Long> savedPositionsMap = new HashMap<>();
    private MainActivity mainActivity;
    private int originalOrientation;
    private boolean isScreenRotated = false;
    private boolean shouldRestoreOrientation = true;
    private boolean isControllerVisible = true; // Флаг видимости контроллера

    // Режимы соотношения сторон
    private int currentAspectRatio = AspectRatioFrameLayout.RESIZE_MODE_FIT;
    private final int[] aspectRatioModes = {
            AspectRatioFrameLayout.RESIZE_MODE_FIT,
            AspectRatioFrameLayout.RESIZE_MODE_FILL,
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM
    };
    private final int[] aspectRatioIcons = {
            R.drawable.ic_aspect_ratio,
            R.drawable.ic_aspect_ratio,
            R.drawable.ic_aspect_ratio
    };
    private final String[] aspectRatioNames = {
            "FIT",
            "FILL",
            "ZOOM"
    };

    // Runnable для периодических задач
    private final Runnable savePositionRunnable = new Runnable() {
        @Override
        public void run() {
            saveCurrentPosition();
            savePositionHandler.postDelayed(this, SAVE_POSITION_INTERVAL_MS);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPlayerBinding.inflate(inflater, container, false);
        mainActivity = (MainActivity) requireActivity();
        mainActivity.hideBottomNavigationView();
        mainActivity.hideToolbar();

        // Сохраняем исходную ориентацию активности
        originalOrientation = mainActivity.getRequestedOrientation();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeUI();
        initializeGestureListener();
        initializeExecutor();
    }

    private void initializeUI() {
        // Находим кнопки и layout
        btnScreenRotate = binding.getRoot().findViewById(R.id.btn_screen_rotate);
        btnAspectRatio = binding.getRoot().findViewById(R.id.btn_aspect_ratio);
        customControlsLayout = binding.getRoot().findViewById(R.id.custom_controls_layout);

        // Сразу переворачиваем экран по умолчанию в ландшафтную ориентацию
        mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        isScreenRotated = true;
        if (btnScreenRotate != null) {
            btnScreenRotate.setImageResource(R.drawable.ic_rotate);
        }

        if (btnScreenRotate != null) {
            btnScreenRotate.setOnClickListener(v -> toggleScreenOrientation());
        }

        if (btnAspectRatio != null) {
            btnAspectRatio.setOnClickListener(v -> toggleAspectRatio());
        }

        if (customControlsLayout != null) {
            // Показываем кнопки по умолчанию
            customControlsLayout.setVisibility(View.VISIBLE);
        }

        // Устанавливаем полноэкранный режим
        setFullscreen(true);

        // Настраиваем PlayerView
        binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);

        // Отключаем стандартный контроллер Media3, так как управление будет через наш GestureListener
        binding.playerView.setUseController(false);
    }

    private void initializeGestureListener() {
        // Создаем GestureListener
        gestureListener = new PlayerGestureListener(
                requireActivity(),
                player,
                binding.playerView,
                binding.centerFeedbackLayout,
                binding.centerFeedbackIcon,
                binding.centerFeedbackText,
                binding.centerFeedbackProgress,
                binding.speed2xText
        );

        // Устанавливаем слушатель одиночного тапа для показа/скрытия контроллера
        gestureListener.setOnSingleTapListener(() -> {
            toggleControllerVisibility();
        });

        // Создаем GestureDetector
        gestureDetector = new GestureDetector(requireContext(), gestureListener);

        // Обрабатываем touch события на PlayerView
        binding.playerView.setOnTouchListener((v, event) -> {
            // Передаем событие в GestureDetector
            boolean handled = gestureDetector.onTouchEvent(event);

            // Обрабатываем ACTION_UP для завершения жестов
            if (event.getAction() == MotionEvent.ACTION_UP) {
                gestureListener.onUp(event);
            }

            return handled;
        });

        // Устанавливаем long clickable для обработки долгого нажатия
        binding.playerView.setLongClickable(true);
    }

    private void toggleControllerVisibility() {
        if (binding.playerView == null) return;

        if (isControllerVisible) {
            // Скрываем контроллер
            binding.playerView.hideController();
            if (customControlsLayout != null) {
                customControlsLayout.setVisibility(View.GONE);
            }
            isControllerVisible = false;
        } else {
            // Показываем контроллер
            binding.playerView.showController();
            if (customControlsLayout != null) {
                customControlsLayout.setVisibility(View.VISIBLE);
            }
            isControllerVisible = true;

            // Автоматически скрываем через 3 секунды
            mainHandler.postDelayed(() -> {
                if (isControllerVisible && !isDestroyed) {
                    binding.playerView.hideController();
                    if (customControlsLayout != null) {
                        customControlsLayout.setVisibility(View.GONE);
                    }
                    isControllerVisible = false;
                }
            }, 3000);
        }
    }

    private void setFullscreen(boolean fullscreen) {
        if (fullscreen) {
            // Скрываем status bar
            Window window = requireActivity().getWindow();
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );

            // Скрываем navigation bar
            View decorView = window.getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);

        } else {
            // Показываем status bar
            Window window = requireActivity().getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            // Показываем navigation bar
            View decorView = window.getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    private void toggleScreenOrientation() {
        // Временно отключаем восстановление ориентации
        shouldRestoreOrientation = false;

        int currentOrientation = mainActivity.getRequestedOrientation();

        if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||
                currentOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            // Возвращаем в портретную ориентацию
            mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            isScreenRotated = false;
            if (btnScreenRotate != null) {
                btnScreenRotate.setImageResource(R.drawable.ic_rotate);
            }
            Toast.makeText(requireContext(), "Портретный режим", Toast.LENGTH_SHORT).show();
        } else {
            // Переключаем в ландшафтную ориентацию
            mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            isScreenRotated = true;
            if (btnScreenRotate != null) {
                btnScreenRotate.setImageResource(R.drawable.ic_rotate);
            }
            Toast.makeText(requireContext(), "Ландшафтный режим", Toast.LENGTH_SHORT).show();
        }

        // Через секунду снова включаем восстановление ориентации
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            shouldRestoreOrientation = true;
        }, 1000);
    }

    private void toggleAspectRatio() {
        // Переключаем режим соотношения сторон
        currentAspectRatio = (currentAspectRatio + 1) % aspectRatioModes.length;

        // Применяем новый режим к PlayerView
        binding.playerView.setResizeMode(aspectRatioModes[currentAspectRatio]);

        // Обновляем иконку кнопки
        if (btnAspectRatio != null) {
            btnAspectRatio.setImageResource(aspectRatioIcons[currentAspectRatio]);
        }

        // Показываем подсказку
        String message = "Режим: " + aspectRatioNames[currentAspectRatio];
        switch (aspectRatioModes[currentAspectRatio]) {
            case AspectRatioFrameLayout.RESIZE_MODE_FIT:
                message += " - видео помещается в экран с сохранением пропорций";
                break;
            case AspectRatioFrameLayout.RESIZE_MODE_FILL:
                message += " - видео заполняет экран (обрезаются края)";
                break;
            case AspectRatioFrameLayout.RESIZE_MODE_ZOOM:
                message += " - видео увеличивается (часть может быть скрыта)";
                break;
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void initializeExecutor() {
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(this::initializeData);
    }

    private void initializeData() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            showErrorAndReturn("Отсутствуют необходимые данные");
            return;
        }

        launchData = (PlayerLaunchData) arguments.getSerializable("playerLaunchData");
        kinopoiskId = arguments.getInt("kinopoiskId", -1);

        if (launchData == null || kinopoiskId == -1) {
            showErrorAndReturn("Некорректные данные для воспроизведения");
            return;
        }

        loadFilmDetails();
    }

    private void loadFilmDetails() {
        filmDetailsDao = KinopoiskDatabaseV2.getDatabase(requireContext()).filmDetailsDao();
        filmDetails = filmDetailsDao.getById(kinopoiskId);

        if (filmDetails == null) {
            showErrorAndReturn("Информация о фильме не найдена");
            return;
        }

        // Загружаем сохраненные позиции
        Map<String, Long> lastPositionPlayerView = filmDetails.getLastPositionPlayerView();
        if (lastPositionPlayerView != null) {
            savedPositionsMap.putAll(lastPositionPlayerView);
        }

        hdvb = new HDVB(getResources().getString(R.string.api_key_hdvb));
        isSerial = filmDetails.isSerial();

        mainHandler.post(() -> initializePlayer(launchData));
    }

    private void showErrorAndReturn(String message) {
        mainHandler.post(() -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initializePlayer(PlayerLaunchData launchData) {
        player = new ExoPlayer.Builder(requireContext()).build();
        binding.playerView.setPlayer(player);

        // Обновляем ссылку на player в gestureListener
        if (gestureListener != null) {
            // Используем рефлексию или добавляем метод для обновления player
            // Проще создать новый listener, но player уже может быть null в момент создания
            // Поэтому добавим метод setPlayer в PlayerGestureListener
            gestureListener.setPlayer(player);
        }

        // Настраиваем стандартный контроллер
        binding.playerView.setUseController(true);
        binding.playerView.setShowNextButton(true);
        binding.playerView.setShowPreviousButton(true);
        binding.playerView.setShowFastForwardButton(false);
        binding.playerView.setShowRewindButton(false);
        binding.playerView.setShowShuffleButton(false);
        binding.playerView.setShowSubtitleButton(true);
        binding.playerView.setShowVrButton(false);

        // Добавляем слушатель для автоматического сохранения позиции
        setupPlayerListeners();

        if (isSerial) {
            setupSerialPlayback(launchData);
        } else {
            setupMoviePlayback(launchData);
        }

        // Показываем контроллер при старте
        mainHandler.postDelayed(() -> {
            if (!isDestroyed && binding.playerView != null) {
                binding.playerView.showController();
                isControllerVisible = true;
            }
        }, 500);
    }

    private void setupPlayerListeners() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                switch (playbackState) {
                    case Player.STATE_READY:
                        // Начинаем периодическое сохранение позиции
                        if (!isDestroyed) {
                            savePositionHandler.post(savePositionRunnable);
                        }
                        break;
                    case Player.STATE_ENDED:
                        // При окончании воспроизведения сбрасываем позицию
                        resetCurrentPosition();
                        savePositionHandler.removeCallbacks(savePositionRunnable);
                        break;
                    case Player.STATE_IDLE:
                    case Player.STATE_BUFFERING:
                        savePositionHandler.removeCallbacks(savePositionRunnable);
                        break;
                }
            }

            @Override
            public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                if (!playWhenReady && !isDestroyed) {
                    // При паузе немедленно сохраняем позицию
                    saveCurrentPosition();
                }
            }
        });
    }

    private void setupSerialPlayback(PlayerLaunchData launchData) {
        List<MediaItem> mediaItems = createSerialMediaItems(launchData);

        if (mediaItems.isEmpty()) {
            showToast("Не удалось создать плейлист для сериала");
            return;
        }

        player.setMediaItems(mediaItems);
        int startEpisodeIndex = launchData.getSelectedIndexPath().get(INDEX_EPISODES);

        // Восстанавливаем позицию для стартового эпизода
        restorePositionForCurrentEpisode(startEpisodeIndex);

        setupSerialAnalyticsListener();

        updateFilmViewStatus(true);

        loadDirectVideoUrlForCurrentItem();

        player.prepare();
        player.play();
    }

    private void restorePositionForCurrentEpisode(int episodeIndex) {
        if (player == null || player.getMediaItemCount() <= episodeIndex) {
            return;
        }

        MediaItem mediaItem = player.getMediaItemAt(episodeIndex);
        if (mediaItem == null) {
            return;
        }

        String key = mediaItem.mediaId;
        Long savedPosition = savedPositionsMap.get(key);

        if (savedPosition != null && savedPosition > 0) {
            player.seekTo(episodeIndex, savedPosition);
        }
    }

    private List<MediaItem> createSerialMediaItems(PlayerLaunchData launchData) {
        List<MediaItem> mediaItems = new ArrayList<>();
        List<Integer> selectedIndexPath = launchData.getSelectedIndexPath();

        Folder selectedBalancer = launchData.getRootFolders().get(selectedIndexPath.get(INDEX_BALANCER));
        Item selectedSeason = selectedBalancer.children.get(selectedIndexPath.get(INDEX_SEASON));

        if (!(selectedSeason instanceof Folder)) {
            return mediaItems;
        }

        for (int episodeIndex = 0; episodeIndex < ((Folder) selectedSeason).children.size(); episodeIndex++) {
            Item episodeItem = ((Folder) selectedSeason).children.get(episodeIndex);
            if (!(episodeItem instanceof Folder)) continue;

            Folder episodeFolder = (Folder) episodeItem;
            Folder selectedVoice = (Folder) episodeFolder.children.get(selectedIndexPath.get(INDEX_VOICE));

            if (selectedVoice.children.size() > selectedIndexPath.get(INDEX_QUALITY)) {
                File selectedQuality = (File) selectedVoice.children.get(selectedIndexPath.get(INDEX_QUALITY));

                List<Integer> episodeIndexPath = new ArrayList<>(selectedIndexPath);
                episodeIndexPath.set(INDEX_EPISODES, episodeIndex);

                MediaItem mediaItem = createMediaItem(
                        PlayerLaunchData.getIndexPathKey(episodeIndexPath),
                        episodeIndexPath,
                        selectedQuality.videoUrl
                );
                mediaItems.add(mediaItem);
            }
        }

        return mediaItems;
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setupSerialAnalyticsListener() {
        analyticsListener = new AnalyticsListener() {
            @Override
            public void onMediaItemTransition(EventTime eventTime, @Nullable MediaItem mediaItem, int reason) {
                AnalyticsListener.super.onMediaItemTransition(eventTime, mediaItem, reason);

                if (!isDestroyed) {
                    // Сохраняем позицию для предыдущего эпизода
                    saveCurrentPosition();

                    // Восстанавливаем позицию для нового эпизода
                    int currentIndex = player.getCurrentMediaItemIndex();
                    restorePositionForCurrentEpisode(currentIndex);

                    loadDirectVideoUrlForCurrentItem();
                }
            }
        };
        player.addAnalyticsListener(analyticsListener);
    }

    private void loadDirectVideoUrlForCurrentItem() {
        if (isDestroyed) return;

        mainHandler.post(() -> {
            if (player == null || player.getCurrentMediaItem() == null) return;

            MediaItem currentItem = player.getCurrentMediaItem();
            if (currentItem.localConfiguration == null) return;

            String videoData = currentItem.localConfiguration.uri.toString();
            String mediaId = currentItem.mediaId;
            Object tag = currentItem.localConfiguration.tag;

            executorService.execute(() -> {
                if (isDestroyed) return;

                String urlVideo = HDVB.getFileSerial(videoData);

                if (isValidUrl(urlVideo)) {
                    MediaItem newMediaItem = createMediaItem(mediaId, tag, urlVideo);

                    mainHandler.post(() -> {
                        if (isDestroyed || player == null) return;

                        int currentIndex = player.getCurrentMediaItemIndex();
                        if (currentIndex >= 0) {
                            // Сохраняем текущую позицию перед заменой
                            long currentPosition = player.getCurrentPosition();

                            player.replaceMediaItem(currentIndex, newMediaItem);

                            // Восстанавливаем позицию после замены
                            player.seekTo(currentIndex, currentPosition);

                            if (!player.isPlaying() && !isDestroyed) {
                                player.prepare();
                                player.play();
                            }
                        }
                    });
                }
            });
        });
    }

    private void setupMoviePlayback(PlayerLaunchData launchData) {
        File startingFile = findFileByPath(launchData.getSelectedIndexPath());
        if (startingFile == null) {
            showToast("Не удалось найти файл для воспроизведения");
            return;
        }

        MediaItem mediaItem = createMediaItem(
                String.valueOf(kinopoiskId),
                startingFile,
                startingFile.videoUrl
        );

        player.setMediaItems(List.of(mediaItem));

        // Восстанавливаем позицию для фильма
        restorePositionForMovie();

        updateFilmViewStatus(true);

        player.prepare();
        player.play();
    }

    private void restorePositionForMovie() {
        if (player == null) return;

        String key = String.valueOf(kinopoiskId);
        Long savedPosition = savedPositionsMap.get(key);

        if (savedPosition != null && savedPosition > 0) {
            player.seekTo(savedPosition);
        }
    }

    private MediaItem createMediaItem(String mediaId, Object tag, String uri) {
        return new MediaItem.Builder()
                .setMediaId(mediaId)
                .setTag(tag)
                .setUri(uri)
                .build();
    }

    private boolean isValidUrl(String url) {
        return url != null && (url.startsWith("https://") || url.startsWith("http://"));
    }

    private File findFileByPath(List<Integer> path) {
        if (path == null || path.isEmpty() || launchData == null) {
            return null;
        }

        try {
            Item currentItem = launchData.getRootFolders().get(path.get(0));
            for (int i = 1; i < path.size(); i++) {
                if (!(currentItem instanceof Folder)) {
                    return null;
                }
                currentItem = ((Folder) currentItem).children.get(path.get(i));
            }
            return currentItem instanceof File ? (File) currentItem : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void showToast(String message) {
        if (isDestroyed) return;
        mainHandler.post(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
    }

    private void saveCurrentPosition() {
        if (isDestroyed || player == null || filmDetails == null || player.getCurrentMediaItem() == null) {
            return;
        }

        String key;
        long position = player.getCurrentPosition();

        if (isSerial) {
            MediaItem currentItem = player.getCurrentMediaItem();
            key = currentItem.mediaId;
        } else {
            key = String.valueOf(kinopoiskId);
        }

        // Обновляем локальную карту
        savedPositionsMap.put(key, position);

        // Сохраняем в базу в фоновом потоке
        executorService.execute(() -> {
            if (isDestroyed) return;

            FilmDetails currentDetails = filmDetailsDao.getById(kinopoiskId);
            if (currentDetails != null) {
                Map<String, Long> positionMap = currentDetails.getLastPositionPlayerView() != null
                        ? new HashMap<>(currentDetails.getLastPositionPlayerView())
                        : new HashMap<>();

                positionMap.put(key, position);
                filmDetailsDao.updatePositions(kinopoiskId, positionMap);

                // Обновляем локальный объект
                if (filmDetails != null) {
                    filmDetails.setLastPositionPlayerView(positionMap);
                }
            }
        });
    }

    private void resetCurrentPosition() {
        if (isDestroyed) return;

        mainHandler.post(() -> {
            if (player == null) return;

            String key;

            if (isSerial) {
                MediaItem currentItem = player.getCurrentMediaItem();
                if (currentItem == null) return;
                key = currentItem.mediaId;
            } else {
                key = String.valueOf(kinopoiskId);
            }

            // Удаляем из локальной карты
            savedPositionsMap.remove(key);

            // Удаляем из базы
            executorService.execute(() -> {
                if (isDestroyed) return;

                FilmDetails currentDetails = filmDetailsDao.getById(kinopoiskId);
                if (currentDetails != null) {
                    Map<String, Long> positionMap = currentDetails.getLastPositionPlayerView() != null
                            ? new HashMap<>(currentDetails.getLastPositionPlayerView())
                            : new HashMap<>();

                    positionMap.remove(key);
                    filmDetailsDao.updatePositions(kinopoiskId, positionMap);

                    if (filmDetails != null) {
                        filmDetails.setLastPositionPlayerView(positionMap);
                    }
                }
            });
        });
    }

    private void updateFilmViewStatus(boolean isStartView) {
        if (filmDetails == null || isDestroyed) {
            return;
        }

        executorService.execute(() -> {
            if (isDestroyed) return;

            FilmDetails currentDetails = filmDetailsDao.getById(kinopoiskId);
            if (currentDetails != null) {
                currentDetails.setIsView(true);
                currentDetails.setIsStartView(isStartView);
                currentDetails.setTimestampAddedHistory(System.currentTimeMillis());

                filmDetailsDao.insertAndPreservePositions(currentDetails);

                mainHandler.post(() -> {
                    if (!isDestroyed) {
                        filmDetails = currentDetails;
                    }
                });
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        saveCurrentPosition();
        savePositionHandler.removeCallbacks(savePositionRunnable);

        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (player != null && !isDestroyed) {
            player.play();
            savePositionHandler.post(savePositionRunnable);
        }

        // Восстанавливаем полноэкранный режим при возврате
        setFullscreen(true);
    }

    @Override
    public void onDestroyView() {
        isDestroyed = true;

        // Удаляем все обработчики
        savePositionHandler.removeCallbacks(savePositionRunnable);

        // Сохраняем позицию синхронно
        if (player != null && filmDetails != null && player.getCurrentMediaItem() != null) {
            String key;
            long position = player.getCurrentPosition();

            if (isSerial) {
                MediaItem currentItem = player.getCurrentMediaItem();
                if (currentItem != null) {
                    key = currentItem.mediaId;
                } else {
                    key = String.valueOf(kinopoiskId);
                }
            } else {
                key = String.valueOf(kinopoiskId);
            }

            final String finalKey = key;
            final long finalPosition = position;

            new Thread(() -> {
                FilmDetails currentDetails = filmDetailsDao.getById(kinopoiskId);
                Map<String, Long> positionMap = currentDetails != null &&
                        currentDetails.getLastPositionPlayerView() != null
                        ? new HashMap<>(currentDetails.getLastPositionPlayerView())
                        : new HashMap<>();

                positionMap.put(finalKey, finalPosition);
                filmDetailsDao.updatePositions(kinopoiskId, positionMap);
            }).start();
        }

        if (player != null) {
            if (analyticsListener != null) {
                player.removeAnalyticsListener(analyticsListener);
            }
            player.stop();
            player.release();
            player = null;
        }

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }

        // Возвращаем ориентацию активности к исходной, только если нужно
        if (shouldRestoreOrientation) {
            mainActivity.setRequestedOrientation(originalOrientation);
        }

        // Выходим из полноэкранного режима
        setFullscreen(false);

        // Восстанавливаем UI элементы MainActivity
        mainActivity.showBottomNavigationView();
        mainActivity.showToolbar();

        binding = null;
        super.onDestroyView();
    }
}