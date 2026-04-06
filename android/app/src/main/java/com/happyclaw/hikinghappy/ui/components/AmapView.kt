package com.happyclaw.hikinghappy.ui.components

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleStartEffect
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions

/**
 * Amap map using native SDK MapView.
 * Note: isMyLocationEnabled is intentionally NOT used — we rely on Google
 * FusedLocationProvider for position, and display it via manual Marker.
 */
@SuppressLint("MissingPermission")
@Composable
fun AmapView(
    latitude: Double,
    longitude: Double,
    hasFix: Boolean,
    modifier: Modifier = Modifier
) {
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
                    uiSettings.isMyLocationButtonEnabled = false
                    uiSettings.isScrollGesturesEnabled = true
                    uiSettings.isZoomGesturesEnabled = true
                    moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(39.90923, 116.397428), 16f))
                }
            }
        },
        modifier = modifier
    )

    // Move camera + marker when GPS updates
    LaunchedEffect(hasFix, latitude, longitude) {
        if (hasFix && latitude != 0.0 && longitude != 0.0) {
            val map = aMap ?: return@LaunchedEffect
            val latLng = LatLng(latitude, longitude)
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

    // Lifecycle: onResume / onPause tied to RESUMED state
    LifecycleStartEffect(Lifecycle.State.RESUMED) {
        mapView?.onResume()
        onStopOrDispose {
            mapView?.onPause()
        }
    }

    // Lifecycle: onDestroy on composable removal
    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDestroy()
        }
    }
}
