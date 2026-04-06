package com.happyclaw.hikinghappy.ui.screens.history

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happyclaw.hikinghappy.data.export.KmlExporter
import com.happyclaw.hikinghappy.data.local.entity.TrackSession
import com.happyclaw.hikinghappy.data.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class HistoryUiState(
    val sessions: List<TrackSession> = emptyList(),
    val isLoading: Boolean = true,
    val exportMessage: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            trackRepository.getAllSessions().collect { sessions ->
                _state.value = _state.value.copy(
                    sessions = sessions.filter { it.endTime != null },
                    isLoading = false
                )
            }
        }
    }

    fun exportSession(sessionId: Long) {
        viewModelScope.launch {
            try {
                val session = trackRepository.getSessionOnce(sessionId) ?: return@launch
                val points = trackRepository.getPointsForSessionOnce(sessionId)

                if (points.isEmpty()) {
                    _state.value = _state.value.copy(exportMessage = "No track points")
                    return@launch
                }

                val kmlContent = KmlExporter.generate(session, points)
                val fileName = KmlExporter.fileName(session)

                // Save to cache directory
                val file = File(context.cacheDir, "kml_exports")
                file.mkdirs()
                val kmlFile = File(file, fileName)
                kmlFile.writeText(kmlContent)

                // Share via Android intent
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    kmlFile
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.google-earth.kml+xml"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, fileName)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(intent, "Export KML")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)

                _state.value = _state.value.copy(exportMessage = null)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    exportMessage = "Export failed: ${e.message}"
                )
            }
        }
    }

    fun dismissMessage() {
        _state.value = _state.value.copy(exportMessage = null)
    }
}
