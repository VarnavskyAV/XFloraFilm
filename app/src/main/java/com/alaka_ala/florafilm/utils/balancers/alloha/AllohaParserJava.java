package com.alaka_ala.florafilm.utils.balancers.alloha;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public final class AllohaParserJava {
    private static final String TAG = "AllohaParserJava";
    private final List<String> userAgents = new ArrayList<>();
    private final Random random = new Random();
    private int uaIndex = 0;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    public volatile String lastIframeUrl = "";
    public final WebView webView;

    public interface Callback {
        void onHlsLinksReceived(String json, Map<String, String> extraHeaders);

        void onConfigUpdate(String edgeHash, int ttlSeconds, Map<String, String> extraHeaders);

        void onM3u8Refreshed(String url, Map<String, String> extraHeaders);

        default void onStreamHeadersUpdated(Map<String, String> extraHeaders) {
        }

        void onError(String error);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public AllohaParserJava(Context context) {
        generateUserAgents();
        uaIndex = random.nextInt(userAgents.size());
        webView = new WebView(context);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.getSettings().setUserAgentString(getUserAgent());
        webView.setKeepScreenOn(true);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @SuppressLint("WebViewClientOnReceivedSslError")
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public Bitmap getDefaultVideoPoster() {
                Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                bitmap.eraseColor(Color.TRANSPARENT);
                return bitmap;
            }
        });
    }

    public void rotateUserAgent() {
        uaIndex = (uaIndex + 1) % userAgents.size();
        webView.getSettings().setUserAgentString(getUserAgent());
        Log.d(TAG, "UA rotated to: " + getUserAgent());
    }

    @SuppressLint("AddJavascriptInterface")
    public void parse(String iframeUrl, Callback callback) {
        lastIframeUrl = iframeUrl;
        mainHandler.post(() -> {
            webView.onResume();
            webView.resumeTimers();
            webView.removeJavascriptInterface("AndroidBridge");
            webView.addJavascriptInterface(new Object() {
                private boolean parsed;

                @JavascriptInterface
                public void onReady(String jsonResponse, String headersJson) {
                    if (parsed) {
                        return;
                    }
                    parsed = true;
                    Map<String, String> headers = parseHeaders(headersJson);
                    mainHandler.post(() -> {
                        CookieManager.getInstance().flush();
                        callback.onHlsLinksReceived(jsonResponse, headers);
                    });
                }

                @JavascriptInterface
                public void onConfigUpdate(String edgeHash, int ttl, String headersJson) {
                    if (!parsed) {
                        return;
                    }
                    Map<String, String> headers = parseHeaders(headersJson);
                    mainHandler.post(() -> callback.onConfigUpdate(edgeHash, ttl, headers));
                }

                @JavascriptInterface
                public void onM3u8Refreshed(String url, String headersJson) {
                    if (!parsed) {
                        return;
                    }
                    Map<String, String> headers = parseHeaders(headersJson);
                    mainHandler.post(() -> callback.onM3u8Refreshed(url, headers));
                }

                @JavascriptInterface
                public void onStreamHeaders(String headersJson) {
                    if (!parsed) {
                        return;
                    }
                    mainHandler.post(() -> {
                        CookieManager.getInstance().flush();
                    });
                    Map<String, String> headers = parseHeaders(headersJson);
                    mainHandler.post(() -> callback.onStreamHeadersUpdated(headers));
                }

                @JavascriptInterface
                public void onLog(String msg) {
                    Log.d("AllohaParserJS", msg);
                }

                private Map<String, String> parseHeaders(String json) {
                    Map<String, String> map = new HashMap<>();
                    try {
                        JSONObject obj = new JSONObject(json);
                        for (java.util.Iterator<String> it = obj.keys(); it.hasNext(); ) {
                            String key = it.next();
                            map.put(key, obj.optString(key));
                        }
                    } catch (Exception ignored) {
                    }
                    return map;
                }
            }, "AndroidBridge");

            try {
                URL parsed = new URL(iframeUrl);
                String baseUrl = parsed.getProtocol() + "://" + parsed.getHost().toLowerCase(Locale.ROOT) + "/";
                webView.loadDataWithBaseURL(baseUrl, buildWrapperHtml(iframeUrl), "text/html", "UTF-8", null);
            } catch (Exception e) {
                callback.onError(e.getMessage() == null ? "parse failed" : e.getMessage());
            }
        });
    }

    public void release() {
        mainHandler.post(webView::destroy);
    }

    private String getUserAgent() {
        return userAgents.get(uaIndex);
    }

    private void generateUserAgents() {
        String[] osList = new String[]{
                "Windows NT 10.0; Win64; x64",
                "Windows NT 11.0; Win64; x64",
                "Macintosh; Intel Mac OS X 10_15_7",
                "Macintosh; Intel Mac OS X 14_4_1",
                "X11; Linux x86_64",
                "X11; Ubuntu; Linux x86_64"
        };
        for (int i = 0; i < 20; i++) {
            String os = osList[random.nextInt(osList.length)];
            int chrome = 130 + random.nextInt(6);
            int firefox = 130 + random.nextInt(7);
            int mode = random.nextInt(3);
            if (mode == 0) {
                userAgents.add("Mozilla/5.0 (" + os + ") AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + chrome + ".0.0.0 Safari/537.36");
            } else if (mode == 1) {
                userAgents.add("Mozilla/5.0 (" + os + "; rv:" + firefox + ".0) Gecko/20100101 Firefox/" + firefox + ".0");
            } else {
                userAgents.add("Mozilla/5.0 (" + os + ") AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + chrome + ".0.0.0 Safari/537.36 Edg/" + chrome + ".0.0.0");
            }
        }
    }

    private String buildWrapperHtml(String iframeUrl) {
        return "<html>\n" +
                "  <body style=\"margin:0;padding:0;background:black;\">\n" +
                "    <iframe\n" +
                "      id=\"alloha_iframe\"\n" +
                "      src=\"" + iframeUrl + "\"\n" +
                "      width=\"100%\"\n" +
                "      height=\"100%\"\n" +
                "      frameborder=\"0\"\n" +
                "      allowfullscreen\n" +
                "    ></iframe>\n" +
                "\n" +
                "    <script>\n" +
                "      (function () {\n" +
                "        try {\n" +
                "          Object.defineProperty(document, \"visibilityState\", { get: () => \"visible\" });\n" +
                "          Object.defineProperty(document, \"hidden\", { get: () => false });\n" +
                "        } catch (e) {}\n" +
                "\n" +
                "        var iframe = document.getElementById(\"alloha_iframe\");\n" +
                "        if (!iframe) return;\n" +
                "\n" +
                "        iframe.onload = function () {\n" +
                "          try {\n" +
                "            var iframeWin = iframe.contentWindow;\n" +
                "\n" +
                "            try {\n" +
                "              Object.defineProperty(iframeWin.document, \"visibilityState\", { get: () => \"visible\" });\n" +
                "              Object.defineProperty(iframeWin.document, \"hidden\", { get: () => false });\n" +
                "            } catch (e) {}\n" +
                "\n" +
                "            var bnsiData = null;\n" +
                "            var capturedHeaders = {};\n" +
                "            var isDone = false;\n" +
                "            var lastM3u8Url = null;\n" +
                "\n" +
                "            var _pushHdrTimer = null;\n" +
                "            function schedulePushStreamHeaders() {\n" +
                "              if (!isDone) return;\n" +
                "              if (_pushHdrTimer) clearTimeout(_pushHdrTimer);\n" +
                "              _pushHdrTimer = setTimeout(function () {\n" +
                "                _pushHdrTimer = null;\n" +
                "                try {\n" +
                "                  AndroidBridge.onStreamHeaders(JSON.stringify(capturedHeaders));\n" +
                "                } catch (e) {}\n" +
                "              }, 40);\n" +
                "            }\n" +
                "\n" +
                "            function putHeader(name, value) {\n" +
                "              if (!name || !value) return;\n" +
                "              capturedHeaders[String(name).toLowerCase()] = String(value);\n" +
                "              schedulePushStreamHeaders();\n" +
                "            }\n" +
                "\n" +
                "            function checkDone() {\n" +
                "              if (isDone) return;\n" +
                "              var hasAuth = false,\n" +
                "                hasAccept = false;\n" +
                "              for (var k in capturedHeaders) {\n" +
                "                if (k === \"authorizations\") hasAuth = true;\n" +
                "                if (k === \"accepts-controls\") hasAccept = true;\n" +
                "              }\n" +
                "              if (bnsiData && hasAuth && hasAccept) {\n" +
                "                isDone = true;\n" +
                "                AndroidBridge.onReady(bnsiData, JSON.stringify(capturedHeaders));\n" +
                "              }\n" +
                "            }\n" +
                "\n" +
                "            putHeader(\"origin\", iframeWin.location.origin);\n" +
                "            putHeader(\"referer\", iframeWin.location.origin + \"/\");\n" +
                "            putHeader(\"user-agent\", iframeWin.navigator.userAgent);\n" +
                "            putHeader(\"accept\", \"*/*\");\n" +
                "            putHeader(\"sec-fetch-dest\", \"empty\");\n" +
                "            putHeader(\"sec-fetch-mode\", \"cors\");\n" +
                "            putHeader(\"sec-fetch-site\", \"cross-site\");\n" +
                "\n" +
                "            // ---------------- XHR intercept ----------------\n" +
                "            var originalOpen = iframeWin.XMLHttpRequest.prototype.open;\n" +
                "            iframeWin.XMLHttpRequest.prototype.open = function (method, url) {\n" +
                "              this._allohaUrl = url;\n" +
                "              this.addEventListener(\"load\", function () {\n" +
                "                var rUrl = this.responseURL || \"\";\n" +
                "\n" +
                "                if (rUrl.indexOf(\"/bnsi/\") !== -1 && !isDone) {\n" +
                "                  bnsiData = this.responseText;\n" +
                "                  checkDone();\n" +
                "                }\n" +
                "\n" +
                "                if (isDone && rUrl.indexOf(\"master.m3u8\") !== -1 && rUrl !== lastM3u8Url) {\n" +
                "                  lastM3u8Url = rUrl;\n" +
                "                  try {\n" +
                "                    AndroidBridge.onM3u8Refreshed(rUrl, JSON.stringify(capturedHeaders));\n" +
                "                  } catch (e) {\n" +
                "                    AndroidBridge.onLog(\"onM3u8Refreshed error: \" + e);\n" +
                "                  }\n" +
                "                }\n" +
                "              });\n" +
                "              return originalOpen.apply(this, arguments);\n" +
                "            };\n" +
                "\n" +
                "            var originalSetHeader = iframeWin.XMLHttpRequest.prototype.setRequestHeader;\n" +
                "            iframeWin.XMLHttpRequest.prototype.setRequestHeader = function (name, value) {\n" +
                "              putHeader(name, value);\n" +
                "              var url = this._allohaUrl || \"\";\n" +
                "              if (url.indexOf(\".m3u8\") !== -1 || url.indexOf(\".ts\") !== -1) {\n" +
                "                checkDone();\n" +
                "              }\n" +
                "              return originalSetHeader.apply(this, arguments);\n" +
                "            };\n" +
                "\n" +
                "            // ---------------- Fetch intercept + fallback ----------------\n" +
                "            var _fallbackHost = null;\n" +
                "            var _primaryHost = null;\n" +
                "            var _fallbackMasterUrl = null;\n" +
                "\n" +
                "            function extractFallbackHost() {\n" +
                "              if (_fallbackHost || !bnsiData) return;\n" +
                "              try {\n" +
                "                var d = JSON.parse(bnsiData);\n" +
                "                var src = d.hlsSource;\n" +
                "                if (src && src[0] && src[0].quality) {\n" +
                "                  var q = src[0].quality;\n" +
                "                  var key = Object.keys(q)[0];\n" +
                "                  var urls = String(q[key] || \"\").split(\" or \");\n" +
                "                  if (urls.length > 1) {\n" +
                "                    var m = urls[0].match(/https?:\\/\\/([^/]+)/);\n" +
                "                    if (m) _primaryHost = m[1];\n" +
                "\n" +
                "                    var fb = urls[1].trim();\n" +
                "                    var m2 = fb.match(/https?:\\/\\/([^/]+)/);\n" +
                "                    if (m2) {\n" +
                "                      _fallbackHost = m2[1];\n" +
                "                      _fallbackMasterUrl = fb;\n" +
                "                    }\n" +
                "\n" +
                "                    AndroidBridge.onLog(\n" +
                "                      \"CDN hosts: primary=\" + _primaryHost + \" fallback=\" + _fallbackHost\n" +
                "                    );\n" +
                "                  }\n" +
                "                }\n" +
                "              } catch (e) {\n" +
                "                AndroidBridge.onLog(\"extractFallback err: \" + e);\n" +
                "              }\n" +
                "            }\n" +
                "\n" +
                "            var originalFetch = iframeWin.fetch;\n" +
                "            iframeWin.fetch = function (input, init) {\n" +
                "              try {\n" +
                "                var url =\n" +
                "                  typeof input === \"string\" ? input : input && input.url ? input.url : \"\";\n" +
                "\n" +
                "                if (init && init.headers) {\n" +
                "                  if (typeof init.headers.forEach === \"function\") {\n" +
                "                    init.headers.forEach(function (v, k) {\n" +
                "                      putHeader(k, v);\n" +
                "                    });\n" +
                "                  } else {\n" +
                "                    for (var hk in init.headers) {\n" +
                "                      putHeader(hk, init.headers[hk]);\n" +
                "                    }\n" +
                "                  }\n" +
                "                }\n" +
                "\n" +
                "                if (url && (url.indexOf(\".m3u8\") !== -1 || url.indexOf(\".ts\") !== -1)) {\n" +
                "                  checkDone();\n" +
                "                  extractFallbackHost();\n" +
                "\n" +
                "                  if (_primaryHost && _fallbackHost && url.indexOf(_primaryHost) !== -1) {\n" +
                "                    var fallbackUrl;\n" +
                "                    if (url.indexOf(\"master.m3u8\") !== -1 && _fallbackMasterUrl) {\n" +
                "                      fallbackUrl = _fallbackMasterUrl;\n" +
                "                    } else {\n" +
                "                      fallbackUrl = url.replace(_primaryHost, _fallbackHost);\n" +
                "                    }\n" +
                "\n" +
                "                    var self = this;\n" +
                "                    return originalFetch.apply(self, [input, init]).then(function (resp) {\n" +
                "                      if (resp.status === 500 || resp.status === 503 || resp.status === 403) {\n" +
                "                        AndroidBridge.onLog(\n" +
                "                          \"fetch \" +\n" +
                "                            resp.status +\n" +
                "                            \" on primary, retrying fallback: \" +\n" +
                "                            fallbackUrl\n" +
                "                        );\n" +
                "                        return originalFetch.apply(iframeWin, [fallbackUrl, init]);\n" +
                "                      }\n" +
                "                      return resp;\n" +
                "                    });\n" +
                "                  }\n" +
                "                }\n" +
                "              } catch (e) {\n" +
                "                AndroidBridge.onLog(\"fetch intercept err: \" + e);\n" +
                "              }\n" +
                "              return originalFetch.apply(this, arguments);\n" +
                "            };\n" +
                "\n" +
                "            // ---------------- WebSocket patch + heartbeat ----------------\n" +
                "            var _origSend = iframeWin.WebSocket.prototype.send;\n" +
                "            var _allohaWs = null;\n" +
                "            var _heartbeatTimer = null;\n" +
                "            var _sessionStart = Date.now();\n" +
                "            var _lastEdgeHash = null;\n" +
                "\n" +
                "            function startHeartbeat(ws) {\n" +
                "              if (_heartbeatTimer) clearInterval(_heartbeatTimer);\n" +
                "              _heartbeatTimer = setInterval(function () {\n" +
                "                if (!isDone) return;\n" +
                "                if (!ws || ws.readyState !== 1) return; // OPEN\n" +
                "\n" +
                "                var t = Math.floor((Date.now() - _sessionStart) / 1000);\n" +
                "                try {\n" +
                "                  _origSend.call(\n" +
                "                    ws,\n" +
                "                    JSON.stringify({\n" +
                "                      type: \"playing\",\n" +
                "                      current_time: t,\n" +
                "                      resolution: \"1080\",\n" +
                "                      track_id: \"1\",\n" +
                "                      speed: 1,\n" +
                "                      subtitle: 0,\n" +
                "                      ts: Date.now(),\n" +
                "                    })\n" +
                "                  );\n" +
                "                  AndroidBridge.onLog(\"Heartbeat sent t=\" + t);\n" +
                "                } catch (e) {\n" +
                "                  AndroidBridge.onLog(\"Heartbeat err: \" + e);\n" +
                "                }\n" +
                "              }, 25000);\n" +
                "            }\n" +
                "\n" +
                "            iframeWin.WebSocket.prototype.send = function (data) {\n" +
                "              if (!this.__alloha_hooked) {\n" +
                "                this.__alloha_hooked = true;\n" +
                "                var ws = this;\n" +
                "                _allohaWs = ws;\n" +
                "                _sessionStart = Date.now();\n" +
                "\n" +
                "                AndroidBridge.onLog(\"WSS hooked via send()\");\n" +
                "\n" +
                "                ws.addEventListener(\"message\", function (event) {\n" +
                "                  try {\n" +
                "                    var msg = JSON.parse(event.data);\n" +
                "                    if (msg && msg.type === \"config_update\" && msg.edge_hash) {\n" +
                "                      if (msg.edge_hash !== _lastEdgeHash) {\n" +
                "                        _lastEdgeHash = msg.edge_hash;\n" +
                "                        var ttl = msg.ttl || 120;\n" +
                "                        capturedHeaders[\"accepts-controls\"] = msg.edge_hash;\n" +
                "                        AndroidBridge.onConfigUpdate(\n" +
                "                          msg.edge_hash,\n" +
                "                          ttl,\n" +
                "                          JSON.stringify(capturedHeaders)\n" +
                "                        );\n" +
                "                      }\n" +
                "                    }\n" +
                "                  } catch (e) {}\n" +
                "                });\n" +
                "\n" +
                "                ws.addEventListener(\"close\", function (e) {\n" +
                "                  AndroidBridge.onLog(\n" +
                "                    \"WSS closed code=\" + (e.code || \"?\") + \" reason=\" + (e.reason || \"\")\n" +
                "                  );\n" +
                "                  if (_allohaWs === ws) {\n" +
                "                    _allohaWs = null;\n" +
                "                    if (_heartbeatTimer) clearInterval(_heartbeatTimer);\n" +
                "                  }\n" +
                "                });\n" +
                "\n" +
                "                startHeartbeat(ws);\n" +
                "              }\n" +
                "              return _origSend.call(this, data);\n" +
                "            };\n" +
                "\n" +
                "            // Backup override for new sockets created later\n" +
                "            var OrigWS = iframeWin.WebSocket;\n" +
                "            iframeWin.WebSocket = function (url, protocols) {\n" +
                "              var ws = protocols ? new OrigWS(url, protocols) : new OrigWS(url);\n" +
                "              ws.addEventListener(\"open\", function () {\n" +
                "                AndroidBridge.onLog(\"WSS opened\");\n" +
                "              });\n" +
                "              return ws;\n" +
                "            };\n" +
                "            iframeWin.WebSocket.prototype = OrigWS.prototype;\n" +
                "            iframeWin.WebSocket.CONNECTING = OrigWS.CONNECTING;\n" +
                "            iframeWin.WebSocket.OPEN = OrigWS.OPEN;\n" +
                "            iframeWin.WebSocket.CLOSING = OrigWS.CLOSING;\n" +
                "            iframeWin.WebSocket.CLOSED = OrigWS.CLOSED;\n" +
                "\n" +
                "            // ---------------- Keep iframe alive ----------------\n" +
                "            setInterval(function () {\n" +
                "              if (!isDone) {\n" +
                "                var playBtn = iframeWin.document.querySelector(\".allplay__play-btn\");\n" +
                "                if (playBtn) playBtn.click();\n" +
                "\n" +
                "                var video = iframeWin.document.querySelector(\"video\");\n" +
                "                if (video) {\n" +
                "                  video.muted = true;\n" +
                "                  if (video.paused) {\n" +
                "                    video.play().catch(function () {});\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            }, 1500);\n" +
                "          } catch (e) {\n" +
                "            try {\n" +
                "              AndroidBridge.onLog(\"JS Error: \" + e);\n" +
                "            } catch (_) {}\n" +
                "          }\n" +
                "        };\n" +
                "      })();\n" +
                "    </script>\n" +
                "  </body>\n" +
                "</html>";
    }
}
