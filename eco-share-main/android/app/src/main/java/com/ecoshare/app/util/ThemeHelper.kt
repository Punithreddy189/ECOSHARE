package com.ecoshare.app.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {
    private const val PREFS_NAME = "ecoshare_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    const val MODE_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    const val MODE_LIGHT = AppCompatDelegate.MODE_NIGHT_NO
    const val MODE_DARK = AppCompatDelegate.MODE_NIGHT_YES

    fun applyTheme(context: Context) {
        val mode = getThemeMode(context)
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun setThemeMode(context: Context, mode: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun getThemeMode(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_THEME_MODE, MODE_SYSTEM)
    }

    fun getThemeModeLabel(mode: Int): String {
        return when (mode) {
            MODE_LIGHT -> "Light"
            MODE_DARK -> "Dark"
            else -> "System default"
        }
    }
}
