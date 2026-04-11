package com.happyclaw.hikinghappy.ui.screens.history

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Poi
import com.amap.api.navi.AmapNaviPage
import com.amap.api.navi.AmapNaviParams
import com.amap.api.navi.AmapNaviType
import com.happyclaw.hikinghappy.data.local.entity.TrackPoint
import com.happyclaw.hikinghappy.data.local.entity.TrackSession
import com.happyclaw.hikinghappy.data.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrackPreviewState(
    val session: TrackSession? = null,
    val points: List<TrackPoint> = emptyList(),
    val isLoading: Boolean = true,
    val centerLat: Double = 0.0,
    val centerLon: Double = 0.0
)

@HiltViewModel
class TrackPreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val trackRepository: TrackRepository
) : ViewModel() {

    private val sessionId: Long = savedStateHandle.get<Long>("sessionId") ?: -1L

    private val _state = MutableStateFlow(TrackPreviewState())
    val state: StateFlow<TrackPreviewState> = _state.asStateFlow()

    init {
        loadTrack()
    }

    private fun loadTrack() {
        if (sessionId <= 0) {
            _state.value = _state.value.copy(isLoading = false)
            return
        }

        viewModelScope.launch {
            val session = trackRepository.getSessionOnce(sessionId)
            val points = trackRepository.getPointsForSessionOnce(sessionId)

            // Compute center point for initial camera position
            val centerLat = if (points.isNotEmpty()) points.map { it.latitude }.average() else 0.0
            val centerLon = if (points.isNotEmpty()) points.map { it.longitude }.average() else 0.0

            _state.value = TrackPreviewState(
                session = session,
                points = points,
                isLoading = false,
                centerLat = centerLat,
                centerLon = centerLon
            )
        }
    }

    /**
     * Start in-app Amap navigation from track start to end point.
     * Uses AmapNaviPage navigation component for walking mode.
     */
    fun startNavigation(context: Context) {
        val points = _state.value.points
        if (points.size < 2) return

        try {
            val startPoi = Poi("起点", LatLng(points.first().latitude, points.first().longitude), "")
            val endPoi = Poi("终点", LatLng(points.last().latitude, points.last().longitude), "")
            val params = AmapNaviParams(startPoi, null, endPoi, AmapNaviType.WALK)
            params.setUseInnerVoice(true)
            AmapNaviPage.getInstance().showRouteActivity(context, params, null)
        } catch (e: Exception) {
            Log.e("TrackPreviewVM", "Navigation failed", e)
            Toast.makeText(context, "导航启动失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
