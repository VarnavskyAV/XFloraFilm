package com.alaka_ala.florafilm.data.media;

import androidx.annotation.Keep;

import com.alaka_ala.florafilm.fragments.filmDetails.SelectorVoiceAdapter;
import com.alaka_ala.florafilm.utils.balancers.Balancer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Простой Serializable класс-контейнер для передачи всех необходимых данных в плеер.
 */
@Keep
public class PlayerLaunchData implements Serializable {
    /**Типы ресурсов для разных источников. Доступные можно посмотреть тут - {@link Balancer} */
    public int getSourceType() {
        return sourceType;
    }

    private int sourceType;
    /**
     * Полные данные из адаптера
     */
    private final List<SelectorVoiceAdapter.Folder> rootFolders;
    /**
     * Индекс патчей.
     * пример для фильма - [0,0,0];
     * пример Для сериала - [0,0,0,0,0];
     * расшифровка для Фильма - Балансер -> Озвучка -> Качество
     * расшифровка для Сериал - Балансер -> Сезон -> Серия -> Озвучка -> Качество
     */
    private final List<Integer> selectedIndexPath;

    public PlayerLaunchData(int sourceType, List<SelectorVoiceAdapter.Folder> rootFolders, List<Integer> selectedIndexPath) {
        this.sourceType = sourceType;
        this.rootFolders = rootFolders;
        this.selectedIndexPath = selectedIndexPath;
    }


    public List<SelectorVoiceAdapter.Folder> getRootFolders() {
        return rootFolders;
    }

    public List<Integer> getSelectedIndexPath() {
        return selectedIndexPath;
    }




}
