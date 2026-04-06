package com.happyclaw.hikinghappy.ui.screens.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happyclaw.hikinghappy.data.local.entity.ActivityRecord
import com.happyclaw.hikinghappy.data.repository.ActivityRepository
import com.happyclaw.hikinghappy.domain.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

enum class TimeRange(val label: String, val millis: Long) {
    ONE_HOUR("1h", 60 * 60 * 1000L),
    TWO_HOURS("2h", 2 * 60 * 60 * 1000L),
    FOUR_HOURS("4h", 4 * 60 * 60 * 1000L),
    TODAY("Today", -1L)
}

@HiltViewModel
class TrendsViewModel @Inject constructor(
    private val repository: ActivityRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _selectedRange = MutableStateFlow(TimeRange.TWO_HOURS)
    val selectedRange: StateFlow<TimeRange> = _selectedRange

    fun setTimeRange(range: TimeRange) {
        _selectedRange.value = range
    }

    private fun getSince(range: TimeRange): Long {
        return if (range == TimeRange.TODAY) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        } else {
            System.currentTimeMillis() - range.millis
        }
    }

    val altitudeUnitIndex: StateFlow<Int> = preferencesRepository.altitudeUnitIndex
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val speedUnitIndex: StateFlow<Int> = preferencesRepository.speedUnitIndex
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val records: StateFlow<List<ActivityRecord>> = _selectedRange
        .flatMapLatest { range ->
            flow {
                while (true) {
                    emit(getSince(range))
                    kotlinx.coroutines.delay(2000)
                }
            }
        }.flatMapLatest { since ->
            repository.getRecordsSince(since)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
}
