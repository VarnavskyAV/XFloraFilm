package com.alaka_ala.florafilm.ui.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.data.database.entities.FilmCollectionEntity;
import com.alaka_ala.florafilm.databinding.FragmentHomeBinding;
import com.alaka_ala.florafilm.ui.activities.MainActivity;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmItem;
import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements FilmCollectionAdapter.OnCollectionClickListener {
    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private FilmCollectionAdapter adapter;
    private AppBarLayout appBarLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new FilmCollectionAdapter(new ArrayList<>());
        adapter.setOnCollectionClickListener(this);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();

        homeViewModel.getFilmCollections().observe(getViewLifecycleOwner(), collections -> {
            if (collections != null) {
                adapter.setCollections(collections);
            }
        });

        if (getActivity() instanceof MainActivity) {
            appBarLayout = getActivity().findViewById(R.id.app_bar_layout);
            if (appBarLayout != null) {
                appBarLayout.setLiftOnScrollTargetViewId(binding.collectionsRecyclerview.getId());
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.collectionsRecyclerview.setAdapter(null);

        if (appBarLayout != null) {
            appBarLayout.setLiftOnScrollTargetViewId(View.NO_ID);
        }
        binding = null;
    }

    private void setupRecyclerView() {
        binding.collectionsRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.collectionsRecyclerview.setAdapter(adapter);
    }

    @Override
    public void onFilmClick(FilmItem film) {
        Toast.makeText(getContext(), "Clicked on film: " + film.getBestName(), Toast.LENGTH_SHORT).show();
        Bundle bundle = new Bundle();
        bundle.putInt("kinopoiskId", film.getKinopoiskId());
        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navigation_home_to_filmDetailsFragment, bundle);
    }

    @Override
    public void onCollectionTitleClick(FilmCollectionEntity collection) {
        Toast.makeText(getContext(), "Clicked on collection: " + collection.getTitle(), Toast.LENGTH_SHORT).show();
    }
}
