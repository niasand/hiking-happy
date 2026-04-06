package com.happyclaw.hikinghappy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_sessions")
data class TrackSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val activityType: ActivityType,
    val location: String? = null,
    val startTime: Long,
    val endTime: Long? = null,
    val totalDistance: Double = 0.0,
    val totalDuration: Long = 0L,
    val maxAltitude: Double = 0.0,
    val minAltitude: Double = 0.0,
    val maxSpeed: Double = 0.0,
    val pointCount: Int = 0
)
