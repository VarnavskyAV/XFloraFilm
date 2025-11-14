package com.alaka_ala.florafilm.ui.fragments.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentHomeBinding;
import com.alaka_ala.florafilm.ui.activities.MainActivity;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.api.KinopoiskApiClientV2;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.constants.FilmCollectionType;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models.FilmCollection;
import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

public class HomeFragment extends Fragment implements FilmAdapter.OnFilmClickListener, FilmCollectionAdapter.OnCollectionClickListener {
    private FragmentHomeBinding binding;
    private AppBarLayout appBarLayout;
    private KinopoiskApiClientV2 kinopoiskApiClientV2;
    private FilmCollectionAdapter filmCollectionAdapter;
    private final List<FilmCollectionAdapter.FilmCollectionItem> filmCollectionItems = new ArrayList<>();
    private final Queue<FilmCollectionType> filmCollectionTypeQueue = new LinkedList<>();

    private Parcelable mainRecyclerViewState;
    private final HashMap<String, Parcelable> nestedRecyclerViewStates = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        // Показываем нижнюю навигацию при выходе из этого фрагмента.
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showBottomNavigationView();
        }
        kinopoiskApiClientV2 = KinopoiskApiClientV2.getInstance();

        if (getActivity() instanceof MainActivity) {
            appBarLayout = getActivity().findViewById(R.id.app_bar_layout);
            if (appBarLayout != null) {
                appBarLayout.setLiftOnScrollTargetViewId(binding.collectionsRecyclerview.getId());
            }
        }

        setupRecyclerView();

        return binding.getRoot();
    }

    /**
     * Вызывается после создания представления фрагмента.
     * Здесь мы проверяем, нужно ли загружать коллекции фильмов.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (filmCollectionItems.isEmpty()) {
            loadFilmCollections();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null) {
            mainRecyclerViewState = binding.collectionsRecyclerview.getLayoutManager().onSaveInstanceState();
            outState.putParcelable("mainRecyclerViewState", mainRecyclerViewState);
            outState.putSerializable("nestedRecyclerViewStates", nestedRecyclerViewStates);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mainRecyclerViewState = savedInstanceState.getParcelable("mainRecyclerViewState");
            HashMap<String, Parcelable> savedNestedStates = (HashMap<String, Parcelable>) savedInstanceState.getSerializable("nestedRecyclerViewStates");
            if (savedNestedStates != null) {
                nestedRecyclerViewStates.putAll(savedNestedStates);
            }
        }
    }

    private void setupRecyclerView() {
        filmCollectionAdapter = new FilmCollectionAdapter(filmCollectionItems, nestedRecyclerViewStates);
        filmCollectionAdapter.setOnFilmClickListener(this);
        filmCollectionAdapter.setOnCollectionClickListener(this);
        binding.collectionsRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.collectionsRecyclerview.setAdapter(filmCollectionAdapter);
    }

    private void loadFilmCollections() {
        filmCollectionTypeQueue.add(FilmCollectionType.TOP_POPULAR_ALL);
        filmCollectionTypeQueue.add(FilmCollectionType.POPULAR_SERIES);
        filmCollectionTypeQueue.add(FilmCollectionType.TOP_POPULAR_MOVIES);

        // Add other collections to the queue
        for (FilmCollectionType type : FilmCollectionType.values()) {
            if (type != FilmCollectionType.TOP_POPULAR_ALL &&
                    type != FilmCollectionType.TOP_POPULAR_MOVIES &&
                    type != FilmCollectionType.POPULAR_SERIES) {
                filmCollectionTypeQueue.add(type);
            }
        }

        loadNextCollection();
    }

    private void loadNextCollection() {
        if (!filmCollectionTypeQueue.isEmpty()) {
            FilmCollectionType type = filmCollectionTypeQueue.poll();
            if (type != null) {
                getFilmCollection(type);
            }
        }
    }

    private void getFilmCollection(FilmCollectionType type) {
        kinopoiskApiClientV2.getFilmCollection(type, 1, false, new KinopoiskApiClientV2.ApiCallback<FilmCollection>() {
            @Override
            public void onSuccess(FilmCollection result) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    filmCollectionItems.add(new FilmCollectionAdapter.FilmCollectionItem(type.name(), result));
                    filmCollectionAdapter.notifyItemInserted(filmCollectionItems.size() - 1);
                    if (mainRecyclerViewState != null) {
                        // TODO: Почему то иногда здесь крашит. Хз почему. Позже разберусь 02.11.25 21:00
                        if (binding != null) {
                            binding.collectionsRecyclerview.getLayoutManager().onRestoreInstanceState(mainRecyclerViewState);
                            mainRecyclerViewState = null;
                        }
                    }
                    loadNextCollection();
                });

            }

            @Override
            public void onError(KinopoiskApiClientV2.ApiException error) {
                // Handle error
                loadNextCollection();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null) {
            mainRecyclerViewState = binding.collectionsRecyclerview.getLayoutManager().onSaveInstanceState();
        }
        binding.collectionsRecyclerview.setAdapter(null);

        if (appBarLayout != null) {
            appBarLayout.setLiftOnScrollTargetViewId(View.NO_ID);
        }
        binding = null;
    }

    @Override
    public void onCollectionClick(FilmCollectionAdapter.FilmCollectionItem collection) {
        Bundle bundle = new Bundle();
        bundle.putString("type", "collection");
        bundle.putString("data", collection.getTitle());
        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navigation_home_to_filtersListFragment, bundle);
    }

    @Override
    public void onFilmClick(com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models.FilmItem film) {
        Bundle bundle = new Bundle();
        bundle.putInt("kinopoiskId", film.getKinopoiskId());
        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navigation_home_to_filmDetailsFragment, bundle);
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add("Поиск").setIcon(R.drawable.find).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add("Избранное").setIcon(R.drawable.bookmark_add).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (Objects.equals(item.getTitle(), "Поиск")) {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navigation_home_to_findFragments);
        } else if (Objects.equals(item.getTitle(), "Избранное")) {
            Bundle bundle = new Bundle();
            bundle.putString("type", "bookmark");
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navigation_home_to_filtersListFragment, bundle);
        }
        return super.onOptionsItemSelected(item);
    }
}
