package android.zero.studio.layouteditor.activities

import android.os.Bundle
import android.zero.studio.layouteditor.BaseActivity
import android.zero.studio.layouteditor.LayoutFile
import android.zero.studio.layouteditor.databinding.ActivityPreviewLayoutBinding
import android.zero.studio.layouteditor.tools.XmlLayoutParser
import android.zero.studio.layouteditor.utils.Constants

class PreviewLayoutActivity : BaseActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val binding = ActivityPreviewLayoutBinding.inflate(layoutInflater)
    setContentView(binding.getRoot())
    @Suppress("DEPRECATION")
    val layoutFile = intent.extras!!.getParcelable<LayoutFile>(Constants.EXTRA_KEY_LAYOUT)
    val parser = XmlLayoutParser(this)
    parser.parseFromXml(layoutFile!!.read(), this)
    binding.getRoot().addView(parser.root)
  }
}
