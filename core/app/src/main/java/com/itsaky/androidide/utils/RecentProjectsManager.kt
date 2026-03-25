package com.itsaky.androidide.utils

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class ProjectHistory(
    val name: String,
    val path: String,
    val timestamp: Long = System.currentTimeMillis(),
    val openCount: Int = 1 // 记录打开次数，用于高频项目(智能预存)
) {
    val letter: String get() = name.take(1).uppercase()
    val color: Color get() {
        val hash = path.hashCode()
        return Color(
            red = (hash and 0xFF0000 shr 16) / 255f * 0.5f + 0.4f,
            green = (hash and 0x00FF00 shr 8) / 255f * 0.5f + 0.4f,
            blue = (hash and 0x0000FF) / 255f * 0.5f + 0.4f,
            alpha = 1f
        )
    }
}

/**
 * 异步历史记录管理器
 * @author android_zero
 */
object RecentProjectsManager {
    private const val PREF_NAME = "recent_projects_prefs_v2"
    private const val KEY_HISTORY = "project_history_json"
    private val gson = Gson()

    private var cachedHistory: MutableList<ProjectHistory>? = null

    /**
     * 异步加载历史记录
     */
    suspend fun getHistoryAsync(context: Context): List<ProjectHistory> = withContext(Dispatchers.IO) {
        cachedHistory?.let { return@withContext it.toList() }
        
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, null)
        
        val list = if (json.isNullOrEmpty()) {
            mutableListOf()
        } else {
            try {
                val type = object : TypeToken<List<ProjectHistory>>() {}.type
                val parsed: List<ProjectHistory> = gson.fromJson(json, type)
                parsed.toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }
        }
        cachedHistory = list
        return@withContext list.toList()
    }

    /**
     * 异步保存项目
     */
    suspend fun addProjectAsync(context: Context, file: File) = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext
        val path = file.absolutePath
        val history = cachedHistory ?: getHistoryAsync(context).toMutableList()

        // 查找是否已存在
        val existingIndex = history.indexOfFirst { it.path == path }
        val newEntry = if (existingIndex != -1) {
            val old = history.removeAt(existingIndex)
            old.copy(timestamp = System.currentTimeMillis(), openCount = old.openCount + 1)
        } else {
            ProjectHistory(name = file.name, path = path)
        }

        // 置顶最新打开的项目
        history.add(0, newEntry)
        
        // 限制最多 25 条
        if (history.size > 25) {
            history.removeAt(history.lastIndex)
        }
        cachedHistory = history

        val json = gson.toJson(history)
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_HISTORY, json)
            .apply()
    }
}