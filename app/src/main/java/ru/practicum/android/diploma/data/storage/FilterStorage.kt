package ru.practicum.android.diploma.data.storage

import android.content.Context
import android.content.SharedPreferences
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
        val json = gson.toJson(settings)
        prefs.edit().putString(KEY_FILTER_SETTINGS, json).apply()
    }

    fun loadFilterSettings(): FilterSettings {
        val json = prefs.getString(KEY_FILTER_SETTINGS, null)
        return if (json != null) {
            try {
                gson.fromJson(json, FilterSettings::class.java)
            } catch (e: JsonSyntaxException) {
                android.util.Log.e("FilterStorage", "Failed to parse filter settings", e)
                FilterSettings()
            }
        } else {
            FilterSettings()
        }
    }

    fun clearFilterSettings() {
        prefs.edit().remove(KEY_FILTER_SETTINGS).apply()
    }
}
