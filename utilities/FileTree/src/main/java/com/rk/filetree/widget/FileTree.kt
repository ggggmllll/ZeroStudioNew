package com.rk.filetree.widget

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rk.filetree.adapters.FileTreeAdapter
import com.rk.filetree.interfaces.FileClickListener
import com.rk.filetree.interfaces.FileIconProvider
import com.rk.filetree.interfaces.FileLongClickListener
import com.rk.filetree.interfaces.FileObject
import com.rk.filetree.model.Node
import com.rk.filetree.provider.DefaultFileIconProvider
import com.rk.filetree.util.Sorter

/**
 * A custom RecyclerView widget that displays a hierarchical file structure. This view allows users
 * to interact with files in a tree-like format, supporting both click and long-click events on
 * individual file nodes.
 *
 * @author android_zero
 */
class FileTree : RecyclerView {

  var fileTreeAdapter: FileTreeAdapter
    private set
  private lateinit var rootFileObject: FileObject

  private var isTreeInitialized = false
  private var isRootNodeVisible: Boolean = true

  // 恢复原有的三个次级构造函数，避免与残留代码发生冲突
  constructor(context: Context) : super(context)
  
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  // Initialization block
  init {
    setItemViewCacheSize(100)
    layoutManager = LinearLayoutManager(context)
    fileTreeAdapter = FileTreeAdapter(context, this)
  }

  /**
   * Sets a custom icon provider to supply icons for different file types.
   *
   * @param fileIconProvider The implementation of the FileIconProvider interface that will provide
   *   icons for file objects.
   */
  fun setIconProvider(fileIconProvider: FileIconProvider) {
    fileTreeAdapter.iconProvider = fileIconProvider
  }

  /**
   * Sets a listener that will be notified when a file node is clicked.
   *
   * @param clickListener The implementation of the FileClickListener interface that will handle
   *   click events on file nodes.
   */
  fun setOnFileClickListener(clickListener: FileClickListener) {
    fileTreeAdapter.onClickListener = clickListener
  }

  /**
   * Sets a listener that will be notified when a file node is long-clicked.
   *
   * @param longClickListener The implementation of the FileLongClickListener interface that will
   *   handle long-click events on file nodes.
   */
  fun setOnFileLongClickListener(longClickListener: FileLongClickListener) {
    fileTreeAdapter.onLongClickListener = longClickListener
  }

  private var init = false
  private var showRootNode: Boolean = true

  /**
   * Loads the file tree starting from the specified root file.
   *
   * @param file The FileObject representing the root directory to be displayed.
   * @param showRootNodeX Optional parameter to determine whether the root node should be displayed.
   *   If null or true, the root node will be shown.
   */
  fun loadFiles(file: FileObject, showRootNodeX: Boolean? = null) {
    rootFileObject = file

    showRootNodeX?.let { isRootNodeVisible = it }

    val nodes: List<Node<FileObject>> =
        if (isRootNodeVisible) {
          mutableListOf<Node<FileObject>>().apply { add(Node(file)) }
        } else {
          Sorter.sort(file)
        }

    if (!isTreeInitialized) {
      if (fileTreeAdapter.iconProvider == null) {
        fileTreeAdapter.iconProvider = DefaultFileIconProvider(context)
      }
      adapter = fileTreeAdapter
      isTreeInitialized = true
    }

    fileTreeAdapter.submitList(nodes)
    if (isRootNodeVisible && nodes.isNotEmpty()) {
      fileTreeAdapter.expandNode(nodes[0])
    }
  }

  fun reloadFileTreeSilently() {
    //记忆滚动状态
    val layoutManager = this.layoutManager as LinearLayoutManager
    val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
    val topOffset = layoutManager.findViewByPosition(firstVisiblePosition)?.top ?: 0
    val savedState = getSaveState()

    //重新加载根节点数据
    val nodes: List<Node<FileObject>> =
        if (isRootNodeVisible) {
          mutableListOf<Node<FileObject>>().apply { add(Node(rootFileObject)) }
        } else {
          Sorter.sort(rootFileObject)
        }

    fileTreeAdapter.submitList(nodes) {
      //恢复展开状态
      restoreState(savedState)
      //恢复 Y 轴滚动位置
      post { layoutManager.scrollToPositionWithOffset(firstVisiblePosition, topOffset) }
    }
  }


  fun expandNode(node: Node<FileObject>) {
    if (!node.isExpand) {
        fileTreeAdapter.expandNode(node)
    }
  }

  fun collapseNode(node: Node<FileObject>) {
    try {
      val method = fileTreeAdapter.javaClass.getDeclaredMethod("collapseNode", Node::class.java)
      method.isAccessible = true
      method.invoke(fileTreeAdapter, node)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun collapseAll() {
    fileTreeAdapter.currentList.filter { it.isExpand }.reversed().forEach { collapseNode(it) }
  }

  fun expandAll() {
    val list = fileTreeAdapter.currentList.toMutableList()
    list.forEach {
      if (it.value.isDirectory() && !it.isExpand) {
        expandNode(it)
      }
    }
  }

  fun getSaveState(): String {
    val sb = java.lang.StringBuilder()
    fileTreeAdapter.currentList.forEach { node ->
      if (node.isExpand) {
        sb.append(node.value.getAbsolutePath()).append(";")
      }
    }
    return sb.toString().trimEnd(';')
  }

  fun restoreState(state: String?) {
    if (state.isNullOrEmpty()) return
    val pathsToExpand = state.split(";").toSet()

    val currentList = fileTreeAdapter.currentList.toList()
    for (node in currentList) {
      if (pathsToExpand.contains(node.value.getAbsolutePath()) && !node.isExpand) {
        expandNode(node)
      }
    }
  }

  /**
   * 精准定位文件位置。通过路径逐级比对并展开父目录，最后滚动到目标。
   */
  fun locateFileAndScroll(targetAbsolutePath: String) {
      post {
          var targetIndex = -1

          var retry = true
          while (retry) {
              retry = false
              val list = fileTreeAdapter.currentList.toList()
              for ((index, node) in list.withIndex()) {
                  val path = node.value.getAbsolutePath()
                  if (path == targetAbsolutePath) {
                      targetIndex = index
                      break
                  }

                  if (targetAbsolutePath.startsWith(path + "/") && !node.isExpand) {
                      expandNode(node)
                      retry = true
                      break
                  }
              }
          }

          if (targetIndex != -1) {
              val lm = layoutManager as LinearLayoutManager
              val offset = (height / 2) // 居中显示
              lm.scrollToPositionWithOffset(targetIndex, offset)
              
              val targetNode = fileTreeAdapter.currentList[targetIndex]
              targetNode.isHighlighted = true
              fileTreeAdapter.notifyItemChanged(targetIndex)
              
              Handler(Looper.getMainLooper()).postDelayed({
                  targetNode.isHighlighted = false
                  val currentPos = fileTreeAdapter.currentList.indexOf(targetNode)
                  if (currentPos != -1) {
                      fileTreeAdapter.notifyItemChanged(currentPos)
                  }
              }, 2500)
          }
      }
  }
}