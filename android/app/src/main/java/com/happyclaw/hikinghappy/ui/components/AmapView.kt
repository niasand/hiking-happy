package com.happyclaw.hikinghappy.ui.components

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions

@Composable
fun AmapView(
    latitude: Double,
    longitude: Double,
    hasFix: Boolean,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var aMap by remember { mutableStateOf<AMap?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView?.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView?.onDestroy()
        }
    }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                mapView = this
                this.onCreate(Bundle())
                aMap = this.map.apply {
                    uiSettings.apply {
                        isZoomControlsEnabled = false
                        isCompassEnabled = false
                        isMyLocationButtonEnabled = false
                        isScaleControlsEnabled = false
                    }
                }
            }
        },
        modifier = modifier
    )

    // Update marker position and camera when location changes
    LaunchedEffect(hasFix, latitude, longitude) {
        val map = aMap ?: return@LaunchedEffect
        if (hasFix && latitude != 0.0 && longitude != 0.0) {
            val latLng = LatLng(latitude, longitude)

            // Clear old markers and add new one
            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title("You")
            )

            // Move camera on first fix
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(latLng, 16f),
                1000,
                null
            )
        }
    }
}
