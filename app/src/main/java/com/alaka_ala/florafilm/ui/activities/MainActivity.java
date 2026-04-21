package com.alaka_ala.florafilm.ui.activities;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.LiveData;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.ui.fragments.filmDetails.SelectorVoiceAdapter;
import com.alaka_ala.florafilm.utils.balancers.alloha.AllohaApiClient;
import com.alaka_ala.florafilm.utils.other.ContentStructureTracker;
import com.alaka_ala.unofficial_kinopoisk_api.db.FilmDetailsDao;
import com.alaka_ala.unofficial_kinopoisk_api.db.KinopoiskDatabaseV2;
import com.alaka_ala.unofficial_kinopoisk_api.models.FilmDetails;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView navView;
    private NavController navController;
    private Toolbar toolbar;
    private FilmDetailsDao filmDetailsDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Включаем edge-to-edge режим
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        // 2. Находим корневую вьюху
        View rootLayout = findViewById(R.id.main);

        // 3. Настраиваем цвет иконок статус-бара (светлые/темные)
        boolean isSystemInDarkTheme = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), rootLayout);
        controller.setAppearanceLightStatusBars(!isSystemInDarkTheme);

        // 4. Устанавливаем цвет статус-бара (как у AppBarLayout)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int statusBarColor = getColorFromAttribute(R.attr.MainColorApp);
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(statusBarColor);
        }

        // 5. Инициализация UI
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navView = findViewById(R.id.bottom_nav_view);

        // 6. Настройка отступов для системных баров
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            navView.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        // 7. Настройка навигации
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_activity, R.id.navigation_menu)
                .build();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        filmDetailsDao = KinopoiskDatabaseV2.getDatabase(this).filmDetailsDao();
    }

    public void showBottomNavigationView() {
        if (navView.getVisibility() == View.GONE) {
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            navView.startAnimation(slideUp);
            navView.setVisibility(View.VISIBLE);
        }
    }

    // Вспомогательный метод для получения цвета из атрибута
    private int getColorFromAttribute(int attributeId) {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(attributeId, typedValue, true);
        return ContextCompat.getColor(this, typedValue.resourceId);
    }


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

    /**
     * Показывает Toolbar с анимацией.
     */
    public void showToolbar() {
        if (toolbar.getVisibility() == View.GONE) {
            AlphaAnimation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setDuration(200);
            toolbar.startAnimation(fadeIn);
            toolbar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Скрывает Toolbar с анимацией.
     */
    public void hideToolbar() {
        if (toolbar.getVisibility() == View.VISIBLE) {
            AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setDuration(200);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    toolbar.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            toolbar.startAnimation(fadeOut);
        }
    }

    public void setToolbarTitle(String title) {
        if (toolbar == null) return;
        toolbar.setTitle(title);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }



    // TODO: Доделать отслеживание новых событий в фильме
    // Не доделанный код проверки изменений данных в базе на фильм.
    private void runContentStructureTracker() {
        ContentStructureTracker tracker = new ContentStructureTracker(this);
        // Получаем список ID которые отслеживаются.
        List<FilmDetails> films = filmDetailsDao.getFilmByObserveVoice().getValue();
        int kinopoiskId = films.get(0).getKinopoiskId();
        boolean isSerial = films.get(0).isSerial();
        AllohaApiClient allohaApiClient = new AllohaApiClient("4cd98e08f1e1f0273692e35b16b690");
        getMainExecutor().execute(() -> {
            try {
                allohaApiClient.fetch(kinopoiskId, new SelectorVoiceAdapter.AdapterData.AdapterDataCallback() {
                    @Override
                    public void onDataReady(SelectorVoiceAdapter.AdapterData data) {
                        boolean hasChanged = tracker.hasStructureChanged(String.valueOf(kinopoiskId), isSerial, data.getRootFolders());
                        if (hasChanged) {
                            MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(getApplicationContext());
                            alert.setTitle("Найдены изменения в Фильме/Сериале");
                            alert.setPositiveButton("Перейти", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("kinopoiskId", kinopoiskId);
                                    navController.navigate(R.id.action_navigation_home_to_filmDetailsFragment, bundle);
                                }
                            });
                            alert.show();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(MainActivity.this, "ERROR: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });


    }
}
