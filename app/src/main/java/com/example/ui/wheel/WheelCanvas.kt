package com.example.ui.wheel

import android.graphics.Color as AndroidColor
import android.graphics.Paint as AndroidPaint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.WheelItemEntity
import com.example.model.WheelPaletteTheme
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class SliceInfo(
    val item: WheelItemEntity,
    val startAngle: Float,
    val sweepAngle: Float,
    val color: Color
)

@Composable
fun WheelCanvas(
    items: List<WheelItemEntity>,
    paletteTheme: WheelPaletteTheme,
    spinDurationSeconds: Float,
    isSpinning: Boolean,
    onSpinStart: () -> Unit,
    onSpinTick: () -> Unit,
    onSpinComplete: (WheelItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val activeItems = remember(items) { items.filter { it.isEnabled } }

    val rotationAnimatable = remember { Animatable(0f) }
    var pointerWobble by remember { mutableFloatStateOf(0f) }

    // Pointer spring animation for peg collisions
    val animatedPointerWobble by animateFloatAsState(
        targetValue = pointerWobble,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "pointerWobble"
    )

    // Calculate slices with weights
    val totalWeight = remember(activeItems) {
        val sum = activeItems.sumOf { it.weight.coerceAtLeast(1) }
        if (sum > 0) sum.toFloat() else 1f
    }

    val slices = remember(activeItems, paletteTheme, totalWeight) {
        if (activeItems.isEmpty()) {
            emptyList()
        } else {
            var currentAngle = 0f
            activeItems.mapIndexed { index, item ->
                val sweep = (item.weight.coerceAtLeast(1) / totalWeight) * 360f
                val sliceColor = if (item.colorHex != null) {
                    Color(item.colorHex)
                } else {
                    paletteTheme.getColorForIndex(index)
                }
                val slice = SliceInfo(
                    item = item,
                    startAngle = currentAngle,
                    sweepAngle = sweep,
                    color = sliceColor
                )
                currentAngle += sweep
                slice
            }
        }
    }

    // Function to trigger spin
    fun startSpin() {
        if (isSpinning || activeItems.isEmpty()) return
        onSpinStart()

        coroutineScope.launch {
            val currentRot = rotationAnimatable.value
            // Spin 6 to 10 full revolutions + random offset
            val fullRevolutions = Random.nextInt(6, 11) * 360f
            val randomOffset = Random.nextFloat() * 360f
            val targetRotation = currentRot + fullRevolutions + randomOffset

            var lastSliceIndex = -1
            val durationMillis = (spinDurationSeconds * 1000).toInt()

            rotationAnimatable.animateTo(
                targetValue = targetRotation,
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = CubicBezierEasing(0.12f, 0.75f, 0.15f, 1.0f)
                )
            ) {
                // Calculate which slice is currently under top pointer (270 degrees)
                val currentNormAngle = (270f - (value % 360f) + 360f) % 360f
                val currentSlice = slices.indexOfFirst { slice ->
                    currentNormAngle >= slice.startAngle &&
                            currentNormAngle < (slice.startAngle + slice.sweepAngle)
                }

                if (currentSlice != -1 && currentSlice != lastSliceIndex) {
                    lastSliceIndex = currentSlice
                    pointerWobble = if (pointerWobble > 0f) -16f else 16f
                    onSpinTick()
                } else {
                    pointerWobble = 0f
                }
            }

            pointerWobble = 0f

            // Determine winner at top pointer (270 degrees)
            val finalNormAngle = (270f - (rotationAnimatable.value % 360f) + 360f) % 360f
            val winningSlice = slices.firstOrNull { slice ->
                finalNormAngle >= slice.startAngle &&
                        finalNormAngle < (slice.startAngle + slice.sweepAngle)
            } ?: slices.firstOrNull()

            if (winningSlice != null) {
                onSpinComplete(winningSlice.item)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Main Wheel Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("wheel_canvas")
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = !isSpinning && activeItems.isNotEmpty()
                ) {
                    startSpin()
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
            val outerRadius = (canvasWidth.coerceAtMost(canvasHeight) / 2f) - 24f
            val hubRadius = outerRadius * 0.24f

            // 1. Draw outer rim shadow & metallic bezel
            drawCircle(
                color = Color(0x33000000),
                radius = outerRadius + 14f,
                center = center + Offset(0f, 8f)
            )
            // Outer golden/metallic rim
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(
                        Color(0xFFD97706),
                        Color(0xFFFDE68A),
                        Color(0xFFB45309),
                        Color(0xFFFDE68A),
                        Color(0xFFD97706)
                    ),
                    center = center
                ),
                radius = outerRadius + 8f,
                center = center
            )
            // Outer dark bezel line
            drawCircle(
                color = Color(0xFF1E1B4B),
                radius = outerRadius + 2f,
                center = center,
                style = Stroke(width = 4f)
            )

            // If empty, draw empty placeholder
            if (slices.isEmpty()) {
                drawCircle(
                    color = Color(0xFF334155),
                    radius = outerRadius,
                    center = center
                )
                drawTextNative(
                    text = "No active names",
                    x = center.x,
                    y = center.y,
                    textColor = AndroidColor.WHITE,
                    textSizePx = 48f,
                    isBold = true
                )
            } else {
                // 2. Rotate and draw slices
                val wheelAngle = rotationAnimatable.value
                val sliceArcRect = Size(outerRadius * 2f, outerRadius * 2f)
                val sliceTopLeft = Offset(center.x - outerRadius, center.y - outerRadius)

                rotate(degrees = wheelAngle, pivot = center) {
                    slices.forEach { slice ->
                        // Draw arc wedge
                        drawArc(
                            color = slice.color,
                            startAngle = slice.startAngle,
                            sweepAngle = slice.sweepAngle,
                            useCenter = true,
                            topLeft = sliceTopLeft,
                            size = sliceArcRect
                        )

                        // Slice separator line
                        val rad = Math.toRadians(slice.startAngle.toDouble())
                        val endX = center.x + (outerRadius * cos(rad)).toFloat()
                        val endY = center.y + (outerRadius * sin(rad)).toFloat()
                        drawLine(
                            color = Color(0x55FFFFFF),
                            start = center,
                            end = Offset(endX, endY),
                            strokeWidth = 3f
                        )

                        // Draw item text inside wedge
                        val midAngle = slice.startAngle + (slice.sweepAngle / 2f)
                        rotate(degrees = midAngle, pivot = center) {
                            // Text positioned along radial axis
                            val textDist = hubRadius + (outerRadius - hubRadius) * 0.58f
                            val maxTextLengthPx = (outerRadius - hubRadius) * 0.75f
                            val formattedName = truncateText(slice.item.name, 16)

                            // Pick text contrast (white text with subtle shadow)
                            drawSliceText(
                                text = formattedName,
                                centerX = center.x + textDist,
                                centerY = center.y,
                                maxWidthPx = maxTextLengthPx
                            )
                        }
                    }

                    // 3. Pegs around the outer rim
                    val pegCount = if (slices.size in 3..24) slices.size else 16
                    for (i in 0 until pegCount) {
                        val pegAngle = i * (360f / pegCount)
                        val rad = Math.toRadians(pegAngle.toDouble())
                        val pegDist = outerRadius - 10f
                        val pegX = center.x + (pegDist * cos(rad)).toFloat()
                        val pegY = center.y + (pegDist * sin(rad)).toFloat()

                        // Outer peg shadow
                        drawCircle(
                            color = Color(0x66000000),
                            radius = 6.5f,
                            center = Offset(pegX, pegY + 1.5f)
                        )
                        // Metallic peg
                        drawCircle(
                            color = Color(0xFFFDE68A),
                            radius = 6f,
                            center = Offset(pegX, pegY)
                        )
                        drawCircle(
                            color = Color(0xFFB45309),
                            radius = 2.5f,
                            center = Offset(pegX, pegY)
                        )
                    }
                }
            }

            // 4. Center Hub (SPIN button)
            // Outer hub shadow
            drawCircle(
                color = Color(0x44000000),
                radius = hubRadius + 6f,
                center = center + Offset(0f, 4f)
            )
            // Hub bezel ring
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFF59E0B), Color(0xFFFDE68A), Color(0xFFB45309)),
                    start = Offset(center.x - hubRadius, center.y - hubRadius),
                    end = Offset(center.x + hubRadius, center.y + hubRadius)
                ),
                radius = hubRadius + 4f,
                center = center
            )
            // Hub core disc
            drawCircle(
                color = if (isSpinning) Color(0xFF1E293B) else Color(0xFF0F172A),
                radius = hubRadius,
                center = center
            )

            // SPIN text inside center hub
            drawTextNative(
                text = if (isSpinning) "..." else "SPIN",
                x = center.x,
                y = center.y + 2f,
                textColor = if (isSpinning) AndroidColor.GRAY else AndroidColor.WHITE,
                textSizePx = hubRadius * 0.44f,
                isBold = true
            )

            // 5. Sleek Top Pointer Arrow (Points down at 12 o'clock into the wheel)
            val pointerWidth = 32f
            val pointerHeight = 44f
            val pointerTipY = center.y - outerRadius + 14f
            val pointerBaseY = pointerTipY - pointerHeight
            val pointerPivot = Offset(center.x, pointerBaseY)

            rotate(degrees = animatedPointerWobble, pivot = pointerPivot) {
                // Pointer shadow
                val shadowPath = Path().apply {
                    moveTo(center.x, pointerTipY + 4f)
                    lineTo(center.x - pointerWidth / 2f - 2f, pointerBaseY + 2f)
                    lineTo(center.x + pointerWidth / 2f + 2f, pointerBaseY + 2f)
                    close()
                }
                drawPath(path = shadowPath, color = Color(0x55000000))

                // Pointer outer border
                val outerPointerPath = Path().apply {
                    moveTo(center.x, pointerTipY)
                    lineTo(center.x - pointerWidth / 2f, pointerBaseY)
                    lineTo(center.x + pointerWidth / 2f, pointerBaseY)
                    close()
                }
                drawPath(path = outerPointerPath, color = Color(0xFFFDE68A))

                // Pointer inner core
                val innerPointerPath = Path().apply {
                    moveTo(center.x, pointerTipY - 5f)
                    lineTo(center.x - pointerWidth / 2f + 4f, pointerBaseY + 4f)
                    lineTo(center.x + pointerWidth / 2f - 4f, pointerBaseY + 4f)
                    close()
                }
                drawPath(path = innerPointerPath, color = Color(0xFFDC2626))

                // Pointer top anchor dot
                drawCircle(
                    color = Color(0xFFFDE68A),
                    radius = 8f,
                    center = Offset(center.x, pointerBaseY + 2f)
                )
                drawCircle(
                    color = Color(0xFF7F1D1D),
                    radius = 4f,
                    center = Offset(center.x, pointerBaseY + 2f)
                )
            }
        }
    }
}

private fun DrawScope.drawSliceText(
    text: String,
    centerX: Float,
    centerY: Float,
    maxWidthPx: Float
) {
    drawIntoCanvas { canvas ->
        val paint = AndroidPaint().apply {
            color = AndroidColor.WHITE
            textSize = 34f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = AndroidPaint.Align.CENTER
            setShadowLayer(6f, 1f, 2f, AndroidColor.argb(200, 0, 0, 0))
        }

        // Measure and auto-downscale if too long
        val textWidth = paint.measureText(text)
        if (textWidth > maxWidthPx && maxWidthPx > 20f) {
            val scale = (maxWidthPx / textWidth).coerceIn(0.6f, 1f)
            paint.textSize = 34f * scale
        }

        // Center vertically
        val fontMetrics = paint.fontMetrics
        val yOffset = (fontMetrics.descent + fontMetrics.ascent) / 2f

        canvas.nativeCanvas.drawText(text, centerX, centerY - yOffset, paint)
    }
}

private fun DrawScope.drawTextNative(
    text: String,
    x: Float,
    y: Float,
    textColor: Int,
    textSizePx: Float,
    isBold: Boolean
) {
    drawIntoCanvas { canvas ->
        val paint = AndroidPaint().apply {
            color = textColor
            textSize = textSizePx
            isAntiAlias = true
            typeface = if (isBold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
            textAlign = AndroidPaint.Align.CENTER
            setShadowLayer(4f, 0f, 2f, AndroidColor.argb(120, 0, 0, 0))
        }
        val fontMetrics = paint.fontMetrics
        val yOffset = (fontMetrics.descent + fontMetrics.ascent) / 2f
        canvas.nativeCanvas.drawText(text, x, y - yOffset, paint)
    }
}

private fun truncateText(text: String, maxChars: Int): String {
    return if (text.length > maxChars) {
        text.take(maxChars - 1) + "…"
    } else {
        text
    }
}
