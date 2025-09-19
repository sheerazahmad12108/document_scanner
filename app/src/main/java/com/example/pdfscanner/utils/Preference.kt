package com.example.pdfscanner.utils

import android.content.Context
import android.content.SharedPreferences

class Preference (context: Context) {

    private val preferenceName = "CT_APP_PREFS"
    private val preference: SharedPreferences =
        context.getSharedPreferences("CT_APP_PREFS", Context.MODE_PRIVATE)



    fun putBoolean(key: String, value: Boolean) {
        preference.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return preference.getBoolean(key, defaultValue)
    }



    fun putString(key: String, value: String) {
        preference.edit().putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String = ""): String? {
        return preference.getString(key, defaultValue)
    }

    fun putInt(key: String, value: Int) {
        preference.edit().putInt(key, value).apply()
    }

    fun getInt(key: String, value: Int = 0): Int {
        return preference.getInt(key, value)
    }

 fun saveStringSet(context: Context, key: String, data: Set<String?>) {
        preference.edit().putStringSet(key, data).apply()
    }

    // Retrieve a Set<String> from SharedPreferences
    fun getStringSet(context: Context, key: String): Set<String> {

        return preference.getStringSet(key, emptySet()) ?: emptySet()
    }

    fun clearCache(context: Context, key: String) {
        val sharedPreferences = context.getSharedPreferences("AppCache", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(key).apply()
    }
}