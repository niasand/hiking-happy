package com.happyclaw.hikinghappy.ui.navigation

sealed class Screen(val route: String) {
    data object Instruments : Screen("instruments")
    data object Trends : Screen("trends")
    data object Settings : Screen("settings")
    data object History : Screen("history")
    data object TrackPreview(val sessionId: Long = 0) : Screen("track_preview/{sessionId}") {
        fun createRoute(id: Long) = "track_preview/$id"
    }
}
