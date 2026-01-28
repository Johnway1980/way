package com.alphadoer.trader.presentation.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import com.alphadoer.trader.presentation.screen.HistoryScreen
import com.alphadoer.trader.presentation.screen.StatisticsScreen
import com.alphadoer.trader.presentation.screen.SettingsScreen
import com.alphadoer.trader.presentation.screen.home.HomeScreen
import com.alphadoer.trader.presentation.screen.morningreading.MorningReadingScreen
import com.alphadoer.trader.presentation.screen.premarketplan.PreMarketPlanScreen
import com.alphadoer.trader.presentation.screen.auctionobservation.AuctionObservationScreen
import com.alphadoer.trader.presentation.screen.review.mistake.MistakeAnalysisScreen
import com.alphadoer.trader.presentation.screen.review.summary.ReviewSummaryScreen
import com.alphadoer.trader.presentation.screen.statistics.StatisticsOverviewScreen
import com.alphadoer.trader.presentation.screen.trading.TradingScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object MorningReading : Screen("morning_reading")
    data object PreMarketPlan : Screen("pre_market_plan")
    data object AuctionObservation : Screen("auction_observation")
    data object Trading : Screen("trading")
    data object PostTradingReview : Screen("post_trading_review")
    data object MistakeAnalysis : Screen("mistake_analysis")
    data object ImprovementPlan : Screen("improvement_plan")
    data object NextDayPrep : Screen("next_day_prep")
    data object ReviewSummary : Screen("review_summary")
    data object History : Screen("history")
    data object Statistics : Screen("statistics")
    data object Settings : Screen("settings")
    data object SectorTracking : Screen("sector_tracking")
}

@Composable
fun AlphaDoerNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route
) {
    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            NavigationBar {
                val items = listOf(
                    Screen.SectorTracking,
                    Screen.ReviewSummary,
                    Screen.Settings
                )
                items.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = { navController.navigate(screen.route) },
                        label = { Text(
                            when (screen) {
                                Screen.SectorTracking -> "板块"
                                Screen.ReviewSummary -> "复盘"
                                Screen.Settings -> "设置"
                                else -> screen.route
                            }
                        ) },
                        icon = {}
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
        ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToStep = { route ->
                    navController.navigate(route)
                }
            )
        }
        composable(Screen.MorningReading.route) { 
            MorningReadingScreen()
        }
        composable(Screen.PreMarketPlan.route) { PreMarketPlanScreen() }
        composable(Screen.AuctionObservation.route) { AuctionObservationScreen() }
        composable(Screen.Trading.route) { TradingScreen() }
        composable(Screen.PostTradingReview.route) { 
            // 占位符：盘后复盘界面，暂时导航到复盘总结
            ReviewSummaryScreen()
        }
        composable(Screen.MistakeAnalysis.route) { MistakeAnalysisScreen() }
        composable(Screen.ImprovementPlan.route) { 
            // 占位符：改进计划界面，暂时导航到复盘总结
            ReviewSummaryScreen()
        }
        composable(Screen.NextDayPrep.route) { 
            // 占位符：次日准备界面，暂时导航到复盘总结
            ReviewSummaryScreen()
        }
        composable(Screen.ReviewSummary.route) { ReviewSummaryScreen() }
        composable(Screen.History.route) { HistoryScreen() }
        composable(Screen.Statistics.route) { StatisticsOverviewScreen() }
        composable(Screen.Settings.route) { 
            SettingsScreen(onNavigateTo = { route -> navController.navigate(route) }) 
        }
        composable(Screen.SectorTracking.route) { 
            com.alphadoer.trader.presentation.screen.sectortracking.SectorTrackingScreen()
        }
        }
    }
}

