package com.happyclaw.hikinghappy.ui.components

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.happyclaw.hikinghappy.BuildConfig

private const val TAG = "AmapView"
private fun amapKey(): String = BuildConfig.AMAP_API_KEY

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AmapView(
    latitude: Double,
    longitude: Double,
    hasFix: Boolean,
    modifier: Modifier = Modifier
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    val key = amapKey()
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    // Use v1.4.15 — no jscode security config required
    val htmlPage = remember(key) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no">
            <style>
                html,body{margin:0;padding:0;width:100%;height:100%;overflow:hidden}
                #map{width:100%;height:100%}
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script src="https://webapi.amap.com/maps?v=1.4.15&key=$key&plugin=AMap.Marker"></script>
            <script>
                var map = null;
                var marker = null;
                var ready = false;

                try {
                    map = new AMap.Map('map', {
                        zoom: 16,
                        center: [116.397428, 39.90923],
                        zooms: [3, 18],
                        resizeEnable: true
                    });
                    ready = true;
                    console.log('AMap initialized OK');
                } catch(e) {
                    console.error('AMap init failed: ' + e.message);
                }

                function moveTo(lat, lng) {
                    if (!map || !ready) return;
                    var center = new AMap.LngLat(lng, lat);
                    if (marker) {
                        map.remove(marker);
                    }
                    marker = new AMap.Marker({
                        position: center,
                        title: 'My Location',
                        icon: new AMap.Icon({
                            size: new AMap.Size(24, 24),
                            image: 'https://webapi.amap.com/theme/v1.3/markers/n/loc.png',
                            imageSize: new AMap.Size(24, 24)
                        })
                    });
                    map.add(marker);
                    map.setCenter(center);
                    map.setZoom(16);
                    console.log('Moved to: ' + lat + ',' + lng);
                }

                window.moveTo = moveTo;
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webView = this
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.mixedContentMode =
                    android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                settings.blockNetworkImage = false

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(msg: ConsoleMessage?): Boolean {
                        Log.d(TAG, "JS: ${msg?.message()} [${msg?.sourceId()}:${msg?.lineNumber()}]")
                        return true
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d(TAG, "WebView page finished: $url")
                        // Re-trigger position after page loads
                        if (hasFix && latitude != 0.0 && longitude != 0.0) {
                            mainHandler.postDelayed({
                                evaluateJavascript("moveTo($latitude,$longitude);", null)
                            }, 500)
                        }
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: android.webkit.WebResourceRequest?,
                        error: android.webkit.WebResourceError
                    ) {
                        Log.e(TAG, "WebView error: ${error.description} code=${error.errorCode}")
                    }
                }

                loadDataWithBaseURL(
                    "https://webapi.amap.com",
                    htmlPage,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        modifier = modifier
    )

    LaunchedEffect(hasFix, latitude, longitude) {
        if (hasFix && latitude != 0.0 && longitude != 0.0) {
            // Small delay to ensure WebView + JS are ready
            mainHandler.postDelayed({
                webView?.evaluateJavascript("if(typeof moveTo==='function')moveTo($latitude,$longitude);", null)
            }, 300)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.destroy()
            mainHandler.removeCallbacksAndMessages(null)
        }
    }
}
