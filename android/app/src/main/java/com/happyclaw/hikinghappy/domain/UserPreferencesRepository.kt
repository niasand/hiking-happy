package com.happyclaw.hikinghappy.domain

import com.happyclaw.hikinghappy.data.local.entity.ActivityType
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val activityType: Flow<ActivityType>
    val location: Flow<String>
    val altitudeUnitIndex: Flow<Int>
    val speedUnitIndex: Flow<Int>

    suspend fun setActivityType(type: ActivityType)
    suspend fun setLocation(location: String)
    suspend fun setAltitudeUnitIndex(index: Int)
    suspend fun setSpeedUnitIndex(index: Int)
}
