// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.anmitali.amble.AmbleApplication
import dev.anmitali.amble.service.StepTrackingService
import dev.anmitali.amble.ui.history.HistoryScreen
import dev.anmitali.amble.ui.home.HomeScreen
import dev.anmitali.amble.ui.onboarding.OnboardingScreen
import dev.anmitali.amble.ui.profile.ProfileScreen
import dev.anmitali.amble.ui.settings.SettingsScreen

@Composable
fun AmbleApp(application: AmbleApplication) {
    val profile by application.profileStore.profile.collectAsStateWithLifecycle()

    if (profile == null) {
        OnboardingScreen(application, onFinished = {})
        return
    }

    LaunchedEffect(Unit) {
        StepTrackingService.start(application)
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationSuiteScaffold(
        navigationItems = {
            AmbleDestination.entries.forEach { destination ->
                val selected = currentRoute == destination.route
                NavigationSuiteItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = null,
                        )
                    },
                    label = { Text(destination.label) },
                )
            }
        },
    ) {
        NavHost(navController = navController, startDestination = AmbleDestination.HOME.route) {
            composable(AmbleDestination.HOME.route) { HomeScreen(application) }
            composable(AmbleDestination.HISTORY.route) { HistoryScreen(application) }
            composable(AmbleDestination.PROFILE.route) { ProfileScreen(application) }
            composable(AmbleDestination.SETTINGS.route) { SettingsScreen(application) }
        }
    }
}
