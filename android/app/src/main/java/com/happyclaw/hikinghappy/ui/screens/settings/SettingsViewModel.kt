package com.happyclaw.hikinghappy.ui.screens.settings

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happyclaw.hikinghappy.data.local.entity.ActivityType
import com.happyclaw.hikinghappy.domain.UserPreferencesRepository
import com.happyclaw.hikinghappy.service.SyncPhase
import com.happyclaw.hikinghappy.service.SyncProgress
import com.happyclaw.hikinghappy.service.SyncService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val syncService: SyncService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val activityType: StateFlow<ActivityType> = preferencesRepository.activityType
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ActivityType.HIKING)

    val location: StateFlow<String> = preferencesRepository.location
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val altitudeUnitIndex: StateFlow<Int> = preferencesRepository.altitudeUnitIndex
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val speedUnitIndex: StateFlow<Int> = preferencesRepository.speedUnitIndex
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _syncProgress = MutableStateFlow<SyncProgress?>(null)
    val syncProgress: StateFlow<SyncProgress?> = _syncProgress.asStateFlow()

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun setActivityType(type: ActivityType) {
        viewModelScope.launch {
            preferencesRepository.setActivityType(type)
        }
    }

    fun setAltitudeUnitIndex(index: Int) {
        viewModelScope.launch {
            preferencesRepository.setAltitudeUnitIndex(index)
        }
    }

    fun setSpeedUnitIndex(index: Int) {
        viewModelScope.launch {
            preferencesRepository.setSpeedUnitIndex(index)
        }
    }

    fun startBackup() {
        if (!isNetworkAvailable()) {
            _syncProgress.value = SyncProgress(
                SyncPhase.ERROR, 0,
                "No network connection. Try again later."
            )
            return
        }
        viewModelScope.launch {
            syncService.backup().collect { progress ->
                _syncProgress.value = progress
            }
        }
    }

    fun startRestore() {
        if (!isNetworkAvailable()) {
            _syncProgress.value = SyncProgress(
                SyncPhase.ERROR, 0,
                "No network connection. Try again later."
            )
            return
        }
        viewModelScope.launch {
            syncService.restore().collect { progress ->
                _syncProgress.value = progress
            }
        }
    }

    fun dismissSyncProgress() {
        _syncProgress.value = null
    }
}
