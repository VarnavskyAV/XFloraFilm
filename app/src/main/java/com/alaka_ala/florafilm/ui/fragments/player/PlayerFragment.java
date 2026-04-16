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

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentPlayerBinding;
import com.alaka_ala.florafilm.ui.activities.MainActivity;
import com.alaka_ala.florafilm.data.media.PlayerLaunchData;
import com.alaka_ala.florafilm.ui.fragments.filmDetails.SelectorVoiceAdapter;
import com.alaka_ala.florafilm.ui.fragments.player.strategy.AllohaStrategy;
import com.alaka_ala.florafilm.ui.fragments.player.strategy.HDVBStrategy;
import com.alaka_ala.florafilm.ui.fragments.player.strategy.PlayerSourceStrategy;
import com.alaka_ala.unofficial_kinopoisk_api.db.FilmDetailsDao;
import com.alaka_ala.unofficial_kinopoisk_api.db.KinopoiskDatabaseV2;
import com.alaka_ala.unofficial_kinopoisk_api.models.FilmDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@UnstableApi
public class PlayerFragment extends Fragment {

    // Константы
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
    private FilmDetailsDao filmDetailsDao;
    private PlayerSourceStrategy sourceStrategy; // Стратегия для текущего источника

    // Состояние
    private boolean isDestroyed = false;
    private Map<String, Long> savedPositionsMap = new HashMap<>();
    private MainActivity mainActivity;
    private int originalOrientation;
    private boolean isScreenRotated = false;
    private boolean shouldRestoreOrientation = true;
    private boolean isControllerVisible = true;

    // Режимы соотношения сторон
    private int currentAspectRatio = AspectRatioFrameLayout.RESIZE_MODE_FIT;
    private final int[] aspectRatioModes = {
            AspectRatioFrameLayout.RESIZE_MODE_FIT,
            AspectRatioFrameLayout.RESIZE_MODE_FILL,
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM
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
        btnScreenRotate = binding.getRoot().findViewById(R.id.btn_screen_rotate);
        btnAspectRatio = binding.getRoot().findViewById(R.id.btn_aspect_ratio);
        customControlsLayout = binding.getRoot().findViewById(R.id.custom_controls_layout);

        mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        isScreenRotated = true;
        if (btnScreenRotate != null) {
            btnScreenRotate.setImageResource(R.drawable.ic_rotate);
            btnScreenRotate.setOnClickListener(v -> toggleScreenOrientation());
        }

        if (btnAspectRatio != null) {
            btnAspectRatio.setOnClickListener(v -> toggleAspectRatio());
        }

        if (customControlsLayout != null) {
            customControlsLayout.setVisibility(View.VISIBLE);
        }

        setFullscreen(true);
        binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        binding.playerView.setUseController(false);
    }

    private void initializeGestureListener() {
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

        gestureListener.setOnSingleTapListener(() -> toggleControllerVisibility());
        gestureDetector = new GestureDetector(requireContext(), gestureListener);

        binding.playerView.setOnTouchListener((v, event) -> {
            boolean handled = gestureDetector.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_UP) {
                gestureListener.onUp(event);
            }
            return handled;
        });

        binding.playerView.setLongClickable(true);
    }

    private void toggleControllerVisibility() {
        if (binding.playerView == null) return;

        if (isControllerVisible) {
            binding.playerView.hideController();
            if (customControlsLayout != null) {
                customControlsLayout.setVisibility(View.GONE);
            }
            isControllerVisible = false;
        } else {
            binding.playerView.showController();
            if (customControlsLayout != null) {
                customControlsLayout.setVisibility(View.VISIBLE);
            }
            isControllerVisible = true;

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
            Window window = requireActivity().getWindow();
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );

            View decorView = window.getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        } else {
            Window window = requireActivity().getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            View decorView = window.getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    private void toggleScreenOrientation() {
        shouldRestoreOrientation = false;
        int currentOrientation = mainActivity.getRequestedOrientation();

        if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||
                currentOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            isScreenRotated = false;
            if (btnScreenRotate != null) {
                btnScreenRotate.setImageResource(R.drawable.ic_rotate);
            }
            Toast.makeText(requireContext(), "Портретный режим", Toast.LENGTH_SHORT).show();
        } else {
            mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            isScreenRotated = true;
            if (btnScreenRotate != null) {
                btnScreenRotate.setImageResource(R.drawable.ic_rotate);
            }
            Toast.makeText(requireContext(), "Ландшафтный режим", Toast.LENGTH_SHORT).show();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            shouldRestoreOrientation = true;
        }, 1000);
    }

    private void toggleAspectRatio() {
        currentAspectRatio = (currentAspectRatio + 1) % aspectRatioModes.length;
        binding.playerView.setResizeMode(aspectRatioModes[currentAspectRatio]);

        String message = "Режим: " + getAspectRatioName(aspectRatioModes[currentAspectRatio]);
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private String getAspectRatioName(int mode) {
        switch (mode) {
            case AspectRatioFrameLayout.RESIZE_MODE_FIT:
                return "FIT - видео помещается в экран";
            case AspectRatioFrameLayout.RESIZE_MODE_FILL:
                return "FILL - видео заполняет экран";
            case AspectRatioFrameLayout.RESIZE_MODE_ZOOM:
                return "ZOOM - видео увеличивается";
            default:
                return "UNKNOWN";
        }
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

        Map<String, Long> lastPositionPlayerView = filmDetails.getLastPositionPlayerView();
        if (lastPositionPlayerView != null) {
            savedPositionsMap.putAll(lastPositionPlayerView);
        }

        // ВАЖНО: Здесь создаем нужную стратегию в зависимости от источника
        sourceStrategy = createSourceStrategy();

        mainHandler.post(() -> initializePlayer());
    }

    /**
     * Фабричный метод для создания стратегии в зависимости от источника
     * Здесь определяется, какой источник используется
     */
    private PlayerSourceStrategy createSourceStrategy() {
        // Определяем тип источника из launchData
        int sourceType = detectSourceType();
        switch (sourceType) {
            case 0: // HDVB
                return new HDVBStrategy();
            case 1: // ALLOHA
            default:
                return new AllohaStrategy();
        }
    }

    /**
     * Определяет тип источника по данным launchData
     */
    private int detectSourceType() {
        // Вариант 1: Если в launchData есть поле sourceType
        if (launchData.getSourceType() != -1) {
            return launchData.getSourceType();
        }

        // TODO: Добавить Вариант 2: Если в launchData нет поля sourceType, но есть rootFolders
        return 0; // По умолчанию HDVB
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
    private void initializePlayer() {
        player = new ExoPlayer.Builder(requireContext()).build();
        binding.playerView.setPlayer(player);

        if (gestureListener != null) {
            gestureListener.setPlayer(player);
        }

        binding.playerView.setUseController(true);
        binding.playerView.setShowNextButton(true);
        binding.playerView.setShowPreviousButton(true);
        binding.playerView.setShowFastForwardButton(false);
        binding.playerView.setShowRewindButton(false);
        binding.playerView.setShowShuffleButton(false);
        binding.playerView.setShowSubtitleButton(true);
        binding.playerView.setShowVrButton(false);

        setupPlayerListeners();

        // Делегируем настройку воспроизведения стратегии
        sourceStrategy.setupPlayback(
                requireContext(),
                player,
                launchData,
                filmDetails,
                savedPositionsMap,
                executorService,
                mainHandler
        );

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
                        if (!isDestroyed) {
                            savePositionHandler.post(savePositionRunnable);
                        }
                        break;
                    case Player.STATE_ENDED:
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
                    saveCurrentPosition();
                }
            }
        });
    }

    private void saveCurrentPosition() {
        if (isDestroyed || player == null || filmDetails == null || player.getCurrentMediaItem() == null) {
            return;
        }

        String key = sourceStrategy.getPositionKey(player, kinopoiskId);
        long position = player.getCurrentPosition();

        savedPositionsMap.put(key, position);

        executorService.execute(() -> {
            if (isDestroyed) return;

            FilmDetails currentDetails = filmDetailsDao.getById(kinopoiskId);
            if (currentDetails != null) {
                Map<String, Long> positionMap = currentDetails.getLastPositionPlayerView() != null
                        ? new HashMap<>(currentDetails.getLastPositionPlayerView())
                        : new HashMap<>();

                positionMap.put(key, position);
                filmDetailsDao.updatePositions(kinopoiskId, positionMap);

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

            String key = sourceStrategy.getPositionKey(player, kinopoiskId);
            savedPositionsMap.remove(key);

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

        setFullscreen(true);
    }

    @Override
    public void onDestroyView() {
        isDestroyed = true;
        savePositionHandler.removeCallbacks(savePositionRunnable);

        if (player != null && filmDetails != null && player.getCurrentMediaItem() != null) {
            String key = sourceStrategy.getPositionKey(player, kinopoiskId);
            long position = player.getCurrentPosition();

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
            sourceStrategy.cleanup(player);
            player.stop();
            player.release();
            player = null;
        }

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }

        if (shouldRestoreOrientation) {
            mainActivity.setRequestedOrientation(originalOrientation);
        }

        setFullscreen(false);
        mainActivity.showBottomNavigationView();
        mainActivity.showToolbar();

        binding = null;
        super.onDestroyView();
    }
}