package com.sentinela.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sentinela.ui.alert.AlertScreen
import com.sentinela.ui.detail.FireDetailScreen
import com.sentinela.ui.list.FireListScreen
import com.sentinela.ui.map.MapScreen
import com.sentinela.ui.points.PointFormScreen
import com.sentinela.ui.points.PointsScreen

@Composable
fun SentinelaNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Routes.MAP,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(Routes.MAP) {
            MapScreen(
                onOpenFires = { navController.navigate(Routes.FIRE_LIST) },
                onOpenPoints = { navController.navigate(Routes.POINTS) },
                onOpenAlerts = { navController.navigate(Routes.ALERT) },
            )
        }
        composable(Routes.FIRE_LIST) {
            FireListScreen(
                onBack = { navController.popBackStack() },
                onFireClick = { id -> navController.navigate(Routes.fireDetail(id)) },
            )
        }
        composable(
            route = Routes.FIRE_DETAIL,
            arguments = listOf(navArgument("fireId") { type = NavType.StringType }),
        ) { entry ->
            val fireId = entry.arguments?.getString("fireId") ?: return@composable
            FireDetailScreen(
                fireId = fireId,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.POINTS) {
            PointsScreen(
                onBack = { navController.popBackStack() },
                onAdd = { navController.navigate(Routes.pointForm(0)) },
                onEdit = { id -> navController.navigate(Routes.pointForm(id)) },
            )
        }
        composable(
            route = Routes.POINT_FORM,
            arguments = listOf(navArgument("pointId") { type = NavType.LongType }),
        ) { entry ->
            val pointId = entry.arguments?.getLong("pointId") ?: 0L
            PointFormScreen(
                pointId = pointId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(Routes.ALERT) {
            AlertScreen(onBack = { navController.popBackStack() })
        }
    }
}
