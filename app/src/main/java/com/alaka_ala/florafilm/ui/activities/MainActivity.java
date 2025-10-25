package com.alaka_ala.florafilm.ui.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

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

    /**
     * Вызывается при создании активности.
     * @param savedInstanceState Если активность создается заново, этот параметр содержит данные, которые она недавно предоставила в onSaveInstanceState.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView navView = findViewById(R.id.bottom_nav_view);

        View rootLayout = findViewById(R.id.main);

        // Настройка отступов для системных панелей
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Применяем верхний отступ к корневому элементу, чтобы Toolbar не заезжал под статус-бар
            // Левый, правый и нижний отступы не применяем к корневому элементу, чтобы контент был edge-to-edge
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            // Применяем нижний отступ к BottomNavigationView, чтобы он не перекрывался системной панелью жестов
            navView.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        // Настройка цвета текста статус-бара в зависимости от темы
        boolean isSystemInDarkTheme = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (!isSystemInDarkTheme) {
            // Если тема светлая, делаем иконки статус-бара темными
            WindowCompat.getInsetsController(getWindow(), rootLayout).setAppearanceLightStatusBars(true);
        } else {
            // Если тема темная, делаем иконки статус-бара светлыми (по умолчанию)
            WindowCompat.getInsetsController(getWindow(), rootLayout).setAppearanceLightStatusBars(false);
        }

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_activity, R.id.navigation_menu)
                .build();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }
}