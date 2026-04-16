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
        return "<html><body style=\"margin:0;padding:0;background:black;\">"
                + "<iframe id=\"alloha_iframe\" src=\"" + iframeUrl + "\" width=\"100%\" height=\"100%\" frameborder=\"0\" allowfullscreen></iframe>"
                + "<script>"
                + "try{Object.defineProperty(document,'visibilityState',{get:()=> 'visible'});Object.defineProperty(document,'hidden',{get:()=> false});}catch(e){}"
                + "var iframe=document.getElementById('alloha_iframe');"
                + "iframe.onload=function(){try{var w=iframe.contentWindow;var bnsiData=null;var hdr={};var done=false;var lastM3u8=null;var _lastEdgeHash=null;"
                + "function put(k,v){if(!k||!v)return;hdr[String(k).toLowerCase()]=String(v);if(done){try{AndroidBridge.onStreamHeaders(JSON.stringify(hdr));}catch(e){}}}"
                + "function check(){if(done)return;var a=!!hdr['authorizations'];var c=!!hdr['accepts-controls'];if(bnsiData&&a&&c){done=true;AndroidBridge.onReady(bnsiData,JSON.stringify(hdr));}}"
                + "put('origin',w.location.origin);put('referer',w.location.origin+'/');put('user-agent',w.navigator.userAgent);put('accept','*/*');"
                + "var oOpen=w.XMLHttpRequest.prototype.open;"
                + "w.XMLHttpRequest.prototype.open=function(m,u){this._u=u;this.addEventListener('load',function(){var r=this.responseURL||'';"
                + "if(r.indexOf('/bnsi/')!==-1&&!done){bnsiData=this.responseText;check();}"
                + "if(done&&r.indexOf('master.m3u8')!==-1&&r!==lastM3u8){lastM3u8=r;AndroidBridge.onM3u8Refreshed(r,JSON.stringify(hdr));}"
                + "});return oOpen.apply(this,arguments);};"
                + "var oSet=w.XMLHttpRequest.prototype.setRequestHeader;"
                + "w.XMLHttpRequest.prototype.setRequestHeader=function(n,v){put(n,v);if((this._u||'').indexOf('.m3u8')!==-1||(this._u||'').indexOf('.ts')!==-1)check();return oSet.apply(this,arguments);};"
                + "var oFetch=w.fetch;w.fetch=function(input,init){try{var u=typeof input==='string'?input:(input&&input.url?input.url:'');"
                + "if(init&&init.headers){if(typeof init.headers.forEach==='function'){init.headers.forEach(function(v,k){put(k,v);});}else{for(var hk in init.headers){put(hk,init.headers[hk]);}}}"
                + "if(u&&(u.indexOf('.m3u8')!==-1||u.indexOf('.ts')!==-1))check();}catch(e){}return oFetch.apply(this,arguments);};"
                + "var oSend=w.WebSocket.prototype.send;"
                + "w.WebSocket.prototype.send=function(data){if(!this.__alloha_hooked){this.__alloha_hooked=true;var ws=this;"
                + "ws.addEventListener('message',function(ev){try{var msg=JSON.parse(ev.data);if(msg&&msg.type==='config_update'&&msg.edge_hash&&msg.edge_hash!==_lastEdgeHash){_lastEdgeHash=msg.edge_hash;hdr['accepts-controls']=msg.edge_hash;AndroidBridge.onConfigUpdate(msg.edge_hash,msg.ttl||120,JSON.stringify(hdr));}}catch(e){}});}"
                + "return oSend.call(this,data);};"
                + "setInterval(function(){if(!done){var b=w.document.querySelector('.allplay__play-btn');if(b)b.click();var v=w.document.querySelector('video');if(v){v.muted=true;if(v.paused)v.play().catch(function(){});}}},1500);"
                + "}catch(e){AndroidBridge.onLog('JS Error: '+e);}};"
                + "</script></body></html>";
    }
}
