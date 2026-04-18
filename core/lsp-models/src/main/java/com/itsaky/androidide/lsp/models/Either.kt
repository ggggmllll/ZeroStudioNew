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
/*
 *  @author android_zero
 */
package com.itsaky.androidide.lsp.models

/**
 * 联合类型容器。
 * LSP 协议中经常出现 "A | B" 的数据结构（例如 Hover 内容可以是 String 或者 MarkupContent）。
 * 
 * @param L 左侧类型
 * @param R 右侧类型
 */
data class Either<L, R>(
    val left: L? = null,
    val right: R? = null
) {
    val isLeft: Boolean get() = left != null
    val isRight: Boolean get() = right != null

    /**
     * 根据当前持有的类型执行映射转换
     */
    fun <T> map(lFunc: (L) -> T, rFunc: (R) -> T): T {
        return if (isLeft) {
            lFunc(left!!)
        } else if (isRight) {
            rFunc(right!!)
        } else {
            throw IllegalStateException("Both left and right are null in Either")
        }
    }

    /**
     * 根据当前持有的类型消费数据
     */
    fun consume(lFunc: (L) -> Unit, rFunc: (R) -> Unit) {
        if (isLeft) {
            lFunc(left!!)
        } else if (isRight) {
            rFunc(right!!)
        }
    }

    companion object {
        @JvmStatic
        fun <L, R> forLeft(left: L): Either<L, R> = Either(left = left)

        @JvmStatic
        fun <L, R> forRight(right: R): Either<L, R> = Either(right = right)
    }
}