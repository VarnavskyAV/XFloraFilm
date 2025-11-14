package com.alaka_ala.florafilm.ui.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.alaka_ala.florafilm.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView navView;
    private NavController navController; // Сделаем navController полем класса
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navView = findViewById(R.id.bottom_nav_view);


        View rootLayout = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Убираем верхний отступ для rootLayout, чтобы контент мог заходить под статус-бар
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            navView.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        boolean isSystemInDarkTheme = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (!isSystemInDarkTheme) {
            WindowCompat.getInsetsController(getWindow(), rootLayout).setAppearanceLightStatusBars(true);
        } else {
            WindowCompat.getInsetsController(getWindow(), rootLayout).setAppearanceLightStatusBars(false);
        }

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_activity, R.id.navigation_menu)
                .build();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController(); // Инициализируем navController

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    /**
     * Показывает нижнюю навигационную панель с анимацией.
     * Этот метод делает BottomNavigationView видимым, используя анимацию slide-in.
     */
    public void showBottomNavigationView() {
        if (navView.getVisibility() == View.GONE) {
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            navView.startAnimation(slideUp);
            navView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Скрывает нижнюю навигационную панель с анимацией.
     * Этот метод запускает анимацию slide-out и делает BottomNavigationView невидимым после ее завершения.
     */
    public void hideBottomNavigationView() {
        if (navView.getVisibility() == View.VISIBLE) {
            Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
            slideDown.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    navView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            navView.startAnimation(slideDown);
        }
    }

    public void setToolbarTitle(String title) {
        if (toolbar == null) return;
        toolbar.setTitle(title);
    }

    /**
     * Обрабатывает нажатие кнопки "назад" в тулбаре.
     * Делегирует обработку NavController для корректной навигации вверх по стеку.
     * @return true, если событие было обработано, false в противном случае.
     */
    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
