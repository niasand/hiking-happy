package com.happyclaw.hikinghappy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.happyclaw.hikinghappy.data.local.entity.TrackPoint

@Dao
interface TrackPointDao {

    @Insert
    suspend fun insertPoints(points: List<TrackPoint>): List<Long>

    @Query("SELECT * FROM track_points WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getPointsForSession(sessionId: Long): kotlinx.coroutines.flow.Flow<List<TrackPoint>>

    @Query("SELECT * FROM track_points WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getPointsForSessionOnce(sessionId: Long): List<TrackPoint>

    @Query("SELECT COUNT(*) FROM track_points WHERE sessionId = :sessionId")
    suspend fun getPointCountForSession(sessionId: Long): Int

    @Query("DELETE FROM track_points WHERE sessionId = :sessionId")
    suspend fun deletePointsForSession(sessionId: Long)
}
