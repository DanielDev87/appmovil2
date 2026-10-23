package com.danidev.appmovil2.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.danidev.appmovil2.ui.dice.DiceViewModel
import com.danidev.appmovil2.ui.screens.AboutScreen
import com.danidev.appmovil2.ui.screens.GameScreen
import com.danidev.appmovil2.ui.screens.StatsScreen
import com.danidev.appmovil2.ui.screens.WelcomeScreen

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Main : Screen("main")
    object About : Screen("about")
    object Stats : Screen("stats")
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route,
        enterTransition = { fadeIn(animationSpec = tween(500)) },
        exitTransition = { fadeOut(animationSpec = tween(500)) }
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onStartClicked = {
                    navController.navigate(Screen.Main.route)
                },
                onNavigateToAbout = {
                    navController.navigate(Screen.About.route)
                }
            )
        }
        composable(Screen.Main.route) {
            GameScreen(
                onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                onNavigateToMenu = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Stats.route) { entry ->
            // Comparte el ViewModel de la partida para ver sus datos al momento.
            val gameEntry = remember(entry) { navController.getBackStackEntry(Screen.Main.route) }
            StatsScreen(
                diceViewModel = viewModel<DiceViewModel>(gameEntry),
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.About.route) {
            AboutScreen(onNavigateBack = {
                navController.popBackStack()
            })
        }
    }
}
