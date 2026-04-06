package com.happyclaw.hikinghappy.data.repository

import com.happyclaw.hikinghappy.data.local.dao.ActivityRecordDao
import com.happyclaw.hikinghappy.data.local.entity.ActivityRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val dao: ActivityRecordDao
) : ActivityRepository {

    override suspend fun insertRecord(record: ActivityRecord): Long {
        return dao.insert(record)
    }

    override suspend fun insertOrIgnore(records: List<ActivityRecord>): List<Long> {
        return dao.insertOrIgnore(records)
    }

    override fun getRecordsSince(since: Long): Flow<List<ActivityRecord>> {
        return dao.getRecordsSince(since)
    }

    override fun getAllRecords(): Flow<List<ActivityRecord>> {
        return dao.getAllRecords()
    }

    override suspend fun getRecordsPaginated(limit: Int, offset: Int): List<ActivityRecord> {
        return dao.getRecordsPaginated(limit, offset)
    }

    override suspend fun getRecordCount(): Int {
        return dao.getRecordCount()
    }

    override suspend fun getLatestRecord(): ActivityRecord? {
        return dao.getLatestRecord()
    }
}
