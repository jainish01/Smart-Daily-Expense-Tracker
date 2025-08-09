// SettingsViewModel.kt
// Provides theme mode setting (system/light/dark) persistently via DataStore.

package com.example.smartdailyexpensetracker.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map

/**
 * ViewModel for persistent settings. Currently only theme mode, persisted with DataStore.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    // Defines DataStore for theme settings
    private val Context.dataStore by preferencesDataStore(name = "settings")
    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    private val dataStore = getApplication<Application>().applicationContext.dataStore

    // Exposes theme mode (system/light/dark) to UI
    private val _themeMode = MutableStateFlow("system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    init {
        // Load and observe persisted theme mode
        viewModelScope.launch {
            dataStore.data.map { prefs -> prefs[THEME_MODE_KEY] ?: "system" }
                .collect { mode ->
                    _themeMode.value = mode
                }
        }
    }

    /**
     * Persists new theme mode selection to DataStore.
     */
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[THEME_MODE_KEY] = mode
            }
        }
    }
}
