package com.happyclaw.hikinghappy.data.import

import com.happyclaw.hikinghappy.data.local.entity.ActivityType

/**
 * Intermediate data model for KML import, decoupled from Room entities.
 */
data class ParsedTrack(
    val name: String,
    val activityType: ActivityType,
    val location: String?,
    val startTime: Long,
    val endTime: Long,
    val points: List<ParsedPoint>,
    val startLocationName: String? = null,
    val endLocationName: String? = null
)

data class ParsedPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Float,
    val accuracy: Float?,
    val timestamp: Long
)
