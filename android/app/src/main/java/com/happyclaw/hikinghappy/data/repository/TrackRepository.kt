package com.happyclaw.hikinghappy.data.repository

import com.happyclaw.hikinghappy.data.import.ParsedTrack
import com.happyclaw.hikinghappy.data.local.entity.ActivityType
import com.happyclaw.hikinghappy.data.local.entity.TrackPoint
import com.happyclaw.hikinghappy.data.local.entity.TrackSession
import kotlinx.coroutines.flow.Flow

interface TrackRepository {

    /**
     * Create a new track session. Returns the session ID.
     */
    suspend fun startSession(activityType: ActivityType, location: String?): Long

    /**
     * Batch insert track points for a session. Returns inserted row IDs.
     */
    suspend fun addTrackPoints(points: List<TrackPoint>): List<Long>

    /**
     * Finalize a session: compute stats (distance, duration, max altitude/speed, point count)
     * and update the session record.
     */
    suspend fun finalizeSession(sessionId: Long)

    /**
     * Observe all sessions, newest first.
     */
    fun getAllSessions(): Flow<List<TrackSession>>

    /**
     * Observe track points for a session, ordered by timestamp ASC.
     */
    fun getPointsForSession(sessionId: Long): Flow<List<TrackPoint>>

    /**
     * Get a single session by ID (one-shot, not Flow).
     */
    suspend fun getSessionOnce(sessionId: Long): TrackSession?

    /**
     * Get all track points for a session (one-shot, not Flow).
     */
    suspend fun getPointsForSessionOnce(sessionId: Long): List<TrackPoint>

    /**
     * Delete a track session and all its points (CASCADE).
     */
    suspend fun deleteSession(sessionId: Long)

    /**
     * Import a parsed KML track as a new session. Returns the session ID.
     */
    suspend fun importSession(parsedTrack: ParsedTrack): Long
}
