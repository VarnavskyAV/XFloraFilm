package com.alaka_ala.florafilm.ui.fragments.filmDetails;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alaka_ala.florafilm.databinding.FragmentFilmDetailsBinding;
import com.alaka_ala.florafilm.ui.activities.MainActivity;

public class FilmDetailsFragment extends Fragment {
    private FragmentFilmDetailsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFilmDetailsBinding.inflate(inflater, container, false);

        // Скрываем нижнюю навигацию при переходе на этот фрагмент.
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideBottomNavigationView();
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Получаем ID фильма из аргументов.
        assert getArguments() != null;
        int kinopoiskId = getArguments().getInt("kinopoiskId");




    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Показываем нижнюю навигацию при выходе из этого фрагмента.
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showBottomNavigationView();
        }
        // Очищаем ссылку на binding для предотвращения утечек памяти.
        binding = null;
    }
}