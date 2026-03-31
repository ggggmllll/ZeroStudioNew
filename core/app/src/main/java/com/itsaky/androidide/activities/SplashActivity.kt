package com.itsaky.androidide.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp

/**
 * Splash screen activity that displays an animation upon app launch.
 * This activity uses Jetpack Compose for its UI.
 *
 * @author android_zero (One-to-one UI restoration & Performance optimization)
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ZeroStudioUltraFastSplash {
                startNextActivity()
            }
        }
    }

    private fun startNextActivity() {
        startActivity(Intent(this, OnboardingActivity::class.java))
        finish()
        overridePendingTransition(0, 0)
    }
}

/**
 * A composable function that displays the ZeroStudio splash screen animation.
 *
 * @param onFinished A callback lambda that is invoked when the animation completes.
 *
 * This composable uses a single `Animatable` float value to drive a complex,
 * multi-stage animation drawn on a `Canvas`.
 *
 * Animation Stages:
 * - 0.0s to 0.48s (p=0.0-0.4): Left bracket `<` appears.
 * - 0.24s to 0.84s (p=0.2-0.7): Android robot rises from the bottom.
 * - 0.48s to 0.96s (p=0.4-0.8): Right bracket `>` appears.
 * - 0.72s to 1.2s  (p=0.6-1.0): "ZeroStudio" text appears character by character.
 */
@Composable
fun ZeroStudioUltraFastSplash(onFinished: () -> Unit) {
    // 动画状态：0f 到 1f
    val animState = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        // 总时长 1.2s
        animState.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
        onFinished()
    }

    val p = animState.value

    Canvas(modifier = Modifier.fillMaxSize().background(Color.White)) {
        val cx = size.width / 2
        val cy = size.height / 2
        
        // 整体缩小约 15%
        val scale = 0.6375f 

        // 左括号 < (阶段 0.0 - 0.4)
        if (p > 0f) {
            val alpha = (p / 0.4f).coerceIn(0f, 1f)
            drawBracket(cx, cy, isLeft = true, progress = alpha, scale = scale)
        }

        // 机器人从中间升起 (阶段 0.2 - 0.7)
        if (p > 0.2f) {
            val robotP = ((p - 0.2f) / 0.5f).coerceIn(0f, 1f)
            drawAndroidRobot(cx, cy, progress = robotP, scale = scale)
        }

        // 右括号 > (阶段 0.4 - 0.8)
        if (p > 0.4f) {
            val alpha = ((p - 0.4f) / 0.4f).coerceIn(0f, 1f)
            drawBracket(cx, cy, isLeft = false, progress = alpha, scale = scale)
        }

        // "ZeroStudio" 逐字出现 (阶段 0.6 - 1.0)
        if (p > 0.6f) {
            val textP = ((p - 0.6f) / 0.4f).coerceIn(0f, 1f)
            drawZeroStudioText(cx, cy + (280f * scale), progress = textP, scale = scale)
        }
    }
}

/**
 * A [DrawScope] extension function to draw the left or right bracket of the logo.
 * @param cx The horizontal center of the canvas.
 * @param cy The vertical center of the canvas.
 * @param isLeft True to draw the left bracket, false for the right.
 * @param progress The animation progress (0.0 to 1.0) for the alpha transparency.
 * @param scale A scaling factor for size and position.
 */
private fun DrawScope.drawBracket(cx: Float, cy: Float, isLeft: Boolean, progress: Float, scale: Float) {
    val offsetX = if (isLeft) -320f * scale else 320f * scale
    
    // 左侧：深蓝过渡到浅蓝/白； 右侧：亮绿过渡到深绿
    val brush = if (isLeft) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF1E88E5), Color(0xFF81D4FA)), // 蓝色渐变
            start = Offset(cx + offsetX, cy - 150f),
            end = Offset(cx + offsetX, cy + 150f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFF00E676), Color(0xFF00B0FF)), // 亮绿渐变
            start = Offset(cx + offsetX, cy - 150f),
            end = Offset(cx + offsetX, cy + 150f)
        )
    }

    val path = Path().apply {
        if (isLeft) {
            moveTo(cx + offsetX + 80f * scale, cy - 180f * scale)
            lineTo(cx + offsetX - 100f * scale, cy)
            lineTo(cx + offsetX + 80f * scale, cy + 180f * scale)
        } else {
            moveTo(cx + offsetX - 80f * scale, cy - 180f * scale)
            lineTo(cx + offsetX + 100f * scale, cy)
            lineTo(cx + offsetX - 80f * scale, cy + 180f * scale)
        }
    }

    drawPath(
        path = path,
        brush = brush,
        alpha = progress,
        style = Stroke(width = 95f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

/**
 * A [DrawScope] extension function to draw the Android robot logo.
 * @param cx The horizontal center of the canvas.
 * @param cy The vertical center of the canvas.
 * @param progress The animation progress (0.0 to 1.0) for the reveal and movement.
 * @param scale A scaling factor for size and position.
 */
private fun DrawScope.drawAndroidRobot(cx: Float, cy: Float, progress: Float, scale: Float) {
    val robotColor = Color(0xFF3DDC84)
    val headRadius = 165f * scale
    // 升起动画，Y轴偏移。将基准中心线下移，与括号视觉重心保持一致
    val baseCy = cy + (60f * scale) 
    val yOffset = (1 - progress) * 100f 

    // 机器人头部使用垂直渐变，底部略微深色透明过渡
    val robotBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF00E676), Color(0xFF1B5E20)),
        startY = baseCy - headRadius + yOffset,
        endY = baseCy + yOffset
    )

    // 绘制机器人半圆头部
    drawArc(
        brush = robotBrush,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(cx - headRadius, baseCy - headRadius + yOffset),
        size = Size(headRadius * 2, headRadius * 2),
        alpha = progress
    )

    // 眼睛
    if (progress > 0.7f) {
        val eyeAlpha = ((progress - 0.7f) / 0.3f).coerceIn(0f, 1f)
        // 眼睛出现时的弹跳缩放
        val eyeScale = if (eyeAlpha < 0.8f) (eyeAlpha / 0.8f) * 1.2f else 1.0f 
        val eyeRadius = 18f * scale * eyeScale
        
        drawCircle(Color.White, eyeRadius, Offset(cx - 65f * scale, baseCy - 75f * scale + yOffset), alpha = eyeAlpha)
        drawCircle(Color.White, eyeRadius, Offset(cx + 65f * scale, baseCy - 75f * scale + yOffset), alpha = eyeAlpha)
    }

    // 触角
    if (progress > 0.5f) {
        val antP = ((progress - 0.5f) / 0.5f).coerceIn(0f, 1f)
        val strokeW = 20f * scale // 调整为有一定宽度的圆柱形
        
        drawLine(
            brush = robotBrush,
            start = Offset(cx - 70f * scale, baseCy - headRadius + 15f * scale + yOffset), 
            end = Offset(cx - 105f * scale, baseCy - headRadius - 45f * scale + yOffset), 
            strokeWidth = strokeW, 
            alpha = antP, 
            cap = StrokeCap.Round
        )
        drawLine(
            brush = robotBrush,
            start = Offset(cx + 70f * scale, baseCy - headRadius + 15f * scale + yOffset), 
            end = Offset(cx + 105f * scale, baseCy - headRadius - 45f * scale + yOffset), 
            strokeWidth = strokeW, 
            alpha = antP, 
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawZeroStudioText(cx: Float, cy: Float, progress: Float, scale: Float) {
    val fullText = "ZeroStudio "
    val textYOffset = 244f * scale 
    
    val count = (fullText.length * progress).toInt()
    val visibleText = fullText.take(count)

    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            textSize = 110f * scale
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            
            // Apply a linear gradient to the text paint.
            shader = android.graphics.LinearGradient(
                cx - 200f, cy + textYOffset, cx + 200f, cy + textYOffset,
                android.graphics.Color.parseColor("#1E88E5"), // Start color
                android.graphics.Color.parseColor("#00E676"), // End color
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        
        canvas.nativeCanvas.drawText(
            visibleText,
            cx,
            cy + textYOffset,
            paint
        )
    }
}
