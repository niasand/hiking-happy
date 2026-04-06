package com.happyclaw.hikinghappy.ui.screens.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
}
