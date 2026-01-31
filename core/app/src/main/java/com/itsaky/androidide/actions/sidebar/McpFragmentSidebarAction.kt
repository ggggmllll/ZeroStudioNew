package com.itsaky.androidide.actions.sidebar

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.itsaky.androidide.R
import android.zero.studio.chatai.server.mcp.McpFragment
import kotlin.reflect.KClass
import com.itsaky.androidide.utils.getTintedDrawable 

/**
 * 侧边栏操作，用于显示数据文件树。
 *
 * @author android_zero
 */
class McpFragmentSidebarAction(context: Context, override val order: Int) : AbstractSidebarAction() {

  companion object {
    const val ID ="ide.editor.sidebar.mcpserver"
  }

  override val id: String = ID
  override val fragmentClass: KClass<out Fragment> = McpFragment::class

  init {
    // 设置标题
    label = context.getString(R.string.title_mcp_server)
        // 设置图标
    icon = ContextCompat.getDrawable(context, R.drawable.ic_ai_mcp_server)
  }
}
