package org.jetbrains.kotlin.reflection.android

object AndroidSupport {
    @JvmStatic
    fun isDalvik(): Boolean = System.getProperty("java.vm.name", "").contains("Dalvik")
}
