package com.happyclaw.hikinghappy.data.repository

import com.happyclaw.hikinghappy.data.import.ParsedTrack
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
        val durationSec = if (original.endTime != null) {
            (original.endTime!! - original.startTime) / 1000L
        } else {
            (now - original.startTime) / 1000L
        }

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
            endTime = original.endTime ?: now,
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

    override suspend fun deleteSession(sessionId: Long) {
        trackSessionDao.deleteById(sessionId) // CASCADE deletes associated TrackPoints
    }

    override suspend fun importSession(parsedTrack: ParsedTrack): Long {
        // Build location string from start/end names, e.g. "马峦山公园 → 郊野公园北门"
        val location = buildImportLocation(parsedTrack)
        val session = TrackSession(
            activityType = parsedTrack.activityType,
            location = location,
            startTime = parsedTrack.startTime,
            endTime = parsedTrack.endTime
        )
        val sessionId = trackSessionDao.insert(session)

        val points = parsedTrack.points.map { p ->
            TrackPoint(
                sessionId = sessionId,
                latitude = p.latitude,
                longitude = p.longitude,
                altitude = p.altitude,
                speed = p.speed,
                accuracy = p.accuracy,
                timestamp = p.timestamp
            )
        }
        trackPointDao.insertPoints(points)

        // Compute stats directly (avoid finalizeSession which may overwrite endTime via Flow timing)
        val dbPoints = trackPointDao.getPointsForSessionOnce(sessionId)
        var totalDistance = 0.0
        for (i in 1 until dbPoints.size) {
            totalDistance += haversine(
                dbPoints[i - 1].latitude, dbPoints[i - 1].longitude,
                dbPoints[i].latitude, dbPoints[i].longitude
            )
        }
        val original = trackSessionDao.getSessionByIdOnce(sessionId) ?: return sessionId
        val updated = original.copy(
            totalDistance = totalDistance,
            totalDuration = (parsedTrack.endTime - parsedTrack.startTime) / 1000L,
            maxAltitude = dbPoints.maxOfOrNull { it.altitude } ?: 0.0,
            minAltitude = dbPoints.minOfOrNull { it.altitude } ?: 0.0,
            maxSpeed = dbPoints.maxOfOrNull { it.speed.toDouble() } ?: 0.0,
            pointCount = dbPoints.size
        )
        trackSessionDao.update(updated)
        return sessionId
    }

    private fun buildImportLocation(parsedTrack: ParsedTrack): String? {
        val start = parsedTrack.startLocationName?.takeIf { it.isNotBlank() }
        val end = parsedTrack.endLocationName?.takeIf { it.isNotBlank() }
        return when {
            start != null && end != null && start != end -> "$start → $end"
            start != null -> start
            end != null -> end
            else -> parsedTrack.location?.takeIf { it.isNotBlank() }
        }
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
