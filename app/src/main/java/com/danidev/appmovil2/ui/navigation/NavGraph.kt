package com.danidev.appmovil2.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.danidev.appmovil2.MoverDados
import com.danidev.appmovil2.ui.screens.AboutScreen
import com.danidev.appmovil2.ui.screens.WelcomeScreen

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Main : Screen("main")
    object About : Screen("about")
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
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToAbout = {
                    navController.navigate(Screen.About.route)
                }
            )
        }
        composable(Screen.Main.route) {
            MoverDados()
        }
        composable(Screen.About.route) {
            AboutScreen(onNavigateBack = {
                navController.popBackStack()
            })
        }
    }
}
