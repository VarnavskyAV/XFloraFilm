package com.alaka_ala.florafilm.ui.fragments.filmDetails;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alaka_ala.florafilm.databinding.FragmentFilmDetailsBinding;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.KinopoiskApiClient;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmDetails;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Locale;

public class FilmDetailsFragment extends BottomSheetDialogFragment {
    private FragmentFilmDetailsBinding binding;
    private int kinopoiskId;
    private KinopoiskApiClient kinopoiskApi;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFilmDetailsBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            kinopoiskId = getArguments().getInt("kinopoiskId", 0);
        }

        kinopoiskApi = KinopoiskApiClient.getInstance();

        if (kinopoiskId > 0) {
            fetchFilmDetails();
        }

        return binding.getRoot();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        if (dialog.getWindow() != null) {
            // This is the only line needed to enable edge-to-edge
            WindowCompat.setDecorFitsSystemWindows(dialog.getWindow(), false);
        }

        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            setupBottomSheet(bottomSheetDialog);
        });

        return dialog;
    }

    private void setupBottomSheet(BottomSheetDialog dialog) {
        FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet == null) return;

        // Let the behavior automatically handle the height
        BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        
        // Устанавливаем высоту bottomSheet на всю высоту экрана
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        bottomSheet.setLayoutParams(layoutParams);

        // Apply insets for status bar and navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(bottomSheet, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            // Apply bottom padding to the NestedScrollView to avoid content overlapping with the navigation bar
            binding.nestedScrollView.setPadding(0, 0, 0, systemBars.bottom);
            
            return insets;
        });
    }

    private void fetchFilmDetails() {
        kinopoiskApi.getFilmDetails(kinopoiskId, new KinopoiskApiClient.ApiCallback<>() {
            @Override
            public void onSuccess(FilmDetails result) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> bindFilmDetails(result));
                }
            }

            @Override
            public void onError(KinopoiskApiClient.ApiException error) {
                // TODO: Handle error
            }
        });
    }

    private void bindFilmDetails(@NonNull FilmDetails details) {
        Glide.with(this)
                .load(details.getBestPoster())
                .into(binding.filmPoster);

        binding.filmTitle.setText(details.getBestName());
        binding.filmOriginalTitle.setText(String.format(Locale.getDefault(), "%s (%s)", details.getNameOriginal(), details.getYear()));
        binding.filmSlogan.setText(details.getSlogan());
        binding.filmDescription.setText(details.getBestDescription());

        String kinopoiskRating = String.format(Locale.getDefault(), "⭐ %.1f (KP)", details.getRatingKinopoisk());
        String imdbRating = String.format(Locale.getDefault(), "⭐ %.1f (IMDb)", details.getRatingImdb());

        binding.filmRatingKinopoisk.setText(kinopoiskRating);
        binding.filmRatingImdb.setText(imdbRating);
    }
}