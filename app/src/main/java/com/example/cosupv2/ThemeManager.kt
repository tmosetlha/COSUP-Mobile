package com.example.cosupv2

import android.app.Activity
import android.content.Context

/** Remembers whether the user picked dark mode and applies the matching COSUP theme. */
object ThemeManager {
    private const val PREFS = "cosup_prefs"
    private const val KEY_DARK = "dark_mode"

    fun isDark(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_DARK, false)

    fun setDark(context: Context, dark: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_DARK, dark).apply()
    }

    /** Call before super.onCreate in every activity. */
    fun apply(activity: Activity) {
        activity.setTheme(if (isDark(activity)) R.style.Theme_CosupDark else R.style.Theme_CosupLight)
    }
}
