package com.happyclaw.hikinghappy.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
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
import com.amap.api.maps.model.PolylineOptions
import com.happyclaw.hikinghappy.data.local.entity.TrackPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

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
    trackPoints: List<TrackPoint> = emptyList(),
    fitTrack: Boolean = false,
    showTrackOverlay: Boolean = false,
    modifier: Modifier = Modifier
) {
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var aMap by remember { mutableStateOf<AMap?>(null) }
    var currentMarker by remember { mutableStateOf<Marker?>(null) }
    var currentPolyline by remember { mutableStateOf<com.amap.api.maps.model.Polyline?>(null) }
    var hasCameraPositioned by remember { mutableStateOf(false) }
    // Track last marker position to avoid unnecessary updates
    var lastMarkerLat by remember { mutableStateOf(0.0) }
    var lastMarkerLon by remember { mutableStateOf(0.0) }

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

    // Update marker only when position moved > 3 meters (avoids gesture interference)
    LaunchedEffect(latitude, longitude) {
        if (latitude != 0.0 && longitude != 0.0) {
            val dist = distanceBetween(lastMarkerLat, lastMarkerLon, latitude, longitude)
            if (dist < 3.0 && currentMarker != null) return@LaunchedEffect

            val map = aMap ?: return@LaunchedEffect
            val latLng = LatLng(latitude, longitude)
            lastMarkerLat = latitude
            lastMarkerLon = longitude

            if (currentMarker != null) {
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

    // Move camera only on first GPS fix
    LaunchedEffect(hasFix, latitude, longitude) {
        if (hasFix && latitude != 0.0 && longitude != 0.0) {
            if (!hasCameraPositioned) {
                hasCameraPositioned = true
                aMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 16f))
            }
        }
    }

    // Re-center camera when user taps locate button
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0 && latitude != 0.0 && longitude != 0.0) {
            aMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 16f))
        }
    }

    // Draw/update polyline for track points
    LaunchedEffect(trackPoints.size, trackPoints.lastOrNull()?.id) {
        val map = aMap ?: return@LaunchedEffect
        if (trackPoints.isEmpty()) {
            currentPolyline?.remove()
            currentPolyline = null
            return@LaunchedEffect
        }

        // Downsample to max 1000 points if needed
        val points = if (trackPoints.size > 1000) {
            val step = trackPoints.size.toDouble() / 1000
            (0 until 1000).map { i ->
                trackPoints[(i * step).toInt().coerceAtMost(trackPoints.size - 1)]
            }
        } else {
            trackPoints
        }

        val latLngs = points.map { LatLng(it.latitude, it.longitude) }

        // Remove old polyline and add new one
        currentPolyline?.remove()
        currentPolyline = map.addPolyline(
            PolylineOptions()
                .addAll(latLngs)
                .width(6f)
                .color(0xFF4ECB71.toInt())
        )

        // Auto-fit camera to show entire track
        if (fitTrack && latLngs.size >= 2) {
            val builder = com.amap.api.maps.model.LatLngBounds.builder()
            latLngs.forEach { builder.include(it) }
            val bounds = builder.build()
            val padding = 80
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
        }
    }

    // Draw start/end markers and direction arrows
    LaunchedEffect(trackPoints.size, trackPoints.lastOrNull()?.id, showTrackOverlay) {
        val map = aMap ?: return@LaunchedEffect

        // Remove previous overlay markers
        trackOverlayMarkers.forEach { it.remove() }
        trackOverlayMarkers.clear()

        if (!showTrackOverlay || trackPoints.size < 2) return@LaunchedEffect

        val first = trackPoints.first()
        val last = trackPoints.last()

        // Start marker (green)
        val startBitmap = createCircleBitmap(Color.parseColor("#22C55E"), 36f)
        map.addMarker(
            MarkerOptions()
                .position(LatLng(first.latitude, first.longitude))
                .icon(BitmapDescriptorFactory.fromBitmap(startBitmap))
                .title("Start")
                .anchor(0.5f, 0.5f)
        ).let { trackOverlayMarkers.add(it) }

        // End marker (red)
        val endBitmap = createCircleBitmap(Color.parseColor("#EF4444"), 36f)
        map.addMarker(
            MarkerOptions()
                .position(LatLng(last.latitude, last.longitude))
                .icon(BitmapDescriptorFactory.fromBitmap(endBitmap))
                .title("End")
                .anchor(0.5f, 0.5f)
        ).let { trackOverlayMarkers.add(it) }

        // Direction arrows at regular intervals (~25 arrows for typical tracks)
        val arrowInterval = if (trackPoints.size > 100) trackPoints.size / 50 else trackPoints.size / 3
        if (arrowInterval >= 2) {
            for (i in arrowInterval until trackPoints.size - 1 step arrowInterval) {
                val p = trackPoints[i]
                val pNext = trackPoints[i + 1]
                val bearing = bearingBetween(p.latitude, p.longitude, pNext.latitude, pNext.longitude)
                val rotatedArrow = createArrowBitmap(bearing.toFloat())

                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(p.latitude, p.longitude))
                        .icon(BitmapDescriptorFactory.fromBitmap(rotatedArrow))
                        .anchor(0.5f, 0.5f)
                ).let { trackOverlayMarkers.add(it) }
            }
        }
    }

    // Lifecycle
    LifecycleStartEffect(Lifecycle.State.RESUMED) {
        mapView?.onResume()
        onStopOrDispose { mapView?.onPause() }
    }

    DisposableEffect(Unit) {
        onDispose {
            trackOverlayMarkers.forEach { it.remove() }
            trackOverlayMarkers.clear()
            currentMarker = null
            currentPolyline = null
            mapView?.onDestroy()
        }
    }
}

// Mutable list to hold overlay markers for cleanup
private val trackOverlayMarkers = mutableListOf<Marker>()

/** Approximate distance in meters between two lat/lon points */
private fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    if (lat1 == 0.0 && lon1 == 0.0) return Double.MAX_VALUE
    val R = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    return R * 2 * atan2(sqrt(a), sqrt(1 - a))
}

/** Bearing in degrees from point1 to point2 (0=North, 90=East) */
private fun bearingBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val dLon = Math.toRadians(lon2 - lon1)
    val lat1R = Math.toRadians(lat1)
    val lat2R = Math.toRadians(lat2)
    val y = sin(dLon) * cos(lat2R)
    val x = cos(lat1R) * sin(lat2R) - sin(lat1R) * cos(lat2R) * cos(dLon)
    return (Math.toDegrees(atan2(y, x)) + 360) % 360
}

/** Create a filled circle bitmap for start/end markers */
private fun createCircleBitmap(color: Int, sizePx: Float): Bitmap {
    val size = sizePx.toInt()
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
    }
    // White border
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    val center = size / 2f
    val radius = (size - 8f) / 2f
    canvas.drawCircle(center, center, radius, paint)
    canvas.drawCircle(center, center, radius, borderPaint)
    return bitmap
}

/** Create a directional arrow bitmap, rotated to the given bearing (degrees, 0=North) */
private fun createArrowBitmap(rotationDegrees: Float = 0f): Bitmap {
    val size = 64
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    canvas.rotate(rotationDegrees, size / 2f, size / 2f)

    val path = Path().apply {
        moveTo(size / 2f, 4f)            // Top tip
        lineTo(size - 6f, size - 10f)    // Bottom right
        lineTo(size / 2f, size - 22f)    // Bottom center notch
        lineTo(6f, size - 10f)           // Bottom left
        close()
    }

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2D9C4F")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    canvas.drawPath(path, fillPaint)
    canvas.drawPath(path, strokePaint)
    return bitmap
}
