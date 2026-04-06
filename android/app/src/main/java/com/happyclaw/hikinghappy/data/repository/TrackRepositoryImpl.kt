package com.happyclaw.hikinghappy.data.repository

import com.happyclaw.hikinghappy.data.local.dao.TrackPointDao
import com.happyclaw.hikinghappy.data.local.dao.TrackSessionDao
import com.happyclaw.hikinghappy.data.local.entity.ActivityType
import com.happyclaw.hikinghappy.data.local.entity.TrackPoint
import com.happyclaw.hikinghappy.data.local.entity.TrackSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class TrackRepositoryImpl @Inject constructor(
    private val trackSessionDao: TrackSessionDao,
    private val trackPointDao: TrackPointDao
) : TrackRepository {

    override suspend fun startSession(activityType: ActivityType, location: String?): Long {
        val session = TrackSession(
            activityType = activityType,
            location = location?.takeIf { it.isNotBlank() },
            startTime = System.currentTimeMillis()
        )
        return trackSessionDao.insert(session)
    }

    override suspend fun addTrackPoints(points: List<TrackPoint>): List<Long> {
        if (points.isEmpty()) return emptyList()
        return trackPointDao.insertPoints(points)
    }

    override suspend fun finalizeSession(sessionId: Long) {
        val points = trackPointDao.getPointsForSessionOnce(sessionId)
        val original = trackSessionDao.getSessionById(sessionId).first() ?: return

        val now = System.currentTimeMillis()
        val durationSec = (now - original.startTime) / 1000L

        // Compute total distance using Haversine
        var totalDistance = 0.0
        for (i in 1 until points.size) {
            totalDistance += haversine(
                points[i - 1].latitude, points[i - 1].longitude,
                points[i].latitude, points[i].longitude
            )
        }

        val maxAltitude = if (points.isEmpty()) 0.0 else points.maxOf { it.altitude }
        val minAltitude = if (points.isEmpty()) 0.0 else points.minOf { it.altitude }
        val maxSpeed = if (points.isEmpty()) 0.0 else points.maxOf { it.speed.toDouble() }

        val updated = original.copy(
            endTime = now,
            totalDistance = totalDistance,
            totalDuration = durationSec,
            maxAltitude = maxAltitude,
            minAltitude = minAltitude,
            maxSpeed = maxSpeed,
            pointCount = points.size
        )
        trackSessionDao.update(updated)
    }

    override fun getAllSessions(): Flow<List<TrackSession>> {
        return trackSessionDao.getAllSessions()
    }

    override fun getPointsForSession(sessionId: Long): Flow<List<TrackPoint>> {
        return trackPointDao.getPointsForSession(sessionId)
    }

    override suspend fun getSessionOnce(sessionId: Long): TrackSession? {
        return trackSessionDao.getSessionByIdOnce(sessionId)
    }

    override suspend fun getPointsForSessionOnce(sessionId: Long): List<TrackPoint> {
        return trackPointDao.getPointsForSessionOnce(sessionId)
    }

    /** Haversine distance in meters */
    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
