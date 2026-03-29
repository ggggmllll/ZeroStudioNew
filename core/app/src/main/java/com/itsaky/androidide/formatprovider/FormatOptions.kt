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
package com.itsaky.androidide.formatprovider

data class FormatOptions(val indentSize: Int = 4, val useTabs: Boolean = false)

data class JavaFormatOptions(val style: Style = Style.AOSP, val organizeImports: Boolean = true) {
  enum class Style {
    AOSP,
    GOOGLE,
  }
}

// NOTE: This class was redeclared in another file.
// The other declaration in 'KotlinFormatOptions.kt' is kept.
//
// data class KotlinFormatOptions(
//    val indentSize: Int = 4,
//    val maxLineLength: Int = 120,
//    val organizeImports: Boolean = true,
//    val useAndroidKdocStyle: Boolean = true
// )
