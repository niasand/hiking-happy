package com.happyclaw.hikinghappy.ui.navigation

sealed class Screen(val route: String) {
    data object Instruments : Screen("instruments")
    data object Trends : Screen("trends")
    data object Settings : Screen("settings")
}
