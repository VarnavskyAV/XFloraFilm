package com.alaka_ala.florafilm.fragments.menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
                        "≽^•⩊•^≼", "История изменений", "Поддержать!"));

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


    private int countTapSup = 0;
    @Override
    public void onInfoButtonClick(MenuItem menuItem, int buttonIndex) {
        //Пример логики
        if (buttonIndex == 1) { // Подробнее
            // Открыть страницу обновления
            Navigation.findNavController(getView()).navigate(R.id.action_navigation_menu_to_changelogFragment2);
        } else if (buttonIndex == 2) { // Позже
            // Просто закрыть
            countTapSup++;
            if (countTapSup == 1) {
                Toast.makeText(getContext(), "Спасибо, ее то мне и не хватало!", Toast.LENGTH_SHORT).show();
            } else if (countTapSup == 2) {
                Toast.makeText(getContext(), "Денег нет, но мы держимся!", Toast.LENGTH_SHORT).show();
            } else if (countTapSup == 3) {
                Toast.makeText(getContext(), "И вы не унывайте!", Toast.LENGTH_SHORT).show();
            } else if (countTapSup == 5) {
                Toast.makeText(getContext(), "да-да", Toast.LENGTH_SHORT).show();
            }else if (countTapSup < 19) {
                Toast.makeText(getContext(), "- " + (countTapSup + 1) + " - раз поддержал. Молоток!", Toast.LENGTH_SHORT).show();
            } else if (countTapSup == 20) {
                Toast.makeText(getContext(), "Ну однозначно лайк. Посмотри лучше фильм какой-нибудь! А я ушел!", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
