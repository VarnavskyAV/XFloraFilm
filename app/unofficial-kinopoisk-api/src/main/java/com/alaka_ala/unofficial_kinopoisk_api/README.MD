# Kinopoisk API Client V2 (KinopoiskV2)

## 🎬 Описание

Оптимизированный клиент для работы с [Kinopoisk API Unofficial](https://kinopoiskapiunofficial.tech/) в Android-приложении. Реализован как **Singleton** с встроенной системой кэширования через **Room Database V2**.

---

## 📁 Структура папки

```
kinopoiskV2/
├── api/
│   └── KinopoiskApiClientV2.java      # Основной клиент API (Singleton)
├── constants/
│   ├── Countries.java                  # Константы стран (ID)
│   ├── FilmCollectionType.java         # Типы коллекций фильмов (TOP_250 и др.)
│   ├── FilmTypes.java                  # Типы контента (Фильм, Сериал и др.)
│   ├── Genres.java                     # Константы жанров
│   ├── ImageType.java                  # Типы изображений для фильмов
│   └── ProductionStatus.java           # Статусы производства
├── db/
│   ├── Converters.java                 # Room конвертеры типовых данных
│   ├── FilmAwardsResponseDao.java      # DAO награды фильма
│   ├── FilmCollectionDao.java          # DAO коллекции фильмов
│   ├── FilmCountryOrGenresResponseDao.java  # DAO жанров и стран
│   ├── FilmDetailsDao.java             # DAO детальной информации о фильме
│   ├── FilmFactsResponseDao.java       # DAO фактов о фильме
│   ├── FilmImagesResponseDao.java      # DAO изображений фильма
│   ├── FilmSearchResponseDao.java      # DAO результатов поиска
│   ├── FilmSequelsAndPrequelsResponseDao.java  # DAO сиквелов/приквелов
│   ├── FilmSimilarResponseDao.java     # DAO похожих фильмов
│   ├── KinopoiskDatabaseV2.java        # Схема Room Database
│   ├── PersonSearchResponseDao.java    # DAO поиска личностей
│   └── StaffDao.java                   # DAO съемочного состава
└── models/
    ├── AwardItem.java                  # Награда/номинация
    ├── BaseModel.java                  # Базовая модель с timestamp'ами
    ├── Converters.java                 # Конвертеры типов (List, Map и т.д.)
    ├── Country.java                    # Страна фильма
    ├── CountryResponse.java            # Ответ API по странам
    ├── FactItem.java                   # Факт о фильме
    ├── FilmAwardsResponse.java         # Награды фильма
    ├── FilmCollection.java             # Коллекция фильмов (TOP_250 и др.)
    ├── FilmCountryOrGenresResponse.java   # Отфильтрованные фильмы
    ├── FilmDetails.java                # Детальная информация о фильме
    ├── FilmFactsResponse.java          # Факты о фильме
    ├── FilmImageItem.java              # Элемент изображения
    ├── FilmImagesResponse.java         # Изображения фильма
    ├── FilmItem.java                   # Фильм в коллекции
    ├── FilmSearchResponse.java         # Результаты поиска
    ├── FilmSequelOrPrequel.java        # Сиквел/приквел
    ├── FilmSequelsAndPrequelsResponse.java   # Сиквелы и приквелы
    ├── FilmSimilarItem.java            # Подобный фильм
    ├── FilmSimilarResponse.java        # Похожие фильмы
    ├── FilmTrailer.java                # Трейлер фильма
    ├── Genre.java                      # Жанр фильма
    ├── GenreResponse.java              # Ответ API по жанрам
    ├── Person.java                     # Лицо (актер, режиссер)
    ├── PersonSearchResponse.java       # Поиск личностей
    ├── SearchCountry.java              # Страна в поиске
    ├── SearchGenre.java                # Жанр в поиске
    ├── SearchResultFilm.java           # Фильм в результатах поиска
    ├── Staff.java                      # Участник съёмок
    └── StaffResponse.java              # Ответ API по участникам
```

---

## 🚀 Использование (Quick Start)

### 1. Инициализация

Вызовите один раз в `Application.onCreate()`:

```java
KinopoiskApiClientV2.initialize(this);
```

### 2. Получение клиента

```java
KinopoiskApiClientV2 client = KinopoiskApiClientV2.getInstance();
```

### 3. Примеры запросов

#### Поиск фильма по ключевому слову

```java
client.searchFilmByKeyword(
    "кинопоиск",      // keyword
    1,                 // page
    false,             // forceRefresh (использовать кэш)
    (result, error) -> {
        if (result != null) {
            // Получить список найденных фильмов
            List<SearchResultFilm> films = result.getFilms();
        }
    }
);
```

#### Получение подробной информации о фильме

```java
client.getFilmDetails(
    879542,                // kinopoiskId фильма
    false,                 // forceRefresh
    (details, error) -> {
        if (details != null) {
            String name = details.getNameRu();           // "Интерстеллар"
            double rating = details.getRatingKinopoisk(); // 8.5
            String year = details.getYear();             // "2014"
            List<Genre> genres = details.getGenres();    // [Фантастика, драма, мелодрама]
        }
    }
);
```

#### Получение актеров и съемочного состава

```java
client.getStaff(
    879542,               // kinopoiskId
    false,                // forceRefresh
    (staff, error) -> {
        if (staff != null) {
            for (Staff member : staff) {
                String name = member.getName();  // "Мэттью МакКонахи"
                String character = member.getCharacter(); // "Купер"
            }
        }
    }
);
```

#### Получение похожих фильмов

```java
client.getFilmSimilar(
    879542,               // kinopoiskId
    false,                // forceRefresh
    (similar, error) -> {
        if (similar != null) {
            List<FilmItem> similarFilms = similar.getFilms();
            for (FilmItem film : similarFilms) {
                String name = film.getNameRu();
                double rating = film.getRatingKinopoisk();
            }
        }
    }
);
```

#### Получение изображений фильма

```java
client.getFilmImages(
    879542,                 // kinopoiskId
    ImageType.POSTER,       // STILL | SHOOTING | POSTER | FAN_ART | PROMO | CONCEPT | WALLPAPER | COVER | SCREENSHOT
    1,                      // page
    false,                  // forceRefresh
    (images, error) -> {
        if (images != null) {
            List<FilmImageItem> items = images.getItems();
            for (FilmImageItem item : items) {
                String imageUrl = item.getMediumUrl();  // URL средней версии изображения
            }
        }
    }
);
```

#### Получение сиквелов и приквелов

```java
client.getFilmSequelsAndPrequels(
    879542,               // kinopoiskId
    false,                // forceRefresh
    (sequels, error) -> {
        if (sequels != null) {
            List<FilmSequelOrPrequel> list = sequels.getItems();
            for (FilmSequelOrPrequel sequel : list) {
                String name = sequel.getNameRu();       // Название сиквела/приквела
                int year = sequel.getYear();             // Год выпуска
                boolean isPrequel = sequel.isPrequel();  // true - если это приквел
            }
        }
    }
);
```

#### Получение фактов о фильме

```java
client.getFilmFacts(
    879542,               // kinopoiskId
    false,                // forceRefresh
    (facts, error) -> {
        if (facts != null) {
            List<FactItem> items = facts.getItems();
            for (FactItem item : items) {
                String factText = item.getFact();      // Текст факта
                String sourceUrl = item.getSourceUrl(); // Источник информации
            }
        }
    }
);
```

#### Поиск личностей по имени

```java
client.searchPersonByName(
    "mattthew mccaughey",  // имя (на английском)
    1,                     // page
    false,                 // forceRefresh
    (person, error) -> {
        if (person != null) {
            String name = person.getNameRu();         // "Мэттью МакКонахи"
            int kinopoiskId = person.getKinopoiskId(); // ID на Кинопоиске
        }
    }
);
```

#### Получение коллекции фильмов (TOP-250)

```java
client.getFilmCollection(
    FilmCollectionType.TOP_250_MOVIES,  // Тип коллекции
    1,                                  // page
    false,                              // forceRefresh
    (collection, error) -> {
        if (collection != null) {
            List<FilmItem> films = collection.getItems();
            for (FilmItem film : films) {
                String name = film.getNameRu();           // Название фильма
                double rating = film.getRatingKinopoisk(); // Рейтинг
            }
        }
    }
);
```

#### Отфильтрованные фильмы

```java
// По жанру
client.getFilmsByGenre("1", 1, false, (result, error) -> {
    // Получить список фильмов жанра
});

// По стране
client.getFilmsByCountry("34", 1, false, (result, error) → {
    // Получить список российских фильмов
});

// По году выпуска
client.getFilmsByYear("2024", 1, false, (result, error) → {
    // Получить фильмы 2024 года
});

// По рейтингу (8-9 баллов)
client.getFilmsByRating("8", "9", 1, false, (result, error) → {
    // Получить фильмы с рейтингом 8-9
});

// Полный фильтр
client.getFilmFromFilter(
    null,               // countryId (null = любые страны)
    "1",                // genreId (ID жанра)
    KinopoiskApiClientV2.FilmOrder.RATING,      // порядок сортировки
    KinopoiskApiClientV2.FilmType.FILM,         // тип контента
    "8",                // рейтинг от
    "10",               // рейтинг до
    null,               // yearFrom (null = любые годы)
    null,               // yearTo
    null,               // keyword
    1,                  // page
    false,              // forceRefresh
    (result, error) → {
        // Получить отфильтрованные фильмы
    }
);
```

#### Получение списка всех жанров и стран

```java
client.getGenreOrCountryList(
    (response, error) -> {
        if (response != null) {
            List<Genre> genres = response.getGenres();     // Все жанры
            List<SearchCountry> countries = response.getCountries(); // Все страны
        }
    }
);
```

---

## 📋 Доступные методы клиента

### Поиск и информация о фильмах
| Метод | Описание |
|-------|----------|
| `searchFilmByKeyword()` | Поиск фильмов по ключевому слову |
| `getFilmDetails()` | Детальная информация о фильме |
| `getStaff()` | Актеры и съемочный состав |
| `getFilmSimilar()` | Похожие фильмы |
| `getFilmSequelsAndPrequels()` | Сиквелы и приквелы |
| `getFilmFacts()` | Факты о фильме |

### Изображения и коллекции
| Метод | Описание |
|-------|----------|
| `getFilmImages()` | Изображения фильма (постеры, кадры и т.д.) |
| `getFilmCollection()` | Коллекции фильмов (TOP-250, TOP-100 и др.) |

### Фильтрация
| Метод | Описание |
|-------|----------|
| `getFilmsByGenre()` | Фильмы по жанру |
| `getFilmsByCountry()` | Фильмы по стране |
| `getFilmsByYear()` | Фильмы по году выпуска |
| `getFilmsByRating()` | Фильмы по диапазону рейтинга |
| `getFilmFromFilter()` | Универсальная фильтрация (все параметры) |

### Личности и поиск
| Метод | Описание |
|-------|----------|
| `searchPersonByName()` | Поиск личностей по имени |
| `getGenreOrCountryList()` | Получение всех жанров и стран |

---

## 🔧 Параметры методов

Все методы имеют следующие параметры:

- **kinopoiskId** - ID фильма на Кинопоиске (для запросов к конкретному фильму)
- **name** - Имя для поиска (на английском языке)
- **keyword** - Ключевое слово для поиска фильмов
- **page** - Номер страницы пагинации (API возвращает до 1000 результатов)
- **type** - Тип запроса (`ImageType`, `FilmCollectionType` и т.д.)
- **forceRefresh** - Если `true`, данные будут загружены из сети, игнорируя кэш

---

## 💾 Система кэширования

### Принцип работы

1. **Время жизни кэша**: Данные кэшируются на **30 дней** (`CACHE_DURATION_MS = 720 * 60 * 60 * 1000`)
2. **Кэш-идентификатор**: Каждый запрос имеет уникальный ID, формируемый из параметров
3. **Автосохранение**: После успешного получения данных они автоматически сохраняются в Room Database
4. **Политика перезагрузки**: 
   - При `forceRefresh = true` кэш игнорируется
   - При `forceRefresh = false` сначала проверяется актуальность кэша (время жизни < 30 дней)
5. **Ошибки сети**: При неудачном запросе возвращается последний сохраненный в БД результат

### Пример логики кэширования

```java
// Первый запрос (нет кэша)
searchFilmByKeyword("кино", 1, false, ...) → Запрос в сеть → Сохранение в БД

// Второй запрос через 5 минут
searchFilmByKeyword("кино", 1, false, ...) → Кэш < 30 дней → Возврат из кэша → Обновление timestamp

// Третий запрос после 31 дня
searchFilmByKeyword("кино", 1, false, ...) → Кэш > 30 дней (устарел) → Запрос в сеть → Сохранение в БД

// Четвертый запрос с forceRefresh=true
searchFilmByKeyword("кино", 1, true, ...) → Игнорирование кэша → Всегда свежий данные из сети
```

---

## 🗂️ Room Database Tables

### Основные таблицы

| Таблица | Описание | Ключевые поля |
|---------|----------|---------------|
| `film_details` | Детальная информация о фильме | `kinopoiskId` (PK) |
| `person_search_response` | Поиск личностей | `id` (PK) |
| `staff` | Съемочный состав фильмов | `id` (PK) |
| `film_collection` | Коллекции и фильтры фильмов | `id` (PK) |
| `film_country_or_genres_response` | Список всех жанров/стран | `genres_countries_id` (PK) |
| `film_facts_response` | Факты о фильмах | `id` (PK) |
| `film_images_response` | Изображения фильмов | `id` (PK) |
| `film_sequels_and_prequels_response` | Сиквелы и приквелы | `id` (PK) |
| `film_similar_response` | Похожие фильмы | `id` (PK) |
| `film_search_response` | Результаты поиска | `id` (PK) |
| `film_awards_response` | Награды фильма | `id` (PK) |
| `film_collection_dao` | Данные для коллекции/истории просмотра | `kinopoiskId`, `isView`, `positionView` |

---

## 🌍 Константы

### Страны (`Countries.java`)

```java
public static final int USA = 1;
public static final int RUSSIA = 34;
public static final int UK = 5;
// ... и более 100 стран с ID
```

### Типы коллекций (`FilmCollectionType.java`)

```java
TOP_POPULAR_ALL("TOP_POPULAR_ALL")
TOP_250_MOVIES("TOP_250_MOVIES")
TOP_250_TV_SHOWS("TOP_250_TV_SHOWS")
VAMPIRE_THEME("VAMPIRE_THEME")
// ... и другие тематические коллекции
```

### Типы контента (`FilmTypes.java`)

```java
ALL, FILM, TV_SHOW, VIDEO, MINI_SERIES, TV_SERIES
```

### Типы изображений (`ImageType.java`)

```java
STILL, SHOOTING, POSTER, FAN_ART, PROMO, 
CONCEPT, WALLPAPER, COVER, SCREENSHOT
```

### Статусы производства (`ProductionStatus.java`)

```java
UNKNOWN, IN_PROGRESS, ANNOUNCEMENT, COMPLETED, 
POST_PRODUCTION, PRE_PRODUCTION
```

---

## 📦 Зависимости

Проект использует следующие внешние библиотеки:

- **OkHttp** - HTTP клиент для запросов к API
- **Gson** - JSON сериализация/десериализация
- **Room** - Room Database для локального кэширования данных

---

## ⚠️ Важные замечания

### 1. Инициализация Singleton
```java
// Обязательный вызов в Application.onCreate()
KinopoiskApiClientV2.initialize(this);
```

### 2. Работа с API ключом
В коде используется встроенный API ключ. Если нужно изменить — отредактируйте `getInternalApiKey()` в клиенте.

### 3. Пагинация
API возвращает до **1000 результатов** за один запрос. Для пагинации используйте параметр `page`.

### 4. Ошибки сети
Все методы обрабатывают ошибки и при сбое возвращают последний сохраненный результат из кэша или вызов `onError()`.

### 5. Асинхронность
Все запросы выполняются асинхронно. Используются колбэки для получения результата:
```java
callback.onSuccess(result);    // Успешный ответ
callback.onError(error);       // Ошибка сети или парсинга
```

---

## 🐛 Обработка ошибок

```java
KinopoiskApiClientV2.ApiException error;
if (error != null) {
    int code = error.getCode();      // HTTP код ошибки
    String message = error.getMessage(); // Описание ошибки
}
```

Типичные ошибки:
- `401` - Неверный API ключ
- `404` - Ресурс не найден
- `5xx` - Ошибка сервера

---

## 🧹 Очистка ресурсов

При завершении работы приложения рекомендуется вызвать:

```java
KinopoiskApiClientV2.getInstance().close();
```

Этот метод освобождает ресурсы OkHttpClient.

---

## 📚 API Документация

Полная документация доступна по адресу:  
🔗 [https://kinopoiskapiunofficial.tech/](https://kinopoiskapiunofficial.tech/)

---

## 🏷️ License

Используйте этот код в соответствии с условиями лицензии проекта.
