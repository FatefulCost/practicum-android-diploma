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
    }

    fun saveFilterSettings(settings: FilterSettings) {
        Log.d("FilterStorage", "=== SAVING FILTERS ===")
        Log.d("FilterStorage", "Settings to save: $settings")
        val json = gson.toJson(settings)
        Log.d("FilterStorage", "JSON: $json")
        prefs.edit()
            .putString(KEY_FILTER_SETTINGS, json)
            .apply()
        Log.d("FilterStorage", "Saved successfully")
    }

    fun loadFilterSettings(): FilterSettings {
        val json = prefs.getString(KEY_FILTER_SETTINGS, null)
        Log.d("FilterStorage", "=== LOADING FILTERS ===")
        Log.d("FilterStorage", "JSON from prefs: $json")
        return if (json != null) {
            try {
                val settings = gson.fromJson(json, FilterSettings::class.java)
                Log.d("FilterStorage", "Loaded settings: $settings")
                settings
            } catch (e: JsonSyntaxException) {
                Log.e("FilterStorage", "Error parsing JSON", e)
                FilterSettings()
            } catch (e: Exception) {
                Log.e("FilterStorage", "Unexpected error", e)
                FilterSettings()
            }
        } else {
            Log.d("FilterStorage", "No saved settings, returning default")
            FilterSettings()
        }
    }

    fun clearFilterSettings() {
        Log.d("FilterStorage", "Clearing all filters")
        prefs.edit().remove(KEY_FILTER_SETTINGS).apply()
    }
}
