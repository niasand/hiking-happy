package com.happyclaw.hikinghappy.ui.screens.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happyclaw.hikinghappy.data.local.entity.ActivityRecord
import com.happyclaw.hikinghappy.data.repository.ActivityRepository
import com.happyclaw.hikinghappy.domain.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TrendsViewModel @Inject constructor(
    private val repository: ActivityRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val twoHoursAgo: Long
        get() = System.currentTimeMillis() - 2 * 60 * 60 * 1000L

    val altitudeUnitIndex: StateFlow<Int> = preferencesRepository.altitudeUnitIndex
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val speedUnitIndex: StateFlow<Int> = preferencesRepository.speedUnitIndex
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val records: StateFlow<List<ActivityRecord>> = flow {
        while (true) {
            emit(Unit)
            kotlinx.coroutines.delay(2000)
        }
    }.flatMapLatest {
        repository.getRecordsSince(twoHoursAgo)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
}
