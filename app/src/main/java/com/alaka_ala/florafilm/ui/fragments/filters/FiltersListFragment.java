package com.alaka_ala.florafilm.ui.fragments.filters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alaka_ala.florafilm.databinding.FragmentFiltersListBinding;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.KinopoiskApiClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FiltersListFragment extends Fragment {
    private FragmentFiltersListBinding binding;
    private KinopoiskApiClient kinopoiskApiClient;
    private RecyclerView rvFilterList;
    private ExecutorService executor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFiltersListBinding.inflate(inflater, container, false);
        kinopoiskApiClient = KinopoiskApiClient.getInstance();
        rvFilterList = binding.rvFilterList;
        rvFilterList.setLayoutManager(new LinearLayoutManager(getContext()));
        executor = Executors.newSingleThreadExecutor();
        String type = getArguments().getString("type", "");






        return binding.getRoot();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    private boolean isTypeValid(String type) {
        String[] types = new String[]{"ratingKinopoisk", "ratingImdb", "year", "genre", "country"};
        for (String t : types) {
            if (t.equals(type)) {
                return true;
            }
        }
        return false;
    }
}
