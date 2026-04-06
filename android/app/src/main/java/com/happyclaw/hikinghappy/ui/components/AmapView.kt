package com.happyclaw.hikinghappy.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.happyclaw.hikinghappy.BuildConfig

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

    val htmlPage = remember(amapKey()) {
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
            <script src="https://webapi.amap.com/maps?v=2.0&key=${amapKey()}"></script>
            <script>
                var map=null,marker=null;
                function init(){
                    map=new AMap.Map('map',{zoom:16,center:[116.397428,39.90923],zooms:[3,18],dragging:true,scrollWheel:false});
                }
                function moveTo(lat,lng){
                    if(!map)return;
                    var c=new AMap.LngLat(lng,lat);
                    if(marker)map.remove(marker);
                    marker=new AMap.Marker({position:c,title:'You',
                        icon:new AMap.Icon({size:new AMap.Size(24,24),
                        image:'https://webapi.amap.com/theme/v1.3/markers/n/loc.png',
                        imageSize:new AMap.Size(24,24)})
                    });
                    map.add(marker);map.setCenter(c);map.setZoom(16);
                }
                window.moveTo=moveTo;initMap();
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
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                webViewClient = WebViewClient()
                loadDataWithBaseURL("https://webapi.amap.com", htmlPage, "text/html", "UTF-8", null)
            }
        },
        modifier = modifier
    )

    LaunchedEffect(hasFix, latitude, longitude) {
        if (hasFix && latitude != 0.0 && longitude != 0.0) {
            webView?.evaluateJavascript("moveTo($latitude,$longitude);", null)
        }
    }

    DisposableEffect(Unit) {
        onDispose { webView?.destroy() }
    }
}
