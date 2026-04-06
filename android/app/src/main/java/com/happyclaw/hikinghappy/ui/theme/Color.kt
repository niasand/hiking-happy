package com.happyclaw.hikinghappy.ui.theme

import androidx.compose.ui.graphics.Color

// Light theme tokens -- strictly from DESIGN.md Section 1.3
object HHColors {
    // Surfaces
    val Background = Color(0xFFF5F7FA)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceElevated = Color(0xFFF0F2F5)
    val SurfaceOverlay = Color(0x0A000000) // rgba(0,0,0,0.04)

    // Borders
    val BorderSubtle = Color(0x0F000000)       // rgba(0,0,0,0.06)
    val BorderStandard = Color(0x1A000000)      // rgba(0,0,0,0.10)
    val BorderActive = Color(0x33000000)        // rgba(0,0,0,0.20)

    // Text
    val TextPrimary = Color(0xFF1A1D23)
    val TextSecondary = Color(0xFF5C6370)
    val TextTertiary = Color(0xFF9CA3AF)
    val TextInverse = Color(0xFFFFFFFF)

    // Altitude accent (Mountain Green)
    val AccentAltitude = Color(0xFF4ECB71)
    val AccentAltitudeDim = Color(0x1F4ECB71)       // rgba(78,203,113,0.12)
    val AccentAltitudeSurface = Color(0x144ECB71)   // rgba(78,203,113,0.08)

    // Speed accent (Sky Blue)
    val AccentSpeed = Color(0xFF4AA8D8)
    val AccentSpeedDim = Color(0x1F4AA8D8)           // rgba(74,168,216,0.12)
    val AccentSpeedSurface = Color(0x144AA8D8)       // rgba(74,168,216,0.08)

    // Semantic
    val AccentActive = Color(0xFF4ECB71)
    val Warning = Color(0xFFD97706)
    val WarningDim = Color(0x1AD97706)
    val WarningSurface = Color(0x14D97706)
    val Success = Color(0xFF16A34A)
    val Error = Color(0xFFDC2626)
    val ErrorDim = Color(0x14DC2626)
    val Info = Color(0xFF2563EB)

    // GPS Signal
    val GpsActive = Color(0xFF4ECB71)
    val GpsWeak = Color(0xFFD97706)
    val GpsPoor = Color(0xFFDC2626)
    val GpsLost = Color(0xFF6B7280)
}
