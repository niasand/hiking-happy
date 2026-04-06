package com.happyclaw.hikinghappy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.happyclaw.hikinghappy.data.local.converter.Converters
import com.happyclaw.hikinghappy.data.local.dao.ActivityRecordDao
import com.happyclaw.hikinghappy.data.local.entity.ActivityRecord

@Database(
    entities = [ActivityRecord::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class HikingDatabase : RoomDatabase() {
    abstract fun activityRecordDao(): ActivityRecordDao
}
