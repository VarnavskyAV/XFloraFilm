package com.alaka_ala.florafilm.ui.fragments.home;

import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.unofficial_kinopoisk_api.models.FilmItem;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Верхний блок главной: карусель с автопрокруткой и индикатором.
 */
public class HeroRowAdapter extends RecyclerView.Adapter<HeroRowAdapter.HeroHolder> {

    private static final long AUTO_SCROLL_MS = 5600L;

    /**
     * Состояние с сервера: null — первая загрузка; пустой список — ошибка/пустой ответ; иначе данные.
     */
    private List<FilmItem> films;
    /**
     * Последняя успешная подборка: если при перепривязке {@link #films} временно null, всё равно показываем её.
     */
    private List<FilmItem> lastHeroFilms;
    private FilmAdapter.OnFilmClickListener onFilmClickListener;

    public HeroRowAdapter() {
        films = null;
    }

    public void setOnFilmClickListener(FilmAdapter.OnFilmClickListener listener) {
        this.onFilmClickListener = listener;
    }

    public void setFilms(List<FilmItem> items) {
        if (items == null) {
            films = null;
        } else {
            films = new ArrayList<>(items);
            if (!items.isEmpty()) {
                lastHeroFilms = new ArrayList<>(items);
            }
        }
        notifyItemChanged(0);
    }

    /**
     * Что реально показывать: актуальный ответ API или, при сбое состояния, последний успешный кэш.
     */
    @Nullable
    private List<FilmItem> getEffectiveFilmsForBind() {
        if (films != null) {
            return films;
        }
        return lastHeroFilms;
    }

    @NonNull
    @Override
    public HeroHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_hero, parent, false);
        HeroHolder holder = new HeroHolder(view);
        holder.setIsRecyclable(false);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull HeroHolder holder, int position) {
        holder.bind(getEffectiveFilmsForBind(), onFilmClickListener);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull HeroHolder holder) {
        super.onViewAttachedToWindow(holder);
        // После ухода ячейки за экран RecyclerView иногда не делает повторный bind, а ViewPager уже сброшен.
        List<FilmItem> effective = getEffectiveFilmsForBind();
        if (effective != null && !effective.isEmpty() && holder.isPagerAdapterMissing()) {
            holder.bind(effective, onFilmClickListener);
        }
    }

    @Override
    public void onViewRecycled(@NonNull HeroHolder holder) {
        // Нельзя вызывать setAdapter(null) здесь: при возврате скролла bind может не вызваться — остаётся вечный progress.
        holder.onRecycleLight();
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    static class HeroHolder extends RecyclerView.ViewHolder {
        private final CircularProgressIndicator progress;
        private final MaterialCardView heroCard;
        private final ViewPager2 viewPager;
        private final LinearLayout dots;
        private final HomeHeroPagerAdapter pagerAdapter = new HomeHeroPagerAdapter();
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final Runnable autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (pagerAdapter.getItemCount() <= 1) {
                    return;
                }
                int next = (viewPager.getCurrentItem() + 1) % pagerAdapter.getItemCount();
                viewPager.setCurrentItem(next, true);
                handler.postDelayed(this, AUTO_SCROLL_MS);
            }
        };

        HeroHolder(@NonNull View itemView) {
            super(itemView);
            progress = itemView.findViewById(R.id.hero_progress);
            heroCard = itemView.findViewById(R.id.hero_card);
            viewPager = itemView.findViewById(R.id.hero_viewpager);
            dots = itemView.findViewById(R.id.hero_dots);
        }

        boolean isPagerAdapterMissing() {
            return viewPager.getAdapter() == null;
        }

        void bind(List<FilmItem> filmList, FilmAdapter.OnFilmClickListener clickListener) {
            prepareForNewBind();
            pagerAdapter.setOnFilmClickListener(clickListener);

            if (filmList == null) {
                progress.setVisibility(View.VISIBLE);
                heroCard.setVisibility(View.GONE);
                dots.setVisibility(View.GONE);
                return;
            }
            if (filmList.isEmpty()) {
                progress.setVisibility(View.GONE);
                heroCard.setVisibility(View.GONE);
                dots.setVisibility(View.GONE);
                return;
            }

            progress.setVisibility(View.GONE);
            heroCard.setVisibility(View.VISIBLE);

            pagerAdapter.setFilms(filmList);
            viewPager.setAdapter(pagerAdapter);
            viewPager.setCurrentItem(0, false);
            viewPager.registerOnPageChangeCallback(pageChangeCallback);
            rebuildDots(filmList.size(), 0);

            if (filmList.size() > 1) {
                handler.postDelayed(autoScrollRunnable, AUTO_SCROLL_MS);
            }
        }

        private final ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                rebuildDots(pagerAdapter.getItemCount(), position);
            }
        };

        void stopAutoScroll() {
            handler.removeCallbacks(autoScrollRunnable);
        }

        /** Только при уходе в pool — без сброса адаптера ViewPager2. */
        void onRecycleLight() {
            stopAutoScroll();
            viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        }

        /** Перед новой привязкой данных — полный сброс ViewPager2 (устраняет артефакты страниц). */
        void prepareForNewBind() {
            stopAutoScroll();
            viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
            viewPager.setAdapter(null);
        }

        private void rebuildDots(int count, int selected) {
            dots.removeAllViews();
            if (count <= 1) {
                dots.setVisibility(View.GONE);
                return;
            }
            dots.setVisibility(View.VISIBLE);
            float dotPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f,
                    itemView.getResources().getDisplayMetrics());
            int gap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f,
                    itemView.getResources().getDisplayMetrics());
            for (int i = 0; i < count; i++) {
                View dot = new View(itemView.getContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) dotPx, (int) dotPx);
                lp.setMargins(gap, 0, gap, 0);
                dot.setLayoutParams(lp);
                dot.setBackgroundResource(R.drawable.hero_dot);
                dot.setAlpha(i == selected ? 1f : 0.35f);
                dots.addView(dot);
            }
        }
    }
}
