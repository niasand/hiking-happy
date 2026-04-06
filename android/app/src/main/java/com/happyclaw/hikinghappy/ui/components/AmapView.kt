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
import com.amap.api.maps.model.Marker
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
    refreshTrigger: Int = 0,
    modifier: Modifier = Modifier
) {
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var aMap by remember { mutableStateOf<AMap?>(null) }
    var currentMarker by remember { mutableStateOf<Marker?>(null) }
    // Track whether camera has been positioned at least once
    var hasCameraPositioned by remember { mutableStateOf(false) }

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

    // Update marker position on every GPS fix (no camera move — preserves user zoom)
    LaunchedEffect(latitude, longitude) {
        if (latitude != 0.0 && longitude != 0.0) {
            val map = aMap ?: return@LaunchedEffect
            val latLng = LatLng(latitude, longitude)
            if (currentMarker != null) {
                // Just update position — no clear/re-add, avoids scroll jank
                currentMarker!!.position = latLng
            } else {
                currentMarker = map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .title("My Location")
                )
            }
        }
    }

    // Move camera only on first GPS fix or when user taps locate button
    LaunchedEffect(hasFix, latitude, longitude, refreshTrigger) {
        if (hasFix && latitude != 0.0 && longitude != 0.0) {
            val map = aMap ?: return@LaunchedEffect
            if (!hasCameraPositioned) {
                hasCameraPositioned = true
                val latLng = LatLng(latitude, longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            }
        }
    }

    // Re-center camera when user taps locate button
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0 && latitude != 0.0 && longitude != 0.0) {
            val map = aMap ?: return@LaunchedEffect
            val latLng = LatLng(latitude, longitude)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        }
    }

    // Lifecycle: onResume / onPause / onDestroy
    LifecycleStartEffect(Lifecycle.State.RESUMED) {
        mapView?.onResume()
        onStopOrDispose {
            mapView?.onPause()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            currentMarker = null
            mapView?.onDestroy()
        }
    }
}
