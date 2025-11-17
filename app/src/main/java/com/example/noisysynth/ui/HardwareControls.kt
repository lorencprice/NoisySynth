package com.example.noisysynth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Hardware-style synthesizer module panel with metallic finish
 */
@Composable
fun HardwareModulePanel(
    title: String,
    accentColor: Color = Color(0xFF00E5FF),
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2A2A2A),
                            Color(0xFF1A1A1A),
                            Color(0xFF0A0A0A)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Module header with LED accent
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // LED indicator
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .shadow(4.dp, CircleShape)
                            .background(accentColor, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Title
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFE0E0E0),
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Module content
                content()
            }
        }
    }
}

/**
 * Rotary knob control with touch drag support
 */
@Composable
fun RotaryKnob(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueDisplay: String = String.format("%.0f%%", value * 100),
    accentColor: Color = Color(0xFF00E5FF),
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableStateOf(value) }
    
    // Sync dragValue with external value when not dragging
    LaunchedEffect(value) {
        if (!isDragging) {
            dragValue = value
        }
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Knob
        Box(
            modifier = Modifier
                .size(50.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { 
                            isDragging = true
                            dragValue = value  // Capture starting value
                        },
                        onDragEnd = { 
                            isDragging = false 
                        },
                        onDragCancel = { 
                            isDragging = false 
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        // Only respond to vertical movement (up/down)
                        // Swipe UP = increase, Swipe DOWN = decrease
                        val delta = -dragAmount.y / 250f
                        dragValue = (dragValue + delta).coerceIn(0f, 1f)  // Update local state
                        onValueChange(dragValue)  // Report to parent
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(50.dp)) {
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val radius = size.minDimension / 2f - 4.dp.toPx()

                // Outer ring (track)
                drawCircle(
                    color = Color(0xFF3A3A3A),
                    radius = radius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 3.dp.toPx())
                )

                // Use dragValue for visual feedback
                val currentValue = if (isDragging) dragValue else value
                val sweepAngle = 270f * currentValue
                val startAngle = 135f
                
                // Active arc
                drawArc(
                    color = accentColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Knob body
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isDragging) Color(0xFF4A4A4A) else Color(0xFF3A3A3A),
                            Color(0xFF1A1A1A)
                        )
                    ),
                    radius = radius - 6.dp.toPx(),
                    center = Offset(centerX, centerY)
                )

                // Indicator line
                val angle = (startAngle + sweepAngle) * PI.toFloat() / 180f
                val indicatorLength = radius - 10.dp.toPx()
                val indicatorX = centerX + cos(angle) * indicatorLength
                val indicatorY = centerY + sin(angle) * indicatorLength

                drawLine(
                    color = accentColor,
                    start = Offset(centerX, centerY),
                    end = Offset(indicatorX, indicatorY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Label
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFB0B0B0),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        // LED-style value display
        LEDDisplay(
            text = valueDisplay,
            accentColor = accentColor
        )
    }
}

/**
 * Vertical fader control (alternative to knob)
 */
@Composable
fun VerticalFader(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueDisplay: String = String.format("%.0f%%", value * 100),
    accentColor: Color = Color(0xFF00E5FF),
    modifier: Modifier = Modifier,
    height: Int = 80
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // LED-style value display
        LEDDisplay(
            text = valueDisplay,
            accentColor = accentColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Fader track
        Box(
            modifier = Modifier
                .width(30.dp)
                .height(height.dp)
                .background(Color(0xFF1A1A1A), RoundedCornerShape(15.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val y = change.position.y.coerceIn(0f, size.height.toFloat())
                        val newValue = 1f - (y / size.height)
                        onValueChange(newValue)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val y = offset.y.coerceIn(0f, size.height.toFloat())
                        val newValue = 1f - (y / size.height)
                        onValueChange(newValue)
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            // Active fill
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .fillMaxHeight(value)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                accentColor,
                                accentColor.copy(alpha = 0.7f)
                            )
                        ),
                        shape = RoundedCornerShape(15.dp)
                    )
            )

            // Fader cap
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(12.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = -(height * value).dp + 6.dp)
                    .shadow(4.dp, RoundedCornerShape(6.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF5A5A5A),
                                Color(0xFF4A4A4A),
                                Color(0xFF5A5A5A)
                            )
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Label
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFB0B0B0),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * LED-style digital display for values
 */
@Composable
fun LEDDisplay(
    text: String,
    accentColor: Color = Color(0xFF00E5FF),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF0A0A0A), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = accentColor,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Hardware-style toggle switch
 */
@Composable
fun HardwareSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color = Color(0xFF00E5FF),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFB0B0B0)
        )

        Box(
            modifier = Modifier
                .width(36.dp)
                .height(20.dp)
                .background(
                    color = if (checked) accentColor.copy(alpha = 0.3f) else Color(0xFF1A1A1A),
                    shape = RoundedCornerShape(10.dp)
                )
                .pointerInput(Unit) {
                    detectTapGestures {
                        onCheckedChange(!checked)
                    }
                },
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .size(16.dp)
                    .shadow(4.dp, CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = if (checked) {
                                listOf(accentColor, accentColor.copy(alpha = 0.8f))
                            } else {
                                listOf(Color(0xFF4A4A4A), Color(0xFF2A2A2A))
                            }
                        ),
                        shape = CircleShape
                    )
            )
        }
    }
}

/**
 * Hardware-style button
 */
@Composable
fun HardwareButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    accentColor: Color = Color(0xFF00E5FF),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(if (selected) 2.dp else 4.dp, RoundedCornerShape(6.dp))
            .background(
                brush = if (selected) {
                    Brush.verticalGradient(
                        colors = listOf(
                            accentColor,
                            accentColor.copy(alpha = 0.8f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF3A3A3A),
                            Color(0xFF2A2A2A)
                        )
                    )
                },
                shape = RoundedCornerShape(6.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures { onClick() }
            }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            color = if (selected) Color(0xFF000000) else Color(0xFFB0B0B0)
        )
    }
}
