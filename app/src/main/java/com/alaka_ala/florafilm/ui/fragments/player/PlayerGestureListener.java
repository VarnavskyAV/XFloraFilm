package com.alaka_ala.florafilm.ui.fragments.player;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {

    public interface OnSingleTapListener {
        void onSingleTap();
    }

    private OnSingleTapListener onSingleTapListener;

    private final Activity activity;
    private ExoPlayer player;
    private final PlayerView playerView;
    private final AudioManager audioManager;

    // UI
    private final LinearLayout centerFeedbackLayout;
    private final ImageView centerFeedbackIcon;
    private final TextView centerFeedbackText;
    private final ProgressBar centerFeedbackProgress;
    private final TextView speed2xText;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Handler seekHandler = new Handler(Looper.getMainLooper());
    private final Handler scrollSeekHandler = new Handler(Looper.getMainLooper());

    private enum GestureAction { NONE, BRIGHTNESS, VOLUME, SEEK }
    private GestureAction currentAction = GestureAction.NONE;

    private float initialBrightness;
    private int initialVolume;

    private long accumulatedSeekTimeMs = 0;
    private long horizontalSeekTimeMs = 0;
    private boolean isForwardTap;

    private static final long SCROLL_SEEK_TIMEOUT = 800;

    public PlayerGestureListener(
            Activity activity,
            ExoPlayer player,
            PlayerView playerView,
            LinearLayout centerFeedbackLayout,
            ImageView centerFeedbackIcon,
            TextView centerFeedbackText,
            ProgressBar centerFeedbackProgress,
            TextView speed2xText
    ) {
        this.activity = activity;
        this.player = player;
        this.playerView = playerView;
        this.centerFeedbackLayout = centerFeedbackLayout;
        this.centerFeedbackIcon = centerFeedbackIcon;
        this.centerFeedbackText = centerFeedbackText;
        this.centerFeedbackProgress = centerFeedbackProgress;
        this.speed2xText = speed2xText;

        this.audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
    }

    public void setOnSingleTapListener(OnSingleTapListener listener) {
        this.onSingleTapListener = listener;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        currentAction = GestureAction.NONE;

        initialBrightness = activity.getWindow().getAttributes().screenBrightness;

        if (audioManager != null) {
            initialVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }

        scrollSeekHandler.removeCallbacks(applyScrollSeekRunnable);
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (currentAction != GestureAction.NONE) return false;

        if (onSingleTapListener != null) {
            onSingleTapListener.onSingleTap();
        }

        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (currentAction != GestureAction.NONE) return true;

        seekHandler.removeCallbacks(seekRunnable);

        boolean forward = e.getX() > playerView.getWidth() / 2;

        if (accumulatedSeekTimeMs == 0 || isForwardTap != forward) {
            accumulatedSeekTimeMs = 0;
            isForwardTap = forward;
        }

        accumulatedSeekTimeMs += forward ? 15000 : -15000;

        showText(""
                + (accumulatedSeekTimeMs > 0 ? "+" : "")
                + (accumulatedSeekTimeMs / 1000) + "s");

        seekHandler.postDelayed(seekRunnable, 800);

        return true; // 💥 съели double tap
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (currentAction != GestureAction.NONE) return;

        player.setPlaybackSpeed(2f);
        if (speed2xText != null) speed2xText.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        if (currentAction == GestureAction.NONE) {
            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                currentAction = GestureAction.SEEK;
                showText("0s");
            } else {
                if (e1.getX() < playerView.getWidth() / 2) {
                    currentAction = GestureAction.BRIGHTNESS;
                    showProgress();
                } else {
                    currentAction = GestureAction.VOLUME;
                    showProgress();
                }
            }
        }

        switch (currentAction) {
            case BRIGHTNESS:
                handleBrightness(e1, e2);
                break;
            case VOLUME:
                handleVolume(e1, e2);
                break;
            case SEEK:
                handleSeek(distanceX);
                break;
        }

        return true;
    }

    private final Runnable seekRunnable = () -> {
        if (player != null) {
            player.seekTo(player.getCurrentPosition() + accumulatedSeekTimeMs);
        }
        accumulatedSeekTimeMs = 0;
        hideFeedback();
    };

    private final Runnable applyScrollSeekRunnable = () -> {
        if (player != null) {
            long pos = player.getCurrentPosition() + horizontalSeekTimeMs;
            if (pos < 0) pos = 0;
            if (pos > player.getDuration()) pos = player.getDuration();
            player.seekTo(pos);
        }
        horizontalSeekTimeMs = 0;
        hideFeedback();
    };

    private void handleSeek(float distanceX) {
        scrollSeekHandler.removeCallbacks(applyScrollSeekRunnable);

        horizontalSeekTimeMs += (long) (-distanceX * 100);

        showText((horizontalSeekTimeMs >= 0 ? "+" : "") + (horizontalSeekTimeMs / 1000) + "s");

        scrollSeekHandler.postDelayed(applyScrollSeekRunnable, SCROLL_SEEK_TIMEOUT);
    }

    private void handleBrightness(MotionEvent e1, MotionEvent e2) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();

        float delta = (e1.getY() - e2.getY()) / playerView.getHeight();
        float newVal = initialBrightness + delta;

        newVal = Math.max(0f, Math.min(1f, newVal));

        lp.screenBrightness = newVal;
        window.setAttributes(lp);

        updateProgress((int) (newVal * 100));
    }

    private void handleVolume(MotionEvent e1, MotionEvent e2) {
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        float delta = (e1.getY() - e2.getY()) / playerView.getHeight();
        int newVol = (int) (initialVolume + delta * max);

        newVol = Math.max(0, Math.min(max, newVol));

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0);

        updateProgress((int) ((newVol / (float) max) * 100));
    }

    public void onUp(MotionEvent e) {
        if (player != null && player.getPlaybackParameters().speed > 1f) {
            player.setPlaybackSpeed(1f);
            if (speed2xText != null) speed2xText.setVisibility(View.GONE);
        }

        if (currentAction != GestureAction.SEEK) {
            hideFeedback();
        }
    }

    private void showText(String text) {
        if (centerFeedbackLayout == null) return;

        centerFeedbackLayout.setVisibility(View.VISIBLE);
        centerFeedbackText.setVisibility(View.VISIBLE);
        centerFeedbackProgress.setVisibility(View.GONE);
        centerFeedbackIcon.setVisibility(View.GONE);

        centerFeedbackText.setText(text);
    }

    private void showProgress() {
        if (centerFeedbackLayout == null) return;

        centerFeedbackLayout.setVisibility(View.VISIBLE);
        centerFeedbackText.setVisibility(View.GONE);
        centerFeedbackProgress.setVisibility(View.VISIBLE);
        centerFeedbackIcon.setVisibility(View.VISIBLE);
    }

    private void updateProgress(int value) {
        if (centerFeedbackProgress != null) {
            centerFeedbackProgress.setProgress(value);
        }
    }

    private void hideFeedback() {
        handler.postDelayed(() -> {
            if (centerFeedbackLayout != null) {
                centerFeedbackLayout.setVisibility(View.GONE);
            }
        }, 500);
    }

    public void setPlayer(ExoPlayer player) {
        this.player = player;
    }
}