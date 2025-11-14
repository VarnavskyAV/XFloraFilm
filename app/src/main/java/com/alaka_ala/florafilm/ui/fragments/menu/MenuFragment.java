package com.alaka_ala.florafilm.ui.fragments.menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.BuildConfig;
import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.ui.fragments.menu.models.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends Fragment implements MenuAdapter.OnItemClickListener {

    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem("Прочитай","Иногда идёт что-то не по плану, но это нормально, ведь это неотъемлемая часть работы.","Подробнее"));
        menuItems.add(new MenuItem(R.drawable.update, "Доступно обновление", R.attr.StrokeUpdateColor));
        menuItems.add(new MenuItem(R.drawable.utorrent, "Торренты"));
        menuItems.add(new MenuItem(R.drawable.download, "Загрузки"));
        menuItems.add(new MenuItem(R.drawable.notification, "Ожидаемые"));
        menuItems.add(new MenuItem(R.drawable.settings, "Настройки"));
        menuItems.add(new MenuItem(R.drawable.info, "О приложении"));

        // Добавляем информационное сообщение
        menuItems.add(new MenuItem("Версия приложения: " + BuildConfig.VERSION_NAME));

        MenuAdapter adapter = new MenuAdapter(menuItems);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(MenuItem menuItem) {
        if (menuItem.getType() == MenuItem.ItemType.REGULAR) {
            Toast.makeText(getContext(), "Нажат пункт: " + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onInfoButtonClick(MenuItem menuItem, int buttonIndex) {
        String message = "Нажата кнопка " + buttonIndex + " в элементе '" + menuItem.getTitle() + "'";
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

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
