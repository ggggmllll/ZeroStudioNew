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

package com.itsaky.androidide.utils

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

/**
 * @author Akash Yadav
 */

// ===================== Android 4.0 - 4.4 区间 =====================
// Android 4.0 (API 14) - ICE_CREAM_SANDWICH
@ChecksSdkIntAtLeast(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
fun isAtLeastICS() = isAtLeast(Build.VERSION_CODES.ICE_CREAM_SANDWICH)

// Android 4.0.3 (API 15) - ICE_CREAM_SANDWICH_MR1
@ChecksSdkIntAtLeast(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
fun isAtLeastICSMR1() = isAtLeast(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)

// Android 4.1 (API 16) - JELLY_BEAN
@ChecksSdkIntAtLeast(Build.VERSION_CODES.JELLY_BEAN)
fun isAtLeastJB() = isAtLeast(Build.VERSION_CODES.JELLY_BEAN)

// Android 4.2 (API 17) - JELLY_BEAN_MR1
@ChecksSdkIntAtLeast(Build.VERSION_CODES.JELLY_BEAN_MR1)
fun isAtLeastJBMR1() = isAtLeast(Build.VERSION_CODES.JELLY_BEAN_MR1)

// Android 4.3 (API 18) - JELLY_BEAN_MR2
@ChecksSdkIntAtLeast(Build.VERSION_CODES.JELLY_BEAN_MR2)
fun isAtLeastJBMR2() = isAtLeast(Build.VERSION_CODES.JELLY_BEAN_MR2)

// Android 4.4 (API 19) - KITKAT
@ChecksSdkIntAtLeast(Build.VERSION_CODES.KITKAT)
fun isAtLeastKK() = isAtLeast(Build.VERSION_CODES.KITKAT)

// Android 4.4W (API 20) - KITKAT_WATCH
@ChecksSdkIntAtLeast(Build.VERSION_CODES.KITKAT_WATCH)
fun isAtLeastKKWatch() = isAtLeast(Build.VERSION_CODES.KITKAT_WATCH)

// ===================== Android 5.0 - 5.1 区间 =====================
// Android 5.0 (API 21) - LOLLIPOP
@ChecksSdkIntAtLeast(Build.VERSION_CODES.LOLLIPOP)
fun isAtLeastL() = isAtLeast(Build.VERSION_CODES.LOLLIPOP)

// Android 5.1 (API 22) - LOLLIPOP_MR1
@ChecksSdkIntAtLeast(Build.VERSION_CODES.LOLLIPOP_MR1)
fun isAtLeastLMR1() = isAtLeast(Build.VERSION_CODES.LOLLIPOP_MR1)

// ===================== Android 6.0 - 6.0 区间 =====================
// Android 6.0 (API 23) - M
@ChecksSdkIntAtLeast(Build.VERSION_CODES.M)
fun isAtLeastM() = isAtLeast(Build.VERSION_CODES.M)

// ===================== Android 7.0 - 7.1 区间 =====================
// Android 7.0 (API 24) - N
@ChecksSdkIntAtLeast(Build.VERSION_CODES.N)
fun isAtLeastN() = isAtLeast(Build.VERSION_CODES.N)

// Android 7.1 (API 25) - N_MR1
@ChecksSdkIntAtLeast(Build.VERSION_CODES.N_MR1)
fun isAtLeastNMR1() = isAtLeast(Build.VERSION_CODES.N_MR1)

// ===================== Android 8.0 - 8.1 区间 =====================
// Android 8.0 (API 26) - O
@ChecksSdkIntAtLeast(Build.VERSION_CODES.O)
fun isAtLeastO() = isAtLeast(Build.VERSION_CODES.O)

// Android 8.1 (API 27) - O_MR1
@ChecksSdkIntAtLeast(Build.VERSION_CODES.O_MR1)
fun isAtLeastOMR1() = isAtLeast(Build.VERSION_CODES.O_MR1)

// ===================== Android 9.0 - 16.0 区间 =====================
// Android 9 (API 28) - P
@ChecksSdkIntAtLeast(Build.VERSION_CODES.P)
fun isAtLeastP() = isAtLeast(Build.VERSION_CODES.P)

// Android 10 (API 29) - Q
@ChecksSdkIntAtLeast(Build.VERSION_CODES.Q)
fun isAtLeastQ() = isAtLeast(Build.VERSION_CODES.Q)

// Android 11 (API 30) - R
@ChecksSdkIntAtLeast(Build.VERSION_CODES.R)
fun isAtLeastR() = isAtLeast(Build.VERSION_CODES.R)

// Android 12 (API 31) - S
@ChecksSdkIntAtLeast(Build.VERSION_CODES.S)
fun isAtLeastS() = isAtLeast(Build.VERSION_CODES.S)

// Android 12L (API 32) - S_V2
@ChecksSdkIntAtLeast(Build.VERSION_CODES.S_V2)
fun isAtLeastSV2() = isAtLeast(Build.VERSION_CODES.S_V2)

// Android 13 (API 33) - TIRAMISU
@ChecksSdkIntAtLeast(Build.VERSION_CODES.TIRAMISU)
fun isAtLeastT() = isAtLeast(Build.VERSION_CODES.TIRAMISU)

// Android 14 (API 34) - UPSIDE_DOWN_CAKE
@ChecksSdkIntAtLeast(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun isAtLeastU() = isAtLeast(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)

// Android 15 (API 35) - VANILLA_ICE_CREAM
@ChecksSdkIntAtLeast(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun isAtLeastV() = isAtLeast(Build.VERSION_CODES.VANILLA_ICE_CREAM)

// Android 16 (API 36) - BAKLAVA :: Build.VERSION_CODES.BAKLAVA
@ChecksSdkIntAtLeast(36)
fun isAtLeastBaklava() = isAtLeast(36)


@ChecksSdkIntAtLeast(parameter = 0)
fun isAtLeast(version: Int): Boolean = Build.VERSION.SDK_INT >= version
