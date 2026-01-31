/*
 *  This file is part of AndroidIDE.
 *
 *  AndroidIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  AndroidIDE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.itsaky.androidide.ui.themes

import android.app.Activity
import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import com.google.auto.service.AutoService
import com.itsaky.androidide.preferences.internal.GeneralPreferences
import com.itsaky.androidide.utils.isSystemInDarkMode
import com.itsaky.androidide.utils.updateSystemBarColors

/**
 * Theme management for IDE
 *
 * @author Akash Yadav
 * @author android_zero
 */
@Suppress("unused")
@AutoService(IThemeManager::class)
class ThemeManager : IThemeManager {

    private val typedValue = TypedValue()

    override fun applyTheme(activity: Activity) {
        // Force update Night Mode configuration first
        val nightModePref = GeneralPreferences.uiMode
        if (AppCompatDelegate.getDefaultNightMode() != nightModePref) {
            AppCompatDelegate.setDefaultNightMode(nightModePref)
        }

        // Check for Dynamic Color (Material You) preference
        if (GeneralPreferences.isDynamicColorEnabled) {
            // Apply Material You Dynamic Colors
            if (DynamicColors.isDynamicColorAvailable()) {
                DynamicColors.applyToActivityIfAvailable(activity)
                // We return here because DynamicColors handles theme overlay internally
                return 
            }
        }

        // Apply Custom Static Theme
        val theme = getCurrentTheme()
        if (theme == IDETheme.MATERIAL_YOU) {
             // Fallback if MATERIAL_YOU enum is selected but preference flag logic implies system dynamic
             DynamicColors.applyToActivityIfAvailable(activity)
             return
        }

        applyStaticTheme(activity, theme)
    }

    private fun applyStaticTheme(activity: Activity, theme: IDETheme) {
        val isNight = isNightModeActive(activity)
        val styleResId = if (isNight) theme.styleDark else theme.styleLight

        if (styleResId != -1) {
            activity.setTheme(styleResId)
            
            // Hot-Swap: Force update window properties to avoid Activity Recreation
            updateWindowUI(activity, styleResId)
        }
    }
    
    /**
     * Updates the Window UI elements immediately based on the target theme style.
     * This allows for "No-Recreate" visual switching.
     */
    private fun updateWindowUI(activity: Activity, themeResId: Int) {
        val theme = activity.theme
        // Force the theme to apply the new style ID on top of existing
        theme.applyStyle(themeResId, true)

        if (theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)) {
            if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                activity.window.decorView.setBackgroundColor(typedValue.data)
            } else {
                activity.window.decorView.setBackgroundResource(typedValue.resourceId)
            }
        }
        
        var statusBarColor = -1
        if (theme.resolveAttribute(android.R.attr.statusBarColor, typedValue, true)) {
             statusBarColor = typedValue.data
        }
        
        var navBarColor = -1
        if (theme.resolveAttribute(android.R.attr.navigationBarColor, typedValue, true)) {
            navBarColor = typedValue.data
        }
        
        // Apply System Bars
        activity.window.updateSystemBarColors(statusBarColor, navBarColor)
        
        activity.window.decorView.invalidate()
    }

    private fun isNightModeActive(activity: Activity): Boolean {
        val mode = AppCompatDelegate.getDefaultNightMode()
        return when (mode) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> activity.isSystemInDarkMode()
        }
    }

    override fun getCurrentTheme(): IDETheme {
        // Use safeValueOf for allocation-free lookup
        return IDETheme.safeValueOf(GeneralPreferences.selectedTheme)
    }
}