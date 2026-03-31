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

/*
 *  This file is part of AndroidIDE.
 *
 *  AndroidIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */

package com.itsaky.androidide.preferences

import android.content.Context
import androidx.preference.Preference
import androidx.preference.SwitchPreference as AndroidXSwitchPreference
import kotlin.reflect.KMutableProperty0

/**
 * A switch preference.
 *
 * @author Akash Yadav
 * @author android_zero
 */
abstract class SwitchPreference
@JvmOverloads
constructor(val setValue: ((Boolean) -> Unit)? = null, val getValue: (() -> Boolean)? = null) :
  BasePreference() {

  constructor(property: KMutableProperty0<Boolean>) : this(property::set, property::get)

  override fun onCreatePreference(context: Context): Preference {
    val pref = AndroidXSwitchPreference(context)
    
    pref.isChecked = prefValue()
    
    return pref
  }

  override fun onPreferenceChanged(preference: Preference, newValue: Any?): Boolean {
    val isChecked = newValue as? Boolean ?: false
    
    setValue?.invoke(isChecked)
    
    if (preference is AndroidXSwitchPreference) {
        preference.isChecked = isChecked
    }
    
    return true
  }

  private fun prefValue(): Boolean {
    return getValue?.invoke() ?: false
  }
}