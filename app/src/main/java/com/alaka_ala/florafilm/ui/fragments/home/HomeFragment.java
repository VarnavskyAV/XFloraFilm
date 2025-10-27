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
import com.alaka_ala.florafilm.databinding.FragmentHomeBinding;
import com.alaka_ala.florafilm.ui.activities.MainActivity;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.KinopoiskApiClient;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.constants.Constants;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmCollection;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmCountryOrGenresResponse;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmDetails;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmImagesResponse;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmItem;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmSearchResponse;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmSequelsAndPrequelsResponse;
import com.alaka_ala.florafilm.ui.utils.kinopoisk.models.FilmSimilarResponse;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.api.KinopoiskApiClientV2;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models.Staff;
import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment{
    private FragmentHomeBinding binding;
    private AppBarLayout appBarLayout;
    private KinopoiskApiClient kinopoiskApiClient;
    private KinopoiskApiClientV2 kinopoiskApiClientV2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        kinopoiskApiClient = KinopoiskApiClient.getInstance();
        if (getActivity() instanceof MainActivity) {
            appBarLayout = getActivity().findViewById(R.id.app_bar_layout);
            if (appBarLayout != null) {
                appBarLayout.setLiftOnScrollTargetViewId(binding.collectionsRecyclerview.getId());
            }
        }


        kinopoiskApiClientV2 = KinopoiskApiClientV2.getInstance();
        kinopoiskApiClientV2.getStaff(666, false, new KinopoiskApiClientV2.ApiCallback<List<Staff>>() {
            @Override
            public void onSuccess(List<Staff> result) {

            }

            @Override
            public void onError(KinopoiskApiClientV2.ApiException error) {

            }
        });










        return binding.getRoot();
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


}
