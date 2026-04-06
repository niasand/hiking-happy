package com.happyclaw.hikinghappy.data.local.converter

import androidx.room.TypeConverter
import com.happyclaw.hikinghappy.data.local.entity.ActivityType

class Converters {
    @TypeConverter
    fun fromActivityType(value: ActivityType): String = value.name

    @TypeConverter
    fun toActivityType(value: String): ActivityType = ActivityType.valueOf(value)
}
