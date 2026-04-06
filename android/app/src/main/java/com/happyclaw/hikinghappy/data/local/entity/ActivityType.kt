package com.happyclaw.hikinghappy.data.local.entity

import kotlinx.serialization.Serializable

@Serializable
enum class ActivityType {
    HIKING,
    WALKING,
    CYCLING,
    RUNNING;

    fun displayName(): String = when (this) {
        HIKING -> "Hiking"
        WALKING -> "Walking"
        CYCLING -> "Cycling"
        RUNNING -> "Running"
    }
}
