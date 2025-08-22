package com.selfbell.home.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.selfbell.home.ui.HistoryScreen // ✅ 새로 만들 HistoryScreen
import com.selfbell.home.ui.HistoryDetailScreen // ✅ 새로 만들 HistoryDetailScreen
import com.selfbell.home.ui.HomeScreen // ✅ 홈 화면

// 홈 모듈 내비게이션 경로 정의
sealed class HomeRoute(val route: String) {
    object History : HomeRoute("history")
    object HistoryDetail : HomeRoute("history_detail/{sessionId}") {
        fun createRoute(sessionId: Long) = "history_detail/$sessionId"
    }
}

// 홈 모듈 내비게이션 그래프
fun NavGraphBuilder.homeGraph(navController: NavController) {
    // 히스토리 목록 화면
    composable(HomeRoute.History.route) {
        HistoryScreen(
            onNavigateToDetail = { sessionId ->
                navController.navigate(HomeRoute.HistoryDetail.createRoute(sessionId))
            }
        )
    }

    // 히스토리 상세 화면
    composable(
        route = HomeRoute.HistoryDetail.route,
        arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
    ) { backStackEntry ->
        val sessionId = backStackEntry.arguments?.getLong("sessionId")
        HistoryDetailScreen(
            sessionId = sessionId,
            onBackClick = { navController.popBackStack() }
        )
    }
}

// 홈 모듈의 메인 내비게이터
@Composable
fun HomeNavigator() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = HomeRoute.History.route // ✅ 시작 화면을 히스토리로 설정
    ) {
        homeGraph(navController)
    }
}