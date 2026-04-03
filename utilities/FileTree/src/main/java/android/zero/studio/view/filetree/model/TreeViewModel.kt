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

package android.zero.studio.view.filetree.model

/**
 * @author android_zero
 */
data class Node<T>(
    var value: T,
    var parent: Node<T>? = null,
    var child: List<Node<T>>? = null,
    var isExpand: Boolean = false,
    var level: Int = 0,
    var isSelected: Boolean = false,
    var isHighlighted: Boolean = false
) {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Node<*>

    return value == other.value &&
        parent == other.parent &&
        child == other.child &&
        isExpand == other.isExpand &&
        level == other.level
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }
  
  fun deleteChild(childNode: Node<T>) {
      val currentChildren = child?.toMutableList() ?: return
      currentChildren.remove(childNode)
      child = currentChildren
  }
  
  fun addChild(childNode: Node<T>) {
      val currentChildren = child?.toMutableList() ?: mutableListOf()
      childNode.parent = this
      childNode.level = this.level + 1
      currentChildren.add(childNode)
      child = currentChildren
  }
}

object TreeViewModel {

  // add child node
  fun <T> add(parent: Node<T>, child: List<Node<T>>? = null) {
    // check
    child?.let {
      if (it.isNotEmpty()) parent.isExpand = true
    }

    parent.parent?.let {
      val nodes = it.child
      if (nodes != null && nodes.size == 1 && ((child != null && child.isEmpty()) || child == null)) {
        parent.isExpand = true
      }
    }

    // parent associate with child
    parent.child = child

    child?.forEach {
      it.parent = parent
      it.level = parent.level + 1
    }
  }

  // remove child node
  fun <T> remove(parent: Node<T>, child: List<Node<T>>? = null) {
    parent.child?.let {
      if (it.isNotEmpty()) {
        parent.isExpand = false
      }
    }
    parent.child = null

    child?.forEach { childNode ->
      childNode.parent = null
      childNode.level = 0
      if (childNode.isExpand) {
        childNode.isExpand = false
        childNode.child?.let { listNodes -> remove(childNode, listNodes) }
      }
    }
  }

  private fun <T> getChildren(parent: Node<T>, result: MutableList<Node<T>>): List<Node<T>> {
    parent.child?.let { result.addAll(it) }
    parent.child?.forEach {
      if (it.isExpand) {
        getChildren(it, result)
      }
    }
    return result
  }

  fun <T> getChildren(parent: Node<T>) = getChildren(parent, mutableListOf())
}