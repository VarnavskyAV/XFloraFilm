# CodeJava

Подробная документация Java-переноса логики Alloha-parser(Kotlin) из библиотеки (Ernous (Ernela)).<br>
(https://github.com/Ernous/alloha-parser-kotlin)

---
## Быстрый старт

````Java
AllohaApiClient allohaApiClient = new AllohaApiClient();
        AllohaParserJava parser = new AllohaParserJava(getApplicationContext());
        new Thread(() -> {
            try {
                AllohaModels.AllohaApiResult resp = allohaApiClient.fetch("4cd98e08f1e1f0273692e35b16b690", "666");

                parser.parse(resp.movieIframe, new AllohaParserJava.Callback() {
                    @Override
                    public void onHlsLinksReceived(String json, Map<String, String> extraHeaders) {
                        Log.i("MainActivity", json);
                        for(Map.Entry headerEntry : extraHeaders.entrySet()) {
                            if(headerEntry != null){
                                Log.v("MainActivity" + ".onHLSLinkRecieved:",headerEntry.toString());
                            }
                        }
                    }

                    @Override
                    public void onConfigUpdate(String edgeHash, int ttlSeconds, Map<String, String> extraHeaders) {
                        Log.wtf("MainActivity","edgehash:"+edgeHash+" ,ttlseconds"+String.valueOf(ttlSeconds));
                    }

                    @Override
                    public void onM3u8Refreshed(String url, Map<String, String> extraHeaders) {
                        HlsProxyServerJava hlsProxyServerJava = new HlsProxyServerJava(extraHeaders, () -> {

                        });
                        hlsProxyServerJava.updateMasterUrl(url);
                        try {
                            hlsProxyServerJava.start();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        initPlayer(hlsProxyServerJava.getFixedMasterUrl());
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("MainActivity",error);
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
````


---

## Что находится в папке

- `AllohaParserJava.java`  
  WebView-парсер: загружает iframe-плеер, перехватывает JS/XHR/fetch/WebSocket события, собирает рабочие заголовки и ссылки на HLS.

- `HlsProxyServerJava.java`  
  Локальный HTTP proxy для HLS (`http://127.0.0.1:8080`), который:
  - отдает `master.m3u8`,
  - переписывает все URI внутри плейлистов на локальный proxy,
  - проксирует и кеширует сегменты,
  - делает prefetch и recovery после 403.

- `AllohaHttpTraceJava.java`  
  Трассировка в формате JSONL (`alloha_http_trace.jsonl`): записывает сетевые round-trip и JS-заголовки для отладки.

- `AllohaApiClient.java`  
  Клиент к `https://api.alloha.tv`, который получает мета-данные (фильм/сериал, сезоны, эпизоды, переводы, iframe).

- `AllohaModels.java`  
  DTO/модели для результата API.

---

## Общий жизненный цикл (как все работает вместе)

1. `AllohaApiClient.fetch(token, kpId)` возвращает структуру контента и `iframeUrl`.
2. `AllohaParserJava.parse(iframeUrl, callback)` поднимает WebView и ждет:
  - `onReady(...)` (bnsi + базовые stream headers),
  - `onConfigUpdate(...)` (обновление CDN-токенов),
  - `onM3u8Refreshed(...)` (обновление master.m3u8 URL).
3. Эти заголовки передаются в `HlsProxyServerJava` через общую `activeHeaders` map.
4. Плеер играет не прямой CDN URL, а локальный `fixedMasterUrl` proxy-сервера.
5. Proxy подставляет актуальные заголовки в запросы к CDN, кеширует сегменты и пытается восстановиться при 403.
6. `AllohaHttpTraceJava` фиксирует, что реально улетело в сеть и что пришло из JS.

---

## Что такое `ttlSeconds`

`ttlSeconds` приходит в callback `onConfigUpdate(edgeHash, ttlSeconds, headers)` и означает:

- **TTL (time-to-live) в секундах** для текущего CDN/edge токена (`edge_hash` / `accepts-controls`).
- По истечении TTL токены становятся устаревшими и CDN чаще отвечает 403.
- Поэтому TTL используют для:
  - планового (проактивного) обновления сессии до истечения,
  - понимания, когда пора перезапрашивать/перезапускать поток.

Проще: `ttlSeconds` = "сколько еще живы текущие auth-параметры стрима".

---

## Документация по классам и методам

## `AllohaApiClient`

### `AllohaApiResult fetch(String token, String kpId)`
- Назначение: получить и распарсить ответ API Alloha.
- Параметры:
  - `token` — API token Alloha.
  - `kpId` — ID контента (Kinopoisk ID).
- Возвращает:
  - `AllohaApiResult` с `isSerial`, `movieIframe`, `seasons/...`.
- Исключения:
  - пробрасывает `Exception`, если сеть/JSON сломан.

### `String requestUnsafe(String url)` (private)
- Назначение: выполнить HTTPS GET с отключенной валидацией сертификата/hostname.
- Используется только для совместимости с оригинальной логикой.

---

## `AllohaModels`

### `TranslationInfo`
- `id` — ID перевода.
- `name` — название озвучки/перевода.
- `iframeUrl` — iframe URL для этого перевода.

### `EpisodeInfo`
- `num` — номер эпизода строкой.
- `translations` — список доступных переводов.

### `SeasonInfo`
- `num` — номер сезона строкой.
- `episodes` — список эпизодов.

### `AllohaApiResult`
- `title` — название контента.
- `isSerial` — сериал или фильм.
- `movieIframe` — iframe для фильма (если это не сериал).
- `seasons` — сезоны/эпизоды/переводы (если сериал).

---

## `AllohaParserJava`

### `AllohaParserJava(Context context)`
- Инициализирует `WebView`:
  - JS + DOM storage,
  - отключение user-gesture ограничения на media,
  - cookie + third-party cookie,
  - WebChromeClient + SSL proceed.

### `void rotateUserAgent()`
- Переключает текущий user-agent на следующий в списке.
- Нужен для анти-403/антибот поведенческой ротации.

### `void parse(String iframeUrl, Callback callback)`
- Главный старт парсинга сессии.
- Параметры:
  - `iframeUrl` — URL плеерного iframe.
  - `callback` — обработчики событий сессии.
- Что делает:
  - создает JS bridge `AndroidBridge`,
  - внедряет HTML wrapper c iframe и перехватчиками,
  - слушает XHR/fetch/WebSocket внутри iframe,
  - передает в Android:
    - `onReady(jsonResponse, headersJson)`,
    - `onConfigUpdate(edgeHash, ttl, headersJson)`,
    - `onM3u8Refreshed(url, headersJson)`,
    - `onStreamHeaders(headersJson)`.

### `void release()`
- Уничтожает `WebView`.

### `Callback` (interface)
- `onHlsLinksReceived(String json, Map<String,String> extraHeaders)`  
  Первичная готовность: получены `hlsSource` и начальные headers.
- `onConfigUpdate(String edgeHash, int ttlSeconds, Map<String,String> extraHeaders)`  
  Обновление токенов/хеша edge. `ttlSeconds` — срок действия.
- `onM3u8Refreshed(String url, Map<String,String> extraHeaders)`  
  Новый master.m3u8 (например после CDN failover).
- `onStreamHeadersUpdated(Map<String,String> extraHeaders)`  
  Пуш текущих stream headers в процессе.
- `onError(String error)`  
  Ошибка парсинга/загрузки.

---

## `HlsProxyServerJava`

### `HlsProxyServerJava(Map<String,String> activeHeaders, Runnable onSessionExpired)`
- Параметры:
  - `activeHeaders` — живая map текущих stream headers (origin/referer/user-agent/authorizations/accepts-controls/...).
  - `onSessionExpired` — callback, когда recovery не удался и нужна полная перезагрузка сессии.

### `void start()`
- Поднимает локальный сервер на `8080`.

### `void stop()`
- Останавливает server socket и executors.

### `void updateMasterUrl(String url)`
- Обновляет активный master URL.
- Сбрасывает кеш/соединения и увеличивает версию сессии.
- Нужен при `m3u8 refresh` и при перезапусках сессии.

### `String getFixedMasterUrl()`
- Возвращает стабильный URL для плеера: `http://127.0.0.1:8080/master.m3u8`.
- Плеер всегда играет его; внутри proxy уже прокидывает актуальный CDN URL.

### `String proxyUrl(String originalUrl)`
- Кодирует реальный URL в `/proxy?url=...`.

### `handleConnection(...)` (private)
- Парсит входящий HTTP запрос:
  - `/master.m3u8` -> `servePlaylist(activeMasterUrl)`,
  - `/proxy?url=...` -> playlist или segment.

### `servePlaylist(String url, OutputStream out)` (private)
- Загружает playlist с CDN (`fetchText`),
- переписывает URI (`rewriteM3u8`),
- отдает клиенту переписанный m3u8.

### `serveSegment(String url, OutputStream out)` (private)
- Отдает segment из кеша или сети (`fetchBytes`),
- при фейле запускает recovery через `fetchSegmentFromFreshPlaylist`.

### `rewriteM3u8(String content, String baseUrl)` (private)
- Переписывает:
  - обычные строки-URL,
  - `URI="..."` в тегах (аудио/субтитры/keys),
    чтобы все шло через локальный proxy.

### `byte[] getOrFetch(String url)` (private)
- Дедупликация одновременных запросов к одному сегменту через `inFlight`.

### `byte[] fetchSegmentFromFreshPlaylist(String failedUrl)` (private)
- Recovery-логика:
  - ждет новую версию сессии,
  - берет свежий master/variant,
  - ищет сегмент с тем же именем,
  - пробует скачать заново.
- При окончательной неудаче вызывает `onSessionExpired`.

### `String fetchText(String url)` / `byte[] fetchBytes(String url)` (private)
- Сетевые методы OkHttp.
- При 403 делают evict connection pool и retry (для bytes).

### `Request buildRequest(String url)` (private)
- Собирает запрос к CDN из `activeHeaders`:
  - `User-Agent`, `Origin`, `Referer`, `Accept`,
  - `accepts-controls`, `authorizations`,
  - `sec-fetch-*`, `accept-language`,
  - `Cookie` из WebView `CookieManager`.

---

## `AllohaHttpTraceJava`

### `File traceFile(Context context)`
- Путь к trace-файлу (`files/alloha_http_trace.jsonl`).

### `void reset(Context context)`
- Сбрасывает sequence и пишет `session_start` в trace.

### `void logOkHttpRoundTrip(Context context, Request request, Response response, String error)`
- Логирует один HTTP round-trip:
  - URL, метод, request headers, status/response headers или error.

### `void logJsHeaders(Context context, String source, Map<String,String> headers)`
- Логирует snapshot заголовков, собранных из JS.
- `source` обычно:
  - `onReady`,
  - `config_update`,
  - `m3u8_refresh`,
  - `stream_push`.
- Для `stream_push` дубликаты фильтруются по паре `authorizations + accepts-controls`.

---

## Важные параметры (быстрый справочник)

- `token` — ключ API Alloha.
- `kpId` — идентификатор фильма/сериала.
- `iframeUrl` — URL player iframe.
- `edgeHash` — актуальный edge/CDN hash (обычно идет в `accepts-controls`).
- `ttlSeconds` — срок жизни `edgeHash` и связанных stream-токенов.
- `extraHeaders` — критичные runtime headers для CDN-доступа:
  - `authorizations`,
  - `accepts-controls`,
  - `origin`,
  - `referer`,
  - `user-agent`,
  - иногда `sec-fetch-*`, `accept-language`, `cookie`.
- `activeMasterUrl` — текущий реальный master.m3u8, который proxy раздает через `fixedMasterUrl`.

---

## Что важно помнить

- Kotlin-исходники проекта не модифицировались.
- Папка `CodeJava` содержит отдельную Java-реализацию логики.
- Для интеграции в приложение нужно подключить эти Java-классы из Activity/Service/Player слоя.
