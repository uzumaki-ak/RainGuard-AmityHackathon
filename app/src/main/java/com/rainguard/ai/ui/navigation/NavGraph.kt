package com.rainguard.ai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rainguard.ai.ui.screens.alerts.AlertsScreen
import com.rainguard.ai.ui.screens.auth.RoleSelectionScreen
import com.rainguard.ai.ui.screens.authority.AllReportsScreen
import com.rainguard.ai.ui.screens.authority.AuthorityDashboardScreen
import com.rainguard.ai.ui.screens.chat.ChatAssistantScreen
import com.rainguard.ai.ui.screens.home.HomeMapScreen
import com.rainguard.ai.ui.screens.onboarding.OnboardingScreen
import com.rainguard.ai.ui.screens.report.ReportScreen
import com.rainguard.ai.ui.screens.route.ARNavigationScreen
import com.rainguard.ai.ui.screens.route.EvacuationRouteScreen
import com.rainguard.ai.ui.screens.settings.SettingsScreen
import com.rainguard.ai.ui.screens.shelter.ShelterDetailsScreen
import com.rainguard.ai.ui.screens.splash.SplashScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoutes.SPLASH
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(navController = navController)
        }

        composable(NavRoutes.ONBOARDING) {
            OnboardingScreen(navController = navController)
        }

        composable(NavRoutes.LOGIN_ROLE) {
            RoleSelectionScreen(navController = navController)
        }

        composable(NavRoutes.HOME_MAP) {
            HomeMapScreen(navController = navController)
        }

        composable(
            route = NavRoutes.EVACUATION_ROUTE,
            arguments = listOf(navArgument("routeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId") ?: return@composable
            EvacuationRouteScreen(
                routeId = routeId,
                navController = navController
            )
        }

        composable(
            route = NavRoutes.AR_NAVIGATION,
            arguments = listOf(navArgument("routeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId") ?: return@composable
            ARNavigationScreen(
                routeId = routeId,
                navController = navController
            )
        }

        composable(NavRoutes.ALERTS) {
            AlertsScreen(navController = navController)
        }

        composable(NavRoutes.REPORT) {
            ReportScreen(navController = navController)
        }

        composable(NavRoutes.CHAT) {
            ChatAssistantScreen(navController = navController)
        }

        composable(
            route = NavRoutes.SHELTER,
            arguments = listOf(navArgument("shelterId") { type = NavType.StringType })
        ) { backStackEntry ->
            val shelterId = backStackEntry.arguments?.getString("shelterId") ?: return@composable
            ShelterDetailsScreen(
                shelterId = shelterId,
                navController = navController
            )
        }

        composable(NavRoutes.AUTHORITY_DASHBOARD) {
            AuthorityDashboardScreen(navController = navController)
        }

        composable(NavRoutes.ALL_REPORTS) {
            AllReportsScreen(navController = navController)
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(navController = navController)
        }
    }
}