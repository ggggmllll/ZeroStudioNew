package com.itsaky.androidide.lsp.util

import android.util.Log
import com.itsaky.androidide.buildinfo.BuildInfo

/**
 * LSP 框架专用的日志包装类。
 * 适配 AndroidIDE 的日志规范，并解决原生代码中的引用缺失。
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
        if (BuildInfo.DEBUG) {
            Log.d(tag, message)
        }
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