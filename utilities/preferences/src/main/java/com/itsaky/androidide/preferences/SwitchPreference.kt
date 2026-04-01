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
package com.itsaky.androidide.preferences

import android.content.Context
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.materialswitch.MaterialSwitch
import kotlin.reflect.KMutableProperty0

/**
 * A switch preference
 *
 * @author Akash Yadav
 * @author android_zero
 */
abstract class SwitchPreference
@JvmOverloads
constructor(val setValue: ((Boolean) -> Unit)? = null, val getValue: (() -> Boolean)? = null) :
    BasePreference() {

  constructor(property: KMutableProperty0<Boolean>) : this(property::set, property::get)

  private var currentChecked = false

  override fun onCreatePreference(context: Context): Preference {
    return MaterialSwitchPreference(context)
  }

  override fun onBindProperty(preference: Preference) {
    currentChecked = getValue?.invoke() ?: false
  }

  private fun updateCheckedState(preference: Preference, isChecked: Boolean) {
    if (currentChecked != isChecked) {
      currentChecked = isChecked
      setValue?.invoke(isChecked)
      if (preference is MaterialSwitchPreference) {
        preference.updateView()
      }
    }
  }

  override fun onPreferenceChanged(preference: Preference, newValue: Any?): Boolean {
    val isChecked = newValue as? Boolean ?: return false
    updateCheckedState(preference, isChecked)
    return true
  }

  override fun onPreferenceClick(preference: Preference): Boolean {
    updateCheckedState(preference, !currentChecked)
    return true
  }

  private inner class MaterialSwitchPreference(context: Context) : Preference(context) {
    init {
      isPersistent = false
      widgetLayoutResource =
          com.itsaky.androidide.preferences.R.layout.preference_widget_material_switch
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
      super.onBindViewHolder(holder)

      val switch =
          holder.findViewById(com.itsaky.androidide.preferences.R.id.preference_switch)
              as? MaterialSwitch ?: return

      switch.setOnCheckedChangeListener(null)
      switch.isChecked = currentChecked

      switch.setOnCheckedChangeListener { _, isChecked -> updateCheckedState(this, isChecked) }
    }

    fun updateView() {
      notifyChanged()
    }
  }
}
