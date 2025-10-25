# 🎬 Оптимизированный Kinopoisk API

Современная, оптимизированная версия клиента для работы с Kinopoisk API. Создана на основе анализа существующего кода с улучшениями архитектуры, производительности и удобства использования.

## 🚀 Основные улучшения

### ✅ Архитектурные улучшения
- **Современные подходы**: Использование Gson для автоматической сериализации/десериализации
- **Типизированные колбэки**: Замена множественных интерфейсов на единый `ApiCallback<T>`
- **Улучшенная обработка ошибок**: Централизованная обработка с кастомными исключениями
- **Безопасные методы**: Автоматическая проверка null значений и пустых строк

### ✅ Производительность
- **Оптимизированные HTTP запросы**: Настроенные таймауты и пулы соединений
- **Умное кэширование**: Автоматическое управление HTTP кэшем
- **Параллельные запросы**: Поддержка множественных одновременных запросов
- **Минимальное потребление памяти**: Оптимизированные модели данных

### ✅ Удобство использования
- **Простой API**: Интуитивно понятные методы
- **Автодополнение**: Полная поддержка IDE
- **Документация**: Подробные комментарии и примеры
- **Гибкость**: Возможность кастомизации HTTP клиента

## 📦 Структура проекта

```
API Kinopoisk/
├── models/                 # Модели данных
│   ├── BaseModel.java     # Базовый класс для всех моделей
│   ├── Genre.java         # Модель жанра
│   ├── Country.java       # Модель страны
│   ├── FilmTrailer.java   # Модель трейлера
│   ├── FilmItem.java      # Краткая информация о фильме
│   ├── FilmDetails.java   # Детальная информация о фильме
│   ├── FilmCollection.java # Коллекция фильмов
│   └── FilmImages.java    # Изображения фильма
├── utils/
│   └── Constants.java     # Константы API
├── KinopoiskApiClient.java # Основной API клиент
└── README.md              # Документация
```

## 🔧 Установка и настройка

### Зависимости
```gradle
dependencies {
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

### Инициализация
```java
// Создание клиента с API ключом
KinopoiskApiClient client = new KinopoiskApiClient("your-api-key");

// Создание клиента с кастомным HTTP клиентом
OkHttpClient customClient = new OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .build();
KinopoiskApiClient client = new KinopoiskApiClient("your-api-key", customClient);
```

## 📖 Примеры использования

### Получение популярных фильмов
```java
client.getTopPopularAll(1, new KinopoiskApiClient.ApiCallback<FilmCollection>() {
    @Override
    public void onSuccess(FilmCollection result) {
        System.out.println("Найдено фильмов: " + result.getItemsCount());
        for (FilmItem film : result.getItems()) {
            System.out.println(film.getBestName() + " (" + film.getYear() + ")");
        }
    }
    
    @Override
    public void onError(ApiException error) {
        System.err.println("Ошибка: " + error.getMessage());
    }
});
```

### Поиск фильмов
```java
client.searchFilms("Интерстеллар", 1, new KinopoiskApiClient.ApiCallback<FilmCollection>() {
    @Override
    public void onSuccess(FilmCollection result) {
        if (!result.isEmpty()) {
            FilmItem film = result.getItems().get(0);
            System.out.println("Найден фильм: " + film.getBestName());
            System.out.println("Рейтинг: " + film.getRatingKinopoisk());
            System.out.println("Описание: " + film.getDescription());
        }
    }
    
    @Override
    public void onError(ApiException error) {
        System.err.println("Ошибка поиска: " + error.getMessage());
    }
});
```

### Получение детальной информации
```java
client.getFilmDetails(435, new KinopoiskApiClient.ApiCallback<FilmDetails>() {
    @Override
    public void onSuccess(FilmDetails film) {
        System.out.println("Название: " + film.getBestName());
        System.out.println("Год: " + film.getYear());
        System.out.println("Длительность: " + film.getFormattedDuration());
        System.out.println("Описание: " + film.getBestDescription());
        
        // Вывод жанров
        System.out.println("Жанры:");
        for (Genre genre : film.getGenres()) {
            System.out.println("- " + genre.getName());
        }
        
        // Вывод стран
        System.out.println("Страны:");
        for (Country country : film.getCountries()) {
            System.out.println("- " + country.getName());
        }
    }
    
    @Override
    public void onError(ApiException error) {
        System.err.println("Ошибка получения деталей: " + error.getMessage());
    }
});
```

### Получение трейлеров
```java
client.getFilmTrailers(435, new KinopoiskApiClient.ApiCallback<List<FilmTrailer>>() {
    @Override
    public void onSuccess(List<FilmTrailer> trailers) {
        System.out.println("Найдено трейлеров: " + trailers.size());
        for (FilmTrailer trailer : trailers) {
            System.out.println(trailer.getName() + " (" + trailer.getSite() + ")");
            System.out.println("URL: " + trailer.getUrl());
        }
    }
    
    @Override
    public void onError(ApiException error) {
        System.err.println("Ошибка получения трейлеров: " + error.getMessage());
    }
});
```

### Получение изображений
```java
client.getFilmImages(435, Constants.ImageTypes.POSTER, 1, 
    new KinopoiskApiClient.ApiCallback<FilmImages>() {
        @Override
        public void onSuccess(FilmImages images) {
            System.out.println("Найдено постеров: " + images.getItemsCount());
            for (FilmImages.FilmImage image : images.getItems()) {
                System.out.println("Размер: " + image.getWidth() + "x" + image.getHeight());
                System.out.println("URL: " + image.getBestImageUrl());
            }
        }
        
        @Override
        public void onError(ApiException error) {
            System.err.println("Ошибка получения изображений: " + error.getMessage());
        }
    });
```

## 🎯 Доступные методы API

### Коллекции фильмов
- `getTopPopularAll(int page, ApiCallback<FilmCollection> callback)` - Популярные фильмы и сериалы
- `getTopPopularMovies(int page, ApiCallback<FilmCollection> callback)` - Популярные фильмы
- `getTop250Movies(int page, ApiCallback<FilmCollection> callback)` - Топ 250 фильмов
- `getTop250TVShows(int page, ApiCallback<FilmCollection> callback)` - Топ 250 сериалов

### Поиск и фильтрация
- `searchFilms(String keyword, int page, ApiCallback<FilmCollection> callback)` - Поиск по ключевому слову
- `getFilmsByGenre(int genreId, int page, ApiCallback<FilmCollection> callback)` - Фильмы по жанру
- `getFilmsByCountry(int countryId, int page, ApiCallback<FilmCollection> callback)` - Фильмы по стране
- `getSimilarFilms(int kinopoiskId, ApiCallback<FilmCollection> callback)` - Похожие фильмы

### Детальная информация
- `getFilmDetails(int kinopoiskId, ApiCallback<FilmDetails> callback)` - Детальная информация о фильме
- `getFilmTrailers(int kinopoiskId, ApiCallback<List<FilmTrailer>> callback)` - Трейлеры фильма
- `getFilmImages(int kinopoiskId, String type, int page, ApiCallback<FilmImages> callback)` - Изображения фильма

## 🛠️ Утилиты и константы

### Константы жанров
```java
// Использование констант жанров
client.getFilmsByGenre(Constants.Genres.SCI_FI, 1, callback);
client.getFilmsByGenre(Constants.Genres.ACTION, 1, callback);
client.getFilmsByGenre(Constants.Genres.COMEDY, 1, callback);
```

### Константы стран
```java
// Использование констант стран
client.getFilmsByCountry(Constants.Countries.RUSSIA, 1, callback);
client.getFilmsByCountry(Constants.Countries.USA, 1, callback);
client.getFilmsByCountry(Constants.Countries.FRANCE, 1, callback);
```

### Типы изображений
```java
// Получение разных типов изображений
client.getFilmImages(435, Constants.ImageTypes.POSTER, 1, callback);
client.getFilmImages(435, Constants.ImageTypes.STILL, 1, callback);
client.getFilmImages(435, Constants.ImageTypes.SCREENSHOT, 1, callback);
```

## 🔒 Обработка ошибок

```java
client.getFilmDetails(435, new KinopoiskApiClient.ApiCallback<FilmDetails>() {
    @Override
    public void onSuccess(FilmDetails result) {
        // Успешный результат
    }
    
    @Override
    public void onError(ApiException error) {
        switch (error.getCode()) {
            case 401:
                System.err.println("Неверный API ключ");
                break;
            case 403:
                System.err.println("Доступ запрещен");
                break;
            case 404:
                System.err.println("Фильм не найден");
                break;
            case 429:
                System.err.println("Превышен лимит запросов");
                break;
            default:
                System.err.println("Ошибка API: " + error.getMessage());
        }
    }
});
```

## 🧹 Освобождение ресурсов

```java
// Важно закрыть клиент после использования
client.close();
```

## 📊 Сравнение с оригинальным API

| Аспект | Оригинальный API | Оптимизированный API |
|--------|------------------|---------------------|
| **Размер кода** | ~2200 строк | ~800 строк |
| **Модели данных** | Ручной парсинг JSON | Автоматическая сериализация |
| **Обработка ошибок** | Разрозненная | Централизованная |
| **Типизация** | Слабая | Строгая |
| **Производительность** | Средняя | Высокая |
| **Удобство использования** | Сложное | Простое |
| **Поддержка** | Ограниченная | Полная |

## 🎉 Заключение

Оптимизированный Kinopoisk API предоставляет:
- **Современную архитектуру** с использованием лучших практик
- **Высокую производительность** благодаря оптимизированным HTTP запросам
- **Простое использование** с интуитивно понятным API
- **Надежность** благодаря улучшенной обработке ошибок
- **Гибкость** для различных сценариев использования

Этот API готов к использованию в production среде и может быть легко интегрирован в существующие проекты.
