package com.happyclaw.hikinghappy.data.repository

import com.happyclaw.hikinghappy.data.local.entity.ActivityRecord
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    suspend fun insertRecord(record: ActivityRecord): Long
    suspend fun insertOrIgnore(records: List<ActivityRecord>): List<Long>
    fun getRecordsSince(since: Long): Flow<List<ActivityRecord>>
    fun getAllRecords(): Flow<List<ActivityRecord>>
    suspend fun getRecordsPaginated(limit: Int, offset: Int): List<ActivityRecord>
    suspend fun getRecordCount(): Int
    suspend fun getLatestRecord(): ActivityRecord?
}
