package com.happyclaw.hikinghappy.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_record",
    indices = [
        Index(value = ["timestamp"], unique = true)
    ]
)
data class ActivityRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val altitude: Double,
    val speed: Double,
    val type: ActivityType,
    val location: String? = null,
    val timestamp: Long
)
