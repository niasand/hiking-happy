package com.happyclaw.hikinghappy.ui.components

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle

private const val TAG = "AmapView"

@SuppressLint("MissingPermission")
@Composable
fun AmapView(
    latitude: Double,
    longitude: Double,
    hasFix: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var aMap by remember { mutableStateOf<AMap?>(null) }

    AndroidView(
        factory = { ctx ->
            MapView(ctx, AMapOptions().apply {
                zoomControlsEnabled(false)
                compassEnabled(false)
                scaleControlsEnabled(false)
                logoPosition(AMapOptions.LOGO_POSITION_BOTTOM_LEFT)
            }).also { mv ->
                mapView = mv
                mv.onCreate(Bundle())
                aMap = mv.map.apply {
                    // Blue dot for current location
                    myLocationStyle = MyLocationStyle().apply {
                        myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
                        strokeColor(0x00000000) // transparent stroke
                        radiusFillColor(0x4D2196F3) // semi-transparent blue fill
                    }
                    isMyLocationEnabled = true
                    uiSettings.isMyLocationButtonEnabled = false
                    uiSettings.isScrollGesturesEnabled = true
                    uiSettings.isZoomGesturesEnabled = true
                    Log.d(TAG, "Native MapView created successfully")
                }
            }
        },
        modifier = modifier
    )

    // Move camera when GPS updates
    LaunchedEffect(hasFix, latitude, longitude) {
        if (hasFix && latitude != 0.0 && longitude != 0.0) {
            val map = aMap ?: return@LaunchedEffect
            val latLng = LatLng(latitude, longitude)
            Log.d(TAG, "Moving camera to: $latitude, $longitude")
            // Clear old markers and add new one
            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .title("My Location")
            )
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        }
    }

    // Lifecycle management
    DisposableEffect(Unit) {
        mapView?.onResume()
        onDispose {
            Log.d(TAG, "MapView disposing")
            mapView?.onDestroy()
        }
    }
}
