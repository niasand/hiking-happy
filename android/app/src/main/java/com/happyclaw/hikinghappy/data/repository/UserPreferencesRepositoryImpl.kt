package com.happyclaw.hikinghappy.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.happyclaw.hikinghappy.data.local.entity.ActivityType
import com.happyclaw.hikinghappy.domain.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesRepository {

    private object Keys {
        val ACTIVITY_TYPE = stringPreferencesKey("activity_type")
        val LOCATION = stringPreferencesKey("location")
        val ALTITUDE_UNIT_INDEX = intPreferencesKey("altitude_unit_index")
        val SPEED_UNIT_INDEX = intPreferencesKey("speed_unit_index")
    }

    override val activityType: Flow<ActivityType> = context.dataStore.data.map { prefs ->
        try {
            ActivityType.valueOf(prefs[Keys.ACTIVITY_TYPE] ?: ActivityType.HIKING.name)
        } catch (_: Exception) {
            ActivityType.HIKING
        }
    }

    override val location: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.LOCATION] ?: ""
    }

    override val altitudeUnitIndex: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.ALTITUDE_UNIT_INDEX] ?: 0
    }

    override val speedUnitIndex: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.SPEED_UNIT_INDEX] ?: 0
    }

    override suspend fun setActivityType(type: ActivityType) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACTIVITY_TYPE] = type.name
        }
    }

    override suspend fun setLocation(location: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LOCATION] = location
        }
    }

    override suspend fun setAltitudeUnitIndex(index: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ALTITUDE_UNIT_INDEX] = index
        }
    }

    override suspend fun setSpeedUnitIndex(index: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SPEED_UNIT_INDEX] = index
        }
    }
}
