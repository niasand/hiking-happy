package com.happyclaw.hikinghappy.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.happyclaw.hikinghappy.ui.screens.instruments.InstrumentsScreen
import com.happyclaw.hikinghappy.ui.screens.history.HistoryScreen
import com.happyclaw.hikinghappy.ui.screens.history.TrackPreviewScreen
import com.happyclaw.hikinghappy.ui.screens.settings.SettingsScreen
import com.happyclaw.hikinghappy.ui.screens.trends.TrendsScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    onNavigateToSettings: () -> Unit,
    onNavigateBack: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Instruments.route
    ) {
        composable(
            route = Screen.Instruments.route,
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(250)) }
        ) {
            InstrumentsScreen(onSettingsClick = onNavigateToSettings)
        }

        composable(
            route = Screen.Trends.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(300)
                )
            }
        ) {
            TrendsScreen(onSettingsClick = onNavigateToSettings)
        }

        composable(
            route = Screen.Settings.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(250)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(250)
                )
            }
        ) {
            SettingsScreen(
                onBackClick = onNavigateBack,
                onHistoryClick = { navController.navigate(Screen.History.route) }
            )
        }

        composable(
            route = Screen.History.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(250)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(250)
                )
            }
        ) {
            HistoryScreen(
                onBackClick = onNavigateBack,
                onTrackClick = { sessionId ->
                    navController.navigate(Screen.TrackPreview.createRoute(sessionId))
                }
            )
        }

        composable(
            route = Screen.TrackPreview.route,
            arguments = listOf(
                androidx.navigation.navArgument("sessionId") {
                    type = androidx.navigation.NavType.LongType
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(250)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(250)
                )
            }
        ) {
            TrackPreviewScreen(onBackClick = onNavigateBack)
        }
    }
}
