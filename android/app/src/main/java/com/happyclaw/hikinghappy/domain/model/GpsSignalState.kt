package com.happyclaw.hikinghappy.domain.model

enum class GpsSignalState {
    ACTIVE,      // accuracy < 20m, green
    WEAK,        // accuracy 20-50m, amber
    POOR,        // accuracy > 50m, red
    LOST         // no fix, gray
}
