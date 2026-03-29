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

package com.itsaky.androidide.lsp.util

import android.util.Log

/**
 * 简单日志包装器。
 *
 * @author android_zero
 */
class Logger private constructor(private val tag: String) {

  companion object {
    fun instance(tag: String): Logger {
      return Logger(tag)
    }
  }

  fun info(message: String) {
    Log.i(tag, message)
  }

  fun debug(message: String) {
    Log.d(tag, message)
  }

  fun warn(message: String) {
    Log.w(tag, message)
  }

  fun error(message: String, throwable: Throwable? = null) {
    if (throwable != null) {
      Log.e(tag, message, throwable)
    } else {
      Log.e(tag, message)
    }
  }
}
