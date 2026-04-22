package com.alaka_ala.florafilm.fragments.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentActivityBinding;
import com.alaka_ala.florafilm.activities.MainActivity;
import com.alaka_ala.florafilm.utils.settings.AppPreferences;
import com.alaka_ala.unofficial_kinopoisk_api.db.FilmDetailsDao;
import com.alaka_ala.unofficial_kinopoisk_api.db.KinopoiskDatabaseV2;
import com.alaka_ala.unofficial_kinopoisk_api.models.FilmDetails;
import com.google.android.material.appbar.AppBarLayout;

import java.util.List;

public class ActivityFragment extends Fragment implements HistoryViewAdapter.OnItemClickListener {
    private FragmentActivityBinding binding;
    private FilmDetailsDao filmDetailsDao;
    private AppBarLayout appBarLayout;
    private RecyclerView rvHistoryView, rvResumeView, rvBookmark;

    private HistoryViewAdapter historyAdapter;
    private HistoryViewAdapter resumeAdapter;
    private HistoryViewAdapter bookmarkAdapter;

    /** Размеры из LiveData: {@link androidx.recyclerview.widget.ListAdapter#submitList} обновляет список асинхронно, нельзя полагаться на {@code getItemCount()} сразу после вызова. */
    private int historyCount;
    private int resumeCount;
    private int bookmarkCount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentActivityBinding.inflate(inflater, container, false);
        // показываем нижнюю навигацию при входе
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showBottomNavigationView();
        }
        filmDetailsDao = KinopoiskDatabaseV2.getDatabase(getContext()).filmDetailsDao();
        if (getActivity() instanceof MainActivity) {
            appBarLayout = getActivity().findViewById(R.id.app_bar_layout);
            if (appBarLayout != null) {
                appBarLayout.setLiftOnScrollTargetViewId(binding.collectionsScrollview.getId());
            }
        }
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
        bookmarkAdapter = new HistoryViewAdapter(HistoryViewAdapter.ViewTypeItem.HORIZONTAL);
        bookmarkAdapter.setOnItemClickListener(this);
        rvBookmark.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvBookmark.setAdapter(bookmarkAdapter);
        stripChangeAnimations(rvBookmark);

        historyAdapter = new HistoryViewAdapter(HistoryViewAdapter.ViewTypeItem.HORIZONTAL);
        historyAdapter.setOnItemClickListener(this);
        rvHistoryView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvHistoryView.setAdapter(historyAdapter);
        stripChangeAnimations(rvHistoryView);

        resumeAdapter = new HistoryViewAdapter(HistoryViewAdapter.ViewTypeItem.HORIZONTAL);
        resumeAdapter.setOnItemClickListener(this);
        rvResumeView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvResumeView.setAdapter(resumeAdapter);
        stripChangeAnimations(rvResumeView);

        setupFilterTitle();
    }

    private static void stripChangeAnimations(RecyclerView recyclerView) {
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
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
        binding.bookmarkFilter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String[] items = new String[]{"Очистить", "Скрыть/Показать список"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Закладки");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            new Thread(() -> {
                                filmDetailsDao.clearBookmarks();
                            }).start();
                        } else {
                            boolean isVisible = AppPreferences.ActivityListViewAdapters.getVisibleAdapter(
                                    AppPreferences.ActivityListViewAdapters.ListNames.BOOKMARK, getContext());
                            AppPreferences.ActivityListViewAdapters.setVisibleAdapter(
                                    AppPreferences.ActivityListViewAdapters.ListNames.BOOKMARK,
                                    getContext(),
                                    !isVisible);
                            isVisible = AppPreferences.ActivityListViewAdapters.getVisibleAdapter(
                                    AppPreferences.ActivityListViewAdapters.ListNames.BOOKMARK, getContext());
                            updateUiVisibility();
                            if (getActivity() != null) {
                                getActivity().invalidateOptionsMenu();
                            }
                        }
                    }
                });
                builder.show();
                return true;
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
        binding.historyFilter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String[] items = new String[]{"Очистить", "Скрыть/Показать список"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("История");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i==0) {
                            new Thread(() -> {
                                filmDetailsDao.clearHistory();
                            }).start();
                        } else {
                            boolean isVisible = AppPreferences.ActivityListViewAdapters.getVisibleAdapter(
                                    AppPreferences.ActivityListViewAdapters.ListNames.HISTORYVIEW, getContext());
                            AppPreferences.ActivityListViewAdapters.setVisibleAdapter(
                                    AppPreferences.ActivityListViewAdapters.ListNames.HISTORYVIEW,
                                    getContext(), !isVisible
                            );
                            isVisible = AppPreferences.ActivityListViewAdapters.getVisibleAdapter(
                                    AppPreferences.ActivityListViewAdapters.ListNames.HISTORYVIEW, getContext());
                            updateUiVisibility();
                            if (getActivity() != null) {
                                getActivity().invalidateOptionsMenu();
                            }
                        }
                    }
                });
                builder.show();
                return true;
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
        binding.resumeFilter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String[] items = new String[]{"Очистить", "Скрыть/Показать список"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Продолжить просмотр");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i==0) {
                            new Thread(() -> {
                                filmDetailsDao.clearResume();
                            }).start();
                        } else {
                            boolean isVisible = AppPreferences.ActivityListViewAdapters.getVisibleAdapter(
                                    AppPreferences.ActivityListViewAdapters.ListNames.RESUMEVIEW, getContext());
                            AppPreferences.ActivityListViewAdapters.setVisibleAdapter(
                                    AppPreferences.ActivityListViewAdapters.ListNames.RESUMEVIEW,
                                    getContext(), !isVisible
                            );
                            isVisible = AppPreferences.ActivityListViewAdapters.getVisibleAdapter(
                                    AppPreferences.ActivityListViewAdapters.ListNames.RESUMEVIEW, getContext());
                            updateUiVisibility();
                            if (getActivity() != null) {
                                getActivity().invalidateOptionsMenu();
                            }
                        }
                    }
                });
                builder.show();
                return true;
            }
        });

    }

    /**
     * Устанавливает наблюдение за LiveData из DAO и обновляет адаптеры при изменении данных.
     */
    private void observeData() {
        filmDetailsDao.getByView().observe(getViewLifecycleOwner(), historyList -> {
            historyCount = listSize(historyList);
            historyAdapter.submitList(historyList);
            updateUiVisibility();
        });

        filmDetailsDao.getByIsStartView().observe(getViewLifecycleOwner(), resumeList -> {
            resumeCount = listSize(resumeList);
            resumeAdapter.submitList(resumeList);
            updateUiVisibility();
        });

        filmDetailsDao.getByBookmark().observe(getViewLifecycleOwner(), bookmarkList -> {
            bookmarkCount = listSize(bookmarkList);
            bookmarkAdapter.submitList(bookmarkList);
            updateUiVisibility();
        });
    }

    private static int listSize(List<?> list) {
        return list == null ? 0 : list.size();
    }

    /**
     * Обновляет видимость элементов пользовательского интерфейса в зависимости от наличия данных.
     * Если данных нет ни в одном из разделов, отображается текстовое поле, информирующее об этом.
     * В противном случае, разделы без данных скрываются.
     */
    private void updateUiVisibility() {
        Context ctx = getContext();
        if (ctx == null || binding == null) {
            return;
        }

        boolean showBookmarkList = AppPreferences.ActivityListViewAdapters.getVisibleAdapter(
                AppPreferences.ActivityListViewAdapters.ListNames.BOOKMARK, ctx);
        boolean showHistoryList = AppPreferences.ActivityListViewAdapters.getVisibleAdapter(
                AppPreferences.ActivityListViewAdapters.ListNames.HISTORYVIEW, ctx);
        boolean showResumeList = AppPreferences.ActivityListViewAdapters.getVisibleAdapter(
                AppPreferences.ActivityListViewAdapters.ListNames.RESUMEVIEW, ctx);

        boolean anySectionEnabled = showBookmarkList || showHistoryList || showResumeList;

        boolean showBookmark = showBookmarkList && bookmarkCount > 0;
        boolean showHistory = showHistoryList && historyCount > 0;
        boolean showResume = showResumeList && resumeCount > 0;
        boolean anyVisibleBlock = showBookmark || showHistory || showResume;

        if (!anySectionEnabled) {
            binding.emptyStateTitle.setText(R.string.activity_empty_all_hidden_title);
            binding.emptyStateMessage.setText(R.string.activity_empty_all_hidden_message);
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.activityPageHeader.setVisibility(View.VISIBLE);
            binding.activitySections.setVisibility(View.GONE);
            binding.bookmarkRootLinear.setVisibility(View.GONE);
            binding.historyRootLinear.setVisibility(View.GONE);
            binding.resumeRootLinear.setVisibility(View.GONE);
            return;
        }

        binding.emptyStateTitle.setText(R.string.activity_empty_title);
        binding.emptyStateMessage.setText(R.string.activity_empty_message);

        if (!anyVisibleBlock) {
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.activityPageHeader.setVisibility(View.VISIBLE);
            binding.activitySections.setVisibility(View.GONE);
            binding.bookmarkRootLinear.setVisibility(View.GONE);
            binding.historyRootLinear.setVisibility(View.GONE);
            binding.resumeRootLinear.setVisibility(View.GONE);
            return;
        }

        binding.emptyState.setVisibility(View.GONE);
        binding.activityPageHeader.setVisibility(View.VISIBLE);
        binding.activitySections.setVisibility(View.VISIBLE);
        binding.bookmarkRootLinear.setVisibility(showBookmark ? View.VISIBLE : View.GONE);
        binding.historyRootLinear.setVisibility(showHistory ? View.VISIBLE : View.GONE);
        binding.resumeRootLinear.setVisibility(showResume ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.activity_fragment_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Context ctx = getContext();
        if (ctx == null) {
            return;
        }
        MenuItem sectionsItem = menu.findItem(R.id.activity_menu_sections);
        SubMenu sectionsSub = sectionsItem != null ? sectionsItem.getSubMenu() : null;
        MenuItem resume = menu.findItem(R.id.activity_menu_show_resume);
        MenuItem bookmark = menu.findItem(R.id.activity_menu_show_bookmark);
        MenuItem history = menu.findItem(R.id.activity_menu_show_history);
        if (resume == null && sectionsSub != null) {
            resume = sectionsSub.findItem(R.id.activity_menu_show_resume);
        }
        if (bookmark == null && sectionsSub != null) {
            bookmark = sectionsSub.findItem(R.id.activity_menu_show_bookmark);
        }
        if (history == null && sectionsSub != null) {
            history = sectionsSub.findItem(R.id.activity_menu_show_history);
        }
        if (resume != null) {
            resume.setChecked(AppPreferences.ActivityListViewAdapters.getVisibleAdapter(
                    AppPreferences.ActivityListViewAdapters.ListNames.RESUMEVIEW, ctx));
        }
        if (bookmark != null) {
            bookmark.setChecked(AppPreferences.ActivityListViewAdapters.getVisibleAdapter(
                    AppPreferences.ActivityListViewAdapters.ListNames.BOOKMARK, ctx));
        }
        if (history != null) {
            history.setChecked(AppPreferences.ActivityListViewAdapters.getVisibleAdapter(
                    AppPreferences.ActivityListViewAdapters.ListNames.HISTORYVIEW, ctx));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Context ctx = getContext();
        if (ctx == null) {
            return super.onOptionsItemSelected(item);
        }
        int id = item.getItemId();
        if (id == R.id.activity_menu_show_all) {
            AppPreferences.ActivityListViewAdapters.setVisibleAdapter(
                    AppPreferences.ActivityListViewAdapters.ListNames.RESUMEVIEW, ctx, true);
            AppPreferences.ActivityListViewAdapters.setVisibleAdapter(
                    AppPreferences.ActivityListViewAdapters.ListNames.BOOKMARK, ctx, true);
            AppPreferences.ActivityListViewAdapters.setVisibleAdapter(
                    AppPreferences.ActivityListViewAdapters.ListNames.HISTORYVIEW, ctx, true);
            updateUiVisibility();
            requireActivity().invalidateOptionsMenu();
            return true;
        }
        if (id == R.id.activity_menu_show_resume) {
            boolean cur = AppPreferences.ActivityListViewAdapters.getVisibleAdapter(
                    AppPreferences.ActivityListViewAdapters.ListNames.RESUMEVIEW, ctx);
            AppPreferences.ActivityListViewAdapters.setVisibleAdapter(
                    AppPreferences.ActivityListViewAdapters.ListNames.RESUMEVIEW, ctx, !cur);
            updateUiVisibility();
            requireActivity().invalidateOptionsMenu();
            return true;
        }
        if (id == R.id.activity_menu_show_bookmark) {
            boolean cur = AppPreferences.ActivityListViewAdapters.getVisibleAdapter(
                    AppPreferences.ActivityListViewAdapters.ListNames.BOOKMARK, ctx);
            AppPreferences.ActivityListViewAdapters.setVisibleAdapter(
                    AppPreferences.ActivityListViewAdapters.ListNames.BOOKMARK, ctx, !cur);
            updateUiVisibility();
            requireActivity().invalidateOptionsMenu();
            return true;
        }
        if (id == R.id.activity_menu_show_history) {
            boolean cur = AppPreferences.ActivityListViewAdapters.getVisibleAdapter(
                    AppPreferences.ActivityListViewAdapters.ListNames.HISTORYVIEW, ctx);
            AppPreferences.ActivityListViewAdapters.setVisibleAdapter(
                    AppPreferences.ActivityListViewAdapters.ListNames.HISTORYVIEW, ctx, !cur);
            updateUiVisibility();
            requireActivity().invalidateOptionsMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(FilmDetails film) {
        Bundle bundle = new Bundle();
        bundle.putInt("kinopoiskId", film.getKinopoiskId());
        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navigation_activity_to_filmDetailsFragment, bundle);
    }
}
