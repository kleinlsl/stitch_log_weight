package com.weighttracker.app.presentation.screens.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    private val context: Context
) {
    private object PreferencesKeys {
        val GOAL_WEIGHT = doublePreferencesKey("goal_weight")
        val HEIGHT = doublePreferencesKey("height")
        val START_WEIGHT = doublePreferencesKey("start_weight")
        val DEFAULT_EXPORT_DIR = stringPreferencesKey("default_export_dir")
    }

    val goalWeight: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.GOAL_WEIGHT] ?: 70.0
    }

    val height: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HEIGHT] ?: 170.0
    }

    val startWeight: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.START_WEIGHT] ?: 80.0
    }

    suspend fun setGoalWeight(weight: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GOAL_WEIGHT] = weight
        }
    }

    suspend fun setHeight(height: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HEIGHT] = height
        }
    }

    suspend fun setStartWeight(weight: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.START_WEIGHT] = weight
        }
    }

    val defaultExportDir: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEFAULT_EXPORT_DIR]
    }

    suspend fun setDefaultExportDir(uriString: String?) {
        context.dataStore.edit { preferences ->
            if (uriString == null) {
                preferences.remove(PreferencesKeys.DEFAULT_EXPORT_DIR)
            } else {
                preferences[PreferencesKeys.DEFAULT_EXPORT_DIR] = uriString
            }
        }
    }
}
