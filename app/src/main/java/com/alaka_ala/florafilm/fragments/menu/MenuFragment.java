package com.alaka_ala.florafilm.fragments.menu;

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
import com.alaka_ala.florafilm.activities.MainActivity;
import com.alaka_ala.florafilm.fragments.menu.models.MenuItem;
import com.alaka_ala.florafilm.utils.appUpdate.AppUpdateManager;
import com.alaka_ala.florafilm.utils.appUpdate.models.UpdateInfo;

import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends Fragment implements MenuAdapter.OnItemClickListener {
    private MenuAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // скрываем нижнюю навигацию при выходе из этого фрагмента.
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showBottomNavigationView();
        }

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem("Информация",
                "Приложение находится в разработке!\n\n" +
                        "Подгоняем приложение под ТВ \uD83D\uDCFA\uD83D\uDE0D \n\n" +
                        "Убир(в)аем баги ᡕᠵデᡁ᠊╾━\uD83D\uDCA5 \uD83E\uDEB2 \uD83D\uDC1B \n\n" +
                        "≽^•⩊•^≼ meow", ""));

        AppUpdateManager.getInstance(getContext()).checkForUpdate(new AppUpdateManager.UpdateCheckCallback() {
            @Override
            public void onUpdateAvailable(UpdateInfo updateInfo) {
                menuItems.add(1, new MenuItem(R.drawable.update, updateInfo.getReleaseNotes(), R.attr.StrokeUpdateColor));
                if (adapter != null) adapter.notifyItemChanged(1);
            }

            @Override
            public void onNoUpdateAvailable() {

            }

            @Override
            public void onError(String errorMessage) {

            }
        });


        //menuItems.add(new MenuItem(R.drawable.utorrent, "Торренты"));
        //menuItems.add(new MenuItem(R.drawable.download, "Загрузки"));
        //menuItems.add(new MenuItem(R.drawable.notification, "Ожидаемые"));
        menuItems.add(new MenuItem(R.drawable.settings, "Настройки"));

        // Добавляем информационное сообщение
        //menuItems.add(new MenuItem("Версия приложения: " + BuildConfig.VERSION_NAME));


        adapter = new MenuAdapter(menuItems);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(MenuItem menuItem) {
        if (menuItem.getType() == MenuItem.ItemType.REGULAR) {
            switch (menuItem.getTitle()) {
                case "Настройки":
                    Navigation.findNavController(getView()).navigate(R.id.action_navigation_menu_to_settingsFragment);
                    break;
            }
        }
    }

    @Override
    public void onInfoButtonClick(MenuItem menuItem, int buttonIndex) {

        // Пример логики
        // if (menuItem.getTitle().equals("Обновление")) {
        //     if (buttonIndex == 1) { // Подробнее
        //         // Открыть страницу обновления
        //     } else if (buttonIndex == 2) { // Позже
        //         // Просто закрыть
        //     }
        // }
    }
}
