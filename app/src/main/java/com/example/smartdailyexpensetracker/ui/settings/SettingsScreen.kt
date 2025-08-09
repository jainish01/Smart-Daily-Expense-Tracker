// SettingsScreen.kt
// Displays app settings, mainly theme mode switch, and app version.

package com.example.smartdailyexpensetracker.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Shows app settings like theme mode selector and app version display.
 *
 * @param settingsViewModel Supplies and sets the current app theme mode.
 */
@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel) {
    // Observe theme mode state from ViewModel
    val themeMode by settingsViewModel.themeMode.collectAsState()
    val options = listOf("Light", "Dark", "System")
    // Map themeMode string to selected index in options
    val selectedIndex = when (themeMode) {
        "light" -> 0
        "dark" -> 1
        else -> 2
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("App Settings", style = MaterialTheme.typography.headlineSmall)
        Text("Theme Mode", style = MaterialTheme.typography.titleMedium)
        // Render toggle buttons for choosing the theme
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            options.forEachIndexed { index, label ->
                Button(
                    onClick = { settingsViewModel.setThemeMode(label.lowercase()) },
                    colors = if (selectedIndex == index) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text(label)
                }
            }
        }
        // Show static app version text
        Text("App Version: 1.0.0", style = MaterialTheme.typography.bodyMedium)
    }
}
