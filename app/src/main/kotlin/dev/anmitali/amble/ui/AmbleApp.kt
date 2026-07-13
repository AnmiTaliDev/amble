// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            NavigationBar {
                AmbleDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AmbleDestination.HOME.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(AmbleDestination.HOME.route) { HomeScreen(application) }
            composable(AmbleDestination.HISTORY.route) { HistoryScreen(application) }
            composable(AmbleDestination.PROFILE.route) { ProfileScreen(application) }
            composable(AmbleDestination.SETTINGS.route) { SettingsScreen(application) }
        }
    }
}
