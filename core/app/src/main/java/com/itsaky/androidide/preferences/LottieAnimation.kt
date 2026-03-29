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

import android.content.Intent
import com.itsaky.androidide.activities.LottieAnimationSelectorActivity
import com.itsaky.androidide.resources.R.string

const val KEY_LOTTIE_ANIMATION = "idepref_lottie_animation"

val lottieAnimationPreference =
    SimpleClickablePreference(
        key = KEY_LOTTIE_ANIMATION,
        title = string.pref_lottie_animation_title,
        summary = string.pref_lottie_animation_summary,
        icon = R.drawable.ic_setting_lottie_animation,
    ) {
      it.context.startActivity(Intent(it.context, LottieAnimationSelectorActivity::class.java))
      true
    }
