package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    val startX: Float,
    val startY: Float,
    val speedX: Float,
    val speedY: Float,
    val gravity: Float,
    val rotationSpeed: Float,
    val width: Float,
    val height: Float,
    val color: Color,
    val isCircle: Boolean
)

@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 90
) {
    val progress = remember { Animatable(0f) }

    val colors = listOf(
        Color(0xFFEF4444),
        Color(0xFFF59E0B),
        Color(0xFF10B981),
        Color(0xFF3B82F6),
        Color(0xFF8B5CF6),
        Color(0xFFEC4899),
        Color(0xFFFBBF24),
        Color(0xFF06B6D4)
    )

    val particles = remember {
        List(particleCount) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 700f + 300f
            ConfettiParticle(
                startX = 0.5f,
                startY = 0.35f,
                speedX = cos(angle) * speed,
                speedY = sin(angle) * speed - 200f,
                gravity = Random.nextFloat() * 500f + 650f,
                rotationSpeed = Random.nextFloat() * 720f - 360f,
                width = Random.nextFloat() * 14f + 8f,
                height = Random.nextFloat() * 22f + 12f,
                color = colors[Random.nextInt(colors.size)],
                isCircle = Random.nextBoolean()
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2600, easing = LinearEasing)
        )
    }

    if (progress.value < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val t = progress.value
            val timeSec = t * 2.6f
            val canvasW = size.width
            val canvasH = size.height

            particles.forEach { p ->
                val currentX = (p.startX * canvasW) + (p.speedX * timeSec)
                val currentY = (p.startY * canvasH) + (p.speedY * timeSec) + (0.5f * p.gravity * timeSec * timeSec)
                val currentRot = p.rotationSpeed * timeSec
                val alpha = (1f - t).coerceIn(0f, 1f)

                if (currentY in -50f..(canvasH + 50f) && currentX in -50f..(canvasW + 50f)) {
                    val pColor = p.color.copy(alpha = alpha)
                    rotate(degrees = currentRot, pivot = Offset(currentX, currentY)) {
                        if (p.isCircle) {
                            drawCircle(
                                color = pColor,
                                radius = p.width / 2f,
                                center = Offset(currentX, currentY)
                            )
                        } else {
                            drawRect(
                                color = pColor,
                                topLeft = Offset(currentX - p.width / 2f, currentY - p.height / 2f),
                                size = Size(p.width, p.height)
                            )
                        }
                    }
                }
            }
        }
    }
}
