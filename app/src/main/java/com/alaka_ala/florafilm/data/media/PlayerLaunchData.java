package com.alaka_ala.florafilm.data.media;

import com.alaka_ala.florafilm.ui.fragments.filmDetails.SelectorVoiceAdapter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Простой Serializable класс-контейнер для передачи всех необходимых данных в плеер.
 */
public class PlayerLaunchData implements Serializable {
    /**Типы ресурсов для разных источников. Доступные можно посмотреть тут - {@link com.alaka_ala.florafilm.utils.balancers.Balancer} */
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
    /**
     * Карта сохранения позиции просмотра.
     * Ключ - это строковое представление IndexPath (например, "0_1_4_0_1"),
     * Значение - позиция в плеере (long).
     */
    private Map<String, Long> lastPositionPlayerView;


    public PlayerLaunchData(int sourceType, List<SelectorVoiceAdapter.Folder> rootFolders, List<Integer> selectedIndexPath) {
        this.sourceType = sourceType;
        this.lastPositionPlayerView = new HashMap<>();
        this.rootFolders = rootFolders;
        this.selectedIndexPath = selectedIndexPath;
    }

    /**
     * Генерирует уникальный строковый ключ из списка индексов.
     * @param path Путь в виде списка индексов.
     * @return Строковый ключ, например "0_1_4_0_1".
     */
    public static String getIndexPathKey(List<Integer> path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        return path.stream().map(String::valueOf).collect(Collectors.joining("_"));
    }

    /**
     * Получает сохраненную позицию для указанного пути.
     * @param currentPath Путь, для которого нужно получить позицию.
     * @return Сохраненная позиция или null, если для этого пути ничего не сохранено.
     */
    public Long getLastPositionPlayerView(List<Integer> currentPath) {
        String key = getIndexPathKey(currentPath);
        return lastPositionPlayerView.get(key);
    }

    public Map<String, Long> getListPositionPlayerView() {
        return this.lastPositionPlayerView;
    }

    public List<SelectorVoiceAdapter.Folder> getRootFolders() {
        return rootFolders;
    }

    public List<Integer> getSelectedIndexPath() {
        return selectedIndexPath;
    }

    public void setLastPositionPlayerView(Map<String, Long> lastPositionPlayerView) {
        if (lastPositionPlayerView != null) {
            this.lastPositionPlayerView = lastPositionPlayerView;
        }
    }

    /**
     * Обновляет позицию просмотра для указанного пути.
     * @param path Путь, для которого сохраняется позиция.
     * @param positionPlayer Текущая позиция плеера.
     */
    public void updateLastPositionPlayerView(List<Integer> path, long positionPlayer) {
        String key = getIndexPathKey(path);
        lastPositionPlayerView.put(key, positionPlayer);
    }
}
