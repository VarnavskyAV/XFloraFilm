package com.alaka_ala.florafilm.ui.fragments.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentActivityBinding;
import com.alaka_ala.florafilm.ui.activities.MainActivity;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.db.FilmDetailsDao;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.db.KinopoiskDatabaseV2;
import com.alaka_ala.florafilm.ui.utils.kinopoiskV2.models.FilmDetails;

public class ActivityFragment extends Fragment implements HistoryViewAdapter.OnItemClickListener {
    private FragmentActivityBinding binding;
    private FilmDetailsDao filmDetailsDao;

    private RecyclerView rvHistoryView, rvResumeView, rvBookmark;

    private HistoryViewAdapter historyAdapter;
    private HistoryViewAdapter resumeAdapter;
    private HistoryViewAdapter bookmarkAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentActivityBinding.inflate(inflater, container, false);
        // скрываем нижнюю навигацию при выходе из этого фрагмента.
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showBottomNavigationView();
        }
        filmDetailsDao = KinopoiskDatabaseV2.getDatabase(getContext()).filmDetailsDao();

        bindingViews();
        setupRecyclerView();
        observeData();

        return binding.getRoot();
    }

    private void bindingViews() {
        rvHistoryView = binding.rvHistoryView;
        rvResumeView = binding.rvResumeView;
        rvBookmark = binding.rvBookmark;
    }

    /**
     * Настраивает RecyclerView, устанавливая LayoutManager и адаптеры.
     */
    private void setupRecyclerView() {
        historyAdapter = new HistoryViewAdapter(HistoryViewAdapter.ViewTypeItem.HORIZONTAL);
        historyAdapter.setOnItemClickListener(this);
        rvHistoryView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvHistoryView.setAdapter(historyAdapter);

        resumeAdapter = new HistoryViewAdapter(HistoryViewAdapter.ViewTypeItem.HORIZONTAL);
        resumeAdapter.setOnItemClickListener(this);
        rvResumeView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvResumeView.setAdapter(resumeAdapter);

        bookmarkAdapter = new HistoryViewAdapter(HistoryViewAdapter.ViewTypeItem.HORIZONTAL);
        bookmarkAdapter.setOnItemClickListener(this);
        rvBookmark.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvBookmark.setAdapter(bookmarkAdapter);

        setupFilterTitle();


    }

    private void setupFilterTitle() {
        binding.bookmarkFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("type", "bookmark");
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navigation_activity_to_filtersListFragment, bundle);
            }
        });

        binding.historyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("type", "history");
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navigation_activity_to_filtersListFragment, bundle);
            }
        });


        binding.resumeFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("type", "resume");
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navigation_activity_to_filtersListFragment, bundle);
            }
        });

    }

    /**
     * Устанавливает наблюдение за LiveData из DAO и обновляет адаптеры при изменении данных.
     */
    private void observeData() {
        filmDetailsDao.getByView().observe(getViewLifecycleOwner(), historyList -> {
            historyAdapter.submitList(historyList);
            updateUiVisibility();
        });

        filmDetailsDao.getByIsStartView().observe(getViewLifecycleOwner(), resumeList -> {
            resumeAdapter.submitList(resumeList);
            updateUiVisibility();
        });

        filmDetailsDao.getByBookmark().observe(getViewLifecycleOwner(), bookmarkList -> {
            bookmarkAdapter.submitList(bookmarkList);
            updateUiVisibility();
        });
    }

    /**
     * Обновляет видимость элементов пользовательского интерфейса в зависимости от наличия данных.
     * Если данных нет ни в одном из разделов, отображается текстовое поле, информирующее об этом.
     * В противном случае, разделы без данных скрываются.
     */
    private void updateUiVisibility() {
        boolean historyEmpty = historyAdapter.getItemCount() == 0;
        boolean resumeEmpty = resumeAdapter.getItemCount() == 0;
        boolean bookmarksEmpty = bookmarkAdapter.getItemCount() == 0;

        if (historyEmpty && resumeEmpty && bookmarksEmpty) {
            binding.textView4.setVisibility(View.VISIBLE);
            binding.historyRootLinear.setVisibility(View.GONE);
            binding.resumeRootLinear.setVisibility(View.GONE);
            binding.bookmarkRootLinear.setVisibility(View.GONE);
        } else {
            binding.textView4.setVisibility(View.GONE);
            binding.historyRootLinear.setVisibility(historyEmpty ? View.GONE : View.VISIBLE);
            binding.resumeRootLinear.setVisibility(resumeEmpty ? View.GONE : View.VISIBLE);
            binding.bookmarkRootLinear.setVisibility(bookmarksEmpty ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(FilmDetails film) {
        Bundle bundle = new Bundle();
        bundle.putInt("kinopoiskId", film.getKinopoiskId());
        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navigation_activity_to_filmDetailsFragment, bundle);
    }
}
