package com.happyclaw.hikinghappy.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.happyclaw.hikinghappy.data.local.entity.TrackSession
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackSessionDao {

    @Insert
    suspend fun insert(session: TrackSession): Long

    @Update
    suspend fun update(session: TrackSession): Int

    @Query("SELECT * FROM track_sessions WHERE id = :id")
    fun getSessionById(id: Long): Flow<TrackSession?>

    @Query("SELECT * FROM track_sessions WHERE id = :id")
    suspend fun getSessionByIdOnce(id: Long): TrackSession?

    @Query("SELECT * FROM track_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<TrackSession>>

    @Delete
    suspend fun delete(session: TrackSession)

    @Query("DELETE FROM track_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
