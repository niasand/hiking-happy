package com.happyclaw.hikinghappy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.happyclaw.hikinghappy.data.local.entity.ActivityRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: ActivityRecord): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(records: List<ActivityRecord>): List<Long>

    @Query("SELECT * FROM activity_record WHERE timestamp >= :since ORDER BY timestamp ASC LIMIT 14400")
    fun getRecordsSince(since: Long): Flow<List<ActivityRecord>>

    @Query("SELECT * FROM activity_record ORDER BY timestamp ASC")
    fun getAllRecords(): Flow<List<ActivityRecord>>

    @Query("SELECT * FROM activity_record ORDER BY timestamp ASC LIMIT :limit OFFSET :offset")
    suspend fun getRecordsPaginated(limit: Int, offset: Int): List<ActivityRecord>

    @Query("SELECT COUNT(*) FROM activity_record")
    suspend fun getRecordCount(): Int

    @Query("SELECT * FROM activity_record ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestRecord(): ActivityRecord?
}
