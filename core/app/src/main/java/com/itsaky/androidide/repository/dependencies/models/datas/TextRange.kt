package com.itsaky.androidide.repository.dependencies.models.datas

import com.itsaky.androidide.repository.dependencies.models.enums.*
import com.itsaky.androidide.repository.dependencies.models.interfaces.*

/**
 * 文本范围的物理索引坐标。
 *
 * <p>
 * <b>核心设计：</b>通过记录 AST（抽象语法树）解析出的绝对字符偏移量， 允许底层的 <code>RandomAccessFile</code> 进行 <b>零风险</b>
 * 的字节流定点替换， 彻底杜绝正则表达式在复杂换行、注释干扰下造成的误杀。 </p>
 *
 * @property startOffset 起始字符偏移量（包含）
 * @property endOffset 结束字符偏移量（不包含）
 */
data class TextRange(val startOffset: Int, val endOffset: Int) {
  /** 获取该文本片段的总长度 */
  val length: Int
    get() = endOffset - startOffset

  /** 检查该范围是否有效/非空 */
  fun isValid(): Boolean = startOffset >= 0 && endOffset > startOffset
}
