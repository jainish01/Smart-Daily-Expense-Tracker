// MainActivity.kt
// Entry point of the app. Handles bottom navigation and high-level theming.

package com.example.smartdailyexpensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartdailyexpensetracker.ui.expenseentry.ExpenseEntryScreen
import com.example.smartdailyexpensetracker.ui.expenselist.ExpenseListScreen
import com.example.smartdailyexpensetracker.ui.reports.ReportScreen
import com.example.smartdailyexpensetracker.ui.settings.SettingsScreen
import com.example.smartdailyexpensetracker.ui.settings.SettingsViewModel
import com.example.smartdailyexpensetracker.ui.theme.SmartDailyExpenseTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity is the entry point of the SmartDailyExpenseTracker app.
 * It manages the app's theme, main navigation (bottom bar), and hosts the four primary screens.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Obtain our theme selection (system/dark/light) from the SettingsViewModel (using Hilt DI)
            val settingsViewModel: SettingsViewModel = viewModel()
            val themeMode by settingsViewModel.themeMode.collectAsState()
            // Determine whether to use dark theme based on user settings or system preference
            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            // Top-level theme wrapper for the app
            SmartDailyExpenseTrackerTheme(darkTheme = darkTheme) {
                var selectedTab by remember { mutableIntStateOf(0) } // Bottom nav selection: Home, Add, Reports, or Settings

                // Scaffold provides the layout structure and bottom navigation bar
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            // Home Tab
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Home") }
                            )
                            // Add Expense Tab
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.AddCircle, contentDescription = "Add") },
                                label = { Text("Add") }
                            )
                            // Reports Tab
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = {
                                    Icon(
                                        Icons.Default.Build,
                                        contentDescription = "Reports"
                                    )
                                },
                                label = { Text("Reports") }
                            )
                            // Settings Tab
                            NavigationBarItem(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                icon = {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = "Settings"
                                    )
                                },
                                label = { Text("Settings") }
                            )
                        }
                    }
                ) { paddingValues ->
                    Box(Modifier.padding(paddingValues)) {
                        // Control which screen is displayed based on the selected tab
                        when (selectedTab) {
                            0 -> ExpenseListScreen()     // Main expense list
                            1 -> ExpenseEntryScreen()    // Add expense
                            2 -> ReportScreen()          // Reports and charts
                            3 -> SettingsScreen(settingsViewModel) // App settings
                        }
                    }
                }
            }
        }
    }
}