package ru.practicum.android.diploma.data.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import ru.practicum.android.diploma.ui.filter.FilterSettings

class FilterStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "filters_prefs"
        private const val KEY_FILTER_SETTINGS = "filter_settings"
        private const val TAG = "FilterStorage"
        private const val SAVING_MSG = "=== SAVING FILTERS ==="
        private const val LOADING_MSG = "=== LOADING FILTERS ==="
    }

    fun saveFilterSettings(settings: FilterSettings) {
        Log.d(TAG, SAVING_MSG)
        Log.d(TAG, "Settings to save: $settings")
        val json = gson.toJson(settings)
        Log.d(TAG, "JSON: $json")
        prefs.edit()
            .putString(KEY_FILTER_SETTINGS, json)
            .apply()
        Log.d(TAG, "Saved successfully")
    }

    fun loadFilterSettings(): FilterSettings {
        val json = prefs.getString(KEY_FILTER_SETTINGS, null)
        Log.d(TAG, LOADING_MSG)
        Log.d(TAG, "JSON from prefs: $json")
        return if (json != null) {
            try {
                val settings = gson.fromJson(json, FilterSettings::class.java)
                Log.d(TAG, "Loaded settings: $settings")
                settings
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "Error parsing JSON", e)
                FilterSettings()
            }
        } else {
            Log.d(TAG, "No saved settings, returning default")
            FilterSettings()
        }
    }

    fun clearFilterSettings() {
        Log.d(TAG, "Clearing all filters")
        prefs.edit().remove(KEY_FILTER_SETTINGS).apply()
    }
}
