package com.itsaky.androidide.activities

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import com.airbnb.lottie.LottieAnimationView
import com.itsaky.androidide.R
import com.itsaky.androidide.preferences.internal.GeneralPreferences
import com.itsaky.androidide.utils.resolveAttr
import java.io.File
import java.io.FileInputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("CustomSplashScreen")
class SplashActivity : Activity() {
  private val scope = CoroutineScope(Dispatchers.Main + Job())

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val lottieView =
        LottieAnimationView(this).apply {
          val size = (200 * resources.displayMetrics.density).toInt()
          layoutParams = FrameLayout.LayoutParams(size, size).apply { gravity = Gravity.CENTER }
          repeatCount = 0
        }

    val root =
        FrameLayout(this).apply {
          setBackgroundColor(resolveAttr(R.attr.colorSurface))
          addView(lottieView)
        }
    setContentView(root)

    lottieView.addAnimatorListener(
        object : Animator.AnimatorListener {
          override fun onAnimationStart(animation: Animator) {}

          override fun onAnimationEnd(animation: Animator) {
            goToNextActivity()
          }

          override fun onAnimationCancel(animation: Animator) {
            goToNextActivity()
          }

          override fun onAnimationRepeat(animation: Animator) {}
        }
    )

    scope.launch(Dispatchers.IO) {
      val animationPath = GeneralPreferences.lottieAnimation
      val isDefault = animationPath == GeneralPreferences.DEFAULT_LOTTIE_ANIMATION
      var isCustomValid = false
      var file: File? = null

      if (!isDefault) {
        file = File(animationPath)
        isCustomValid = file.exists() && file.isFile
      }

      withContext(Dispatchers.Main) {
        try {
          if (isCustomValid && file != null) {
            lottieView.setAnimation(FileInputStream(file), file.name)
          } else {
            lottieView.setAnimation(animationPath)
          }
          lottieView.playAnimation()
        } catch (e: Exception) {
          try {
            lottieView.setAnimation(GeneralPreferences.DEFAULT_LOTTIE_ANIMATION)
            lottieView.playAnimation()
          } catch (ex: Exception) {
            goToNextActivity()
          }
        }
      }
    }
  }

  private fun goToNextActivity() {
    startActivity(Intent(this, OnboardingActivity::class.java))
    finish()
  }
}
