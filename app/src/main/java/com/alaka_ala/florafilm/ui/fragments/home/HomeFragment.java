package com.alaka_ala.florafilm.ui.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentHomeBinding;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.KinopoiskApiClient;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmDetails;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private KinopoiskApiClient kinopoiskApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        kinopoiskApi = KinopoiskApiClient.getInstance();







        return binding.getRoot();
    }
}