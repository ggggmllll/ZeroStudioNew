package android.zero.studio.layouteditor.tools

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.zero.studio.layouteditor.editor.initializer.AttributeInitializer
import android.zero.studio.layouteditor.editor.initializer.AttributeMap
import android.zero.studio.layouteditor.managers.IdManager.addNewId
import android.zero.studio.layouteditor.managers.IdManager.clear
import android.zero.studio.layouteditor.utils.Constants
import android.zero.studio.layouteditor.utils.FileUtil
import android.zero.studio.layouteditor.utils.InvokeUtil.createView
import android.zero.studio.layouteditor.utils.InvokeUtil.invokeMethod
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.io.StringReader
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory

class XmlLayoutParser(context: Context) {

  val viewAttributeMap: HashMap<View, AttributeMap> = HashMap()

  private val initializer: AttributeInitializer
  private val container: LinearLayoutCompat

  init {
    val attributes =
        Gson()
            .fromJson<HashMap<String, List<HashMap<String, Any>>>>(
                FileUtil.readFromAsset(Constants.ATTRIBUTES_FILE, context),
                object : TypeToken<HashMap<String, List<HashMap<String, Any>>>>() {}.type,
            )
    val parentAttributes =
        Gson()
            .fromJson<HashMap<String, List<HashMap<String, Any>>>>(
                FileUtil.readFromAsset(Constants.PARENT_ATTRIBUTES_FILE, context),
                object : TypeToken<HashMap<String, List<HashMap<String, Any>>>>() {}.type,
            )

    initializer = AttributeInitializer(context, attributes, parentAttributes)

    container = LinearLayoutCompat(context)
    container.layoutParams =
        ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
  }

  val root: View
    get() {
      if (container.childCount > 0) {
        val view = container.getChildAt(0)
        container.removeView(view)
        return view
      }
      return container // Return container if empty to avoid crash
    }

  fun parseFromXml(xml: String, context: Context) {

    if (xml.trim().isEmpty()) {
      return
    }

    val listViews: MutableList<View> = ArrayList()
    listViews.add(container)

    try {
      val factory = XmlPullParserFactory.newInstance()
      val parser = factory.newPullParser()
      parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
      parser.setInput(StringReader(xml))

      while (parser.eventType != XmlPullParser.END_DOCUMENT) {
        when (parser.eventType) {
          XmlPullParser.START_TAG -> {
            val viewObj = createView(parser.name, context)

            if (viewObj is View) {
              val view = viewObj
              listViews.add(view)

              val map = AttributeMap()

              var i = 0
              while (i < parser.attributeCount) {
                if (!parser.getAttributeName(i).startsWith("xmlns")) {
                  map.putValue(parser.getAttributeName(i), parser.getAttributeValue(i))
                }
                i++
              }

              viewAttributeMap[view] = map
            } else {
              Log.e("XmlLayoutParser", "Failed to create view for tag: ${parser.name}")
            }
          }

          XmlPullParser.END_TAG -> {

            if (listViews.size > 1) {
              val index = parser.depth

              if (index < listViews.size) {
                (listViews[index - 1] as ViewGroup).addView(listViews[index])
                listViews.removeAt(index)
              }
            }
          }
        }
        parser.next()
      }
    } catch (e: XmlPullParserException) {
      e.printStackTrace()
    } catch (e: IOException) {
      e.printStackTrace()
    } catch (e: Exception) {
      e.printStackTrace()
    }

    clear()

    for (view in viewAttributeMap.keys) {
      val map = viewAttributeMap[view]!!

      for (key in map.keySet()) {
        if (key == "android:id") {
          addNewId(view, map.getValue("android:id"))
        }
      }
    }

    for (view in viewAttributeMap.keys) {
      val map = viewAttributeMap[view]!!
      applyAttributes(view, map)
    }
  }

  private fun applyAttributes(target: View, attributeMap: AttributeMap) {
    val allAttrs = initializer.getAllAttributesForView(target)

    val keys = attributeMap.keySet()

    for (i in keys.indices.reversed()) {
      val key = keys[i]

      val attr = initializer.getAttributeFromKey(key, allAttrs) ?: continue
      val methodName = attr[Constants.KEY_METHOD_NAME].toString()
      val className = attr[Constants.KEY_CLASS_NAME].toString()
      val value = attributeMap.getValue(key)

      if (key == "android:id") {
        continue
      }

      invokeMethod(methodName, className, target, value, target.context)
    }
  }
}
