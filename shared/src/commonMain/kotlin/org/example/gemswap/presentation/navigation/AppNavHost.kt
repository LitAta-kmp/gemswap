package org.example.gemswap.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.example.gemswap.presentation.ui.BattleScreen
import org.example.gemswap.presentation.ui.GameScreen
import org.example.gemswap.presentation.ui.HistoryScreen
import org.example.gemswap.presentation.ui.HomeScreen
import org.example.gemswap.presentation.ui.ResultsScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.Home) {
        composable<Routes.Home> {
            HomeScreen(
                onPlayClicked = { navController.navigate(Routes.Game) },
                onHistoryClicked = { navController.navigate(Routes.History) },
                onBattleClicked = { navController.navigate(Routes.Battle)}
            )
        }
        composable<Routes.History> {
            HistoryScreen(onBackClicked = { navController.popBackStack() })
        }
        composable<Routes.Game> {
            GameScreen(
                onGameFinished = { finalScore ->
                    navController.navigate(Routes.Results(finalScore)) {
                        popUpTo(Routes.Home)
                    }
                }
            )
        }
        composable<Routes.Battle> {
            BattleScreen(
                onHomeClicked = {
                    navController.navigate(Routes.Home) { popUpTo(Routes.Home) { inclusive = true } }
                }
            )
        }
        composable<Routes.Results> { backStackEntry ->
            val results: Routes.Results = backStackEntry.toRoute()
            ResultsScreen(
                score = results.score,
                onPlayAgainClicked = {
                    navController.navigate(Routes.Game) { popUpTo(Routes.Home) }
                },
                onHomeClicked = {
                    navController.navigate(Routes.Home) { popUpTo(Routes.Home) { inclusive = true } }
                }
            )
        }
    }
}