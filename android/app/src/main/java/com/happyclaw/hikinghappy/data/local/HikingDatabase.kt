package com.happyclaw.hikinghappy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.happyclaw.hikinghappy.data.local.converter.Converters
import com.happyclaw.hikinghappy.data.local.dao.ActivityRecordDao
import com.happyclaw.hikinghappy.data.local.dao.TrackPointDao
import com.happyclaw.hikinghappy.data.local.dao.TrackSessionDao
import com.happyclaw.hikinghappy.data.local.entity.ActivityRecord
import com.happyclaw.hikinghappy.data.local.entity.TrackPoint
import com.happyclaw.hikinghappy.data.local.entity.TrackSession

@Database(
    entities = [
        ActivityRecord::class,
        TrackSession::class,
        TrackPoint::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class HikingDatabase : RoomDatabase() {
    abstract fun activityRecordDao(): ActivityRecordDao
    abstract fun trackSessionDao(): TrackSessionDao
    abstract fun trackPointDao(): TrackPointDao
}
