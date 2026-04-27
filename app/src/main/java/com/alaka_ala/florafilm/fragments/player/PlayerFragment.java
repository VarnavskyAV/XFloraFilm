package com.alaka_ala.florafilm.fragments.player;

import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Rational;
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
import androidx.annotation.RequiresApi;
import androidx.core.app.PictureInPictureModeChangedInfo;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentPlayerBinding;
import com.alaka_ala.florafilm.activities.MainActivity;
import com.alaka_ala.florafilm.data.media.PlayerLaunchData;
import com.alaka_ala.florafilm.fragments.player.strategy.AllohaStrategy;
import com.alaka_ala.florafilm.fragments.player.strategy.HDVBStrategy;
import com.alaka_ala.florafilm.fragments.player.strategy.PlayerSourceStrategy;
import com.alaka_ala.florafilm.utils.settings.AppPreferences;
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
    private static final long SAVE_POSITION_INTERVAL_MS = 5000;

    // UI элементы
    private FragmentPlayerBinding binding;
    private ImageButton btnScreenRotate;
    private ImageButton btnAspectRatio;
    private ImageButton btnPip;
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
    private PlayerSourceStrategy sourceStrategy;

    // Состояние
    private boolean isDestroyed = false;
    private Map<String, Long> savedPositionsMap = new HashMap<>();
    private MainActivity mainActivity;
    private int originalOrientation;
    private boolean isScreenRotated = false;
    private boolean shouldRestoreOrientation = true;
    private boolean isControllerVisible = false;

    // PiP
    private boolean isInPipMode = false;

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
        setupPictureInPicture();
    }

    @SuppressLint("SetTextI18n")
    private void initializeUI() {
        btnScreenRotate = binding.getRoot().findViewById(R.id.btn_screen_rotate);
        btnAspectRatio = binding.getRoot().findViewById(R.id.btn_aspect_ratio);
        btnPip = binding.getRoot().findViewById(R.id.btn_pip);
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

        if (btnPip != null) {
            btnPip.setOnClickListener(v -> enterPictureInPictureMode());
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                btnPip.setVisibility(View.GONE);
            }
        }

        if (customControlsLayout != null) {
            customControlsLayout.setVisibility(View.VISIBLE);
        }

        setFullscreen(true);
        binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        binding.playerView.setUseController(false);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeGestureListener() {
        if (getContext() == null) return;
        if (!AppPreferences.PlayerSettings.GestureListenerSettings.onIsGestureListener(getContext()))
            return;

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
        if (isInPipMode) return;
        if (!isAdded() || getActivity() == null) return;

        if (isControllerVisible) {
            if (binding != null && binding.playerView != null) {
                binding.playerView.hideController();
            }
        } else {
            if (binding != null && binding.playerView != null) {
                binding.playerView.showController();
            }
        }
    }

    private void setFullscreen(boolean fullscreen) {
        if (!isAdded() || getActivity() == null) {
            return;
        }

        Window window = requireActivity().getWindow();
        if (window == null) return;

        if (fullscreen) {
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );

            View decorView = window.getDecorView();
            if (decorView != null) {
                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                decorView.setSystemUiVisibility(uiOptions);
            }
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            View decorView = window.getDecorView();
            if (decorView != null) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
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

        sourceStrategy = createSourceStrategy();

        mainHandler.post(() -> initializePlayer());
    }

    private PlayerSourceStrategy createSourceStrategy() {
        int sourceType = detectSourceType();
        switch (sourceType) {
            case 0:
                return new HDVBStrategy();
            case 1:
            default:
                return new AllohaStrategy();
        }
    }

    private int detectSourceType() {
        if (launchData.getSourceType() != -1) {
            return launchData.getSourceType();
        }
        return 0;
    }

    private void showErrorAndReturn(String message) {
        if (!isAdded() || getActivity() == null) return;

        mainHandler.post(() -> {
            if (!isAdded() || getContext() == null) return;
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
        binding.playerView.setShowFastForwardButton(AppPreferences.PlayerSettings.PlayerButtonsControlSettings.isOnActiveButtonFastForward(getContext()));
        binding.playerView.setShowRewindButton(AppPreferences.PlayerSettings.PlayerButtonsControlSettings.isOnActiveButtonFastRewind(getContext()));
        binding.playerView.setShowShuffleButton(false);
        binding.playerView.setShowSubtitleButton(true);
        binding.playerView.setShowVrButton(false);

        setupPlayerListeners();

        sourceStrategy.setupPlayback(
                requireContext(),
                player,
                launchData,
                filmDetails,
                savedPositionsMap,
                executorService,
                mainHandler
        );
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

        binding.playerView.setControllerVisibilityListener(new PlayerView.ControllerVisibilityListener() {
            @Override
            public void onVisibilityChanged(int visibility) {
                if (customControlsLayout == null) return;

                if (visibility == View.VISIBLE) {
                    customControlsLayout.setVisibility(View.VISIBLE);
                    isControllerVisible = true;
                } else {
                    customControlsLayout.setVisibility(View.GONE);
                    isControllerVisible = false;
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void saveCurrentPosition() {
        if (isDestroyed || player == null || filmDetails == null || player.getCurrentMediaItem() == null) {
            return;
        }

        String key = sourceStrategy.getPositionKey(player, kinopoiskId);
        long position = player.getCurrentPosition();
        int currentMediaItemIndex = player.getCurrentMediaItemIndex();
        savedPositionsMap.put(key, position);

        executorService.execute(() -> {
            if (isDestroyed) return;

            FilmDetails currentDetails = filmDetailsDao.getById(kinopoiskId);
            if (currentDetails != null) {
                Map<String, Long> positionMap = currentDetails.getLastPositionPlayerView() != null
                        ? new HashMap<>(currentDetails.getLastPositionPlayerView())
                        : new HashMap<>();

                positionMap.put(key, position);
                List<Integer> selectedIndex = new ArrayList<>(launchData.getSelectedIndexPath());
                if (filmDetails.isSerial()) {
                    if (selectedIndex.size() > 2) {
                        selectedIndex.set(2, currentMediaItemIndex);
                    } else {
                        selectedIndex.add(2, currentMediaItemIndex);
                    }
                    filmDetails.setSelectedIndexPath(selectedIndex);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        // Balancer > Seasons > Episode > Translation > quality
                        if (getContext() == null) return;
                        binding.textViewTitleMovie.setText(currentDetails.getBestName() + "(" + currentDetails.getYear() + ") | Сезон: " + (selectedIndex.get(1) + 1) + " | Эпизод: " + (selectedIndex.get(2) + 1));
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        // Balancer > Translation > quality
                        if (getContext() == null) return;
                        binding.textViewTitleMovie.setText(currentDetails.getBestName() + "(" + currentDetails.getYear() + ")");
                    });
                }
                filmDetailsDao.insertAndPreservePositions(filmDetails);
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
                    filmDetails.setLastPositionPlayerView(positionMap);
                }
            });
        });
    }

    // ==================== PiP Methods ====================

    private void setupPictureInPicture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().addOnPictureInPictureModeChangedListener(new Consumer<PictureInPictureModeChangedInfo>() {
                @Override
                public void accept(PictureInPictureModeChangedInfo pictureInPictureModeChangedInfo) {
                    boolean wasInPipMode = isInPipMode;
                    isInPipMode = pictureInPictureModeChangedInfo.isInPictureInPictureMode();
                    
                    if (pictureInPictureModeChangedInfo.isInPictureInPictureMode()) {
                        onEnterPictureInPicture();
                    } else if (wasInPipMode) {
                        // Пользователь вышел из PIP - сбрасываем флаг, чтобы при уходе из плеера
                        // восстановился statusBar и другой UI
                        isInPipMode = false;
                        
                        // Восстанавливаем видимость контроллера если он был виден до входа в PIP
                        if (customControlsLayout != null && isControllerVisible) {
                            customControlsLayout.setVisibility(View.VISIBLE);
                        }
                        
                        // Восстанавливаем ориентацию ландшафта после выхода из PIP
                        if (mainActivity != null) {
                            mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        }
                    }
                }
            });
        }
    }

    private void enterPictureInPictureMode() {
        if (!isAdded() || getActivity() == null) {
            return;
        }

        if (!isPictureInPictureSupported()) {
            if (isAdded() && getContext() != null) {
                Toast.makeText(requireContext(), "PiP режим не поддерживается", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (player == null) return;

        saveCurrentPosition();

        float videoAspectRatio = getVideoAspectRatio();
        Rational aspectRatio = new Rational(
                (int) (videoAspectRatio * 1000),
                1000
        );

        PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setAutoEnterEnabled(true);
            builder.setSeamlessResizeEnabled(true);
        }

        try {
            requireActivity().enterPictureInPictureMode(builder.build());
        } catch (IllegalStateException | IllegalArgumentException e) {
            e.printStackTrace();
            if (isAdded() && getContext() != null) {
                Toast.makeText(requireContext(), "Не удалось войти в PiP режим", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private float getVideoAspectRatio() {
        if (player == null || player.getVideoSize() == null) {
            return 16f / 9f;
        }

        int videoWidth = player.getVideoSize().width;
        int videoHeight = player.getVideoSize().height;

        if (videoWidth == 0 || videoHeight == 0) {
            return 16f / 9f;
        }

        return (float) videoWidth / videoHeight;
    }

    private void onEnterPictureInPicture() {
        if (!isAdded() || getActivity() == null) {
            return;
        }

        if (customControlsLayout != null) {
            customControlsLayout.setVisibility(View.GONE);
        }

        if (player != null && !player.isPlaying()) {
            player.play();
        }

        setFullscreen(true);

        if (mainActivity != null) {
            mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        if (!isAdded() || getActivity() == null) {
            return;
        }

        if (isInPictureInPictureMode) {
            if (binding != null && binding.playerView != null) {
                binding.playerView.hideController();
            }
            if (gestureDetector != null && binding != null && binding.playerView != null) {
                binding.playerView.setOnTouchListener(null);
            }
        } else {
            if (gestureDetector != null && gestureListener != null && binding != null && binding.playerView != null) {
                binding.playerView.setOnTouchListener((v, event) -> {
                    boolean handled = gestureDetector.onTouchEvent(event);
                    if (event.getAction() == MotionEvent.ACTION_UP && gestureListener != null) {
                        gestureListener.onUp(event);
                    }
                    return handled;
                });
            }

            if (isControllerVisible && binding != null && binding.playerView != null) {
                binding.playerView.showController();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                !isInPipMode &&
                player != null &&
                player.isPlaying() &&
                isAdded() &&
                getActivity() != null &&
                isPictureInPictureSupported()) {

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!isDestroyed && !isInPipMode && player != null && player.isPlaying() && isAdded()) {
                    enterPictureInPictureMode();
                }
            }, 100);
        }
    }

    /**
     * Проверяет, поддерживается ли режим "Картинка в картинке" на устройстве
     * @return true если PiP поддерживается, false в противном случае
     */
    private boolean isPictureInPictureSupported() {
        // PiP доступен только на Android 8.0 (API 26) и выше
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return false;
        }

        // Пытаемся войти в PiP режим с пустыми параметрами
        // Если выбросит исключение - значит не поддерживается
        try {
            PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder();
            // Не вызываем enterPictureInPictureMode, просто проверяем через рефлексию
            // или полагаемся на версию Android
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Lifecycle Methods ====================

    @Override
    public void onPause() {
        super.onPause();
        saveCurrentPosition();
        savePositionHandler.removeCallbacks(savePositionRunnable);

        if (player != null && player.isPlaying() && !isInPipMode) {
            player.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (player != null && !isDestroyed && !isInPipMode) {
            player.play();
            savePositionHandler.post(savePositionRunnable);
        }

        // Оставляем fullscreen только если мы всё ещё в плеере (не вышли из него)
        // Проверка: если binding существует и мы видим этот фрагмент, значит мы ещё в плеере
        if (binding != null && isAdded()) {
            setFullscreen(true);
        }
    }

    @Override
    public void onDestroyView() {
        isDestroyed = true;
        savePositionHandler.removeCallbacks(savePositionRunnable);

        // Сразу восстанавливаем системный UI при выходе из фрагмента (не в PIP)
        if (!isInPipMode && mainActivity != null) {
            mainActivity.restoreSystemUI();
        }

        if (!isInPipMode && player != null && filmDetails != null && player.getCurrentMediaItem() != null) {
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

        if (player != null && !isInPipMode) {
            sourceStrategy.cleanup(player);
            player.stop();
            player.release();
            player = null;
        }

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }

        if (shouldRestoreOrientation && !isInPipMode && mainActivity != null) {
            mainActivity.setRequestedOrientation(originalOrientation);
        }

        if (!isInPipMode && mainActivity != null) {
            // Восстанавливаем UI только если мы не в PIP режиме и пользователь вышел из плеера
            setFullscreen(false);
            mainActivity.showBottomNavigationView();
            mainActivity.showToolbar();
            // Дополнительно вызываем восстановление системного UI на уровне Activity
            mainActivity.restoreSystemUI();
        } else if (isInPipMode) {
            // Если мы всё ещё в PIP режиме, восстанавливаем fullscreen для корректной работы
            setFullscreen(true);
        }

        binding = null;
        super.onDestroyView();
    }
}