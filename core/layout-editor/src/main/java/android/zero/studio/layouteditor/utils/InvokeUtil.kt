package android.zero.studio.layouteditor.utils

import android.content.Context
import android.view.View
import android.zero.studio.layouteditor.R.mipmap
import java.lang.reflect.InvocationTargetException

/** @author android_zero (Added class name resolution) */
object InvokeUtil {
  @JvmStatic
  fun createView(className: String, context: Context): Any? {

    val classNamesToTry =
        if (!className.contains(".")) {
          arrayOf(
              "android.widget.$className",
              "android.view.$className",
              "android.webkit.$className",
          )
        } else {
          arrayOf(className)
        }

    for (name in classNamesToTry) {
      try {
        val clazz = Class.forName(name)
        val constructor = clazz.getConstructor(Context::class.java)
        return constructor.newInstance(context)
      } catch (e: ClassNotFoundException) {
        // Ignore and try next
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    return null
  }

  @JvmStatic
  fun invokeMethod(
      methodName: String,
      className: String,
      target: View,
      value: String,
      context: Context,
  ) {
    try {
      val clazz = Class.forName("android.zero.studio.layouteditor.editor.callers.$className")
      val method =
          clazz.getMethod(methodName, View::class.java, String::class.java, Context::class.java)
      method.invoke(clazz, target, value, context)
    } catch (e: ClassNotFoundException) {
      e.printStackTrace()
    } catch (e: NoSuchMethodException) {
      e.printStackTrace()
    } catch (e: InvocationTargetException) {
      e.printStackTrace()
    } catch (e: IllegalAccessException) {
      e.printStackTrace()
    }
  }

  @JvmStatic
  fun getMipmapId(name: String): Int {
    try {
      val cls = mipmap::class.java
      val field = cls.getField(name)
      return field.getInt(cls)
    } catch (e: NoSuchFieldException) {
      e.printStackTrace()
    } catch (e: IllegalAccessException) {
      e.printStackTrace()
    }

    return 0
  }

  @JvmStatic
  fun getSuperClassName(clazz: String): String? {
    return try {
      Class.forName(clazz).superclass.name
    } catch (e: ClassNotFoundException) {
      null
    }
  }
}
