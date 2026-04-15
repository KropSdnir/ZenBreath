package com.example.zenbreath.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class FireTimerUiState(
    val remainingMillis: Long,
    val totalMillis: Long,
    val isFinished: Boolean,
    val color: Color = Color(0xFF6200EE)
)

/**
 * Clean, solid-color Progress Timer.
 * Replaces the previous AGSL fire effect for a more minimal aesthetic.
 */
@Composable
fun FireTimer(
    uiState: FireTimerUiState,
    modifier: Modifier = Modifier
) {
    val elapsed = (uiState.totalMillis - uiState.remainingMillis).toFloat()
    val rawProgress = if (uiState.totalMillis > 0) (elapsed / uiState.totalMillis) else 0f
    
    val targetProgress = if (uiState.isFinished) 1.0f else rawProgress.coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = if (uiState.isFinished) {
            // Elegant snap for the finish state
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        } else {
            // High-frequency linear interpolation to match the 50ms ViewModel tick
            tween(durationMillis = 50, easing = LinearEasing)
        },
        label = "Progress"
    )

    Box(
        modifier = modifier.size(300.dp),
        contentAlignment = Alignment.Center
    ) {
        // Solid Progress Ring
        ProgressRing(
            progress = animatedProgress,
            color = uiState.color,
            modifier = Modifier.fillMaxSize()
        )

        // Center Time Display
        Text(
            text = formatTime(uiState.remainingMillis),
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 64.sp,
                color = MaterialTheme.colorScheme.onSurface,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.1f),
                    blurRadius = 8f,
                    offset = Offset(0f, 4f)
                )
            )
        )
    }
}

@Composable
private fun ProgressRing(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidthPx = 16.dp.toPx()
        val arcSize = size.width - strokeWidthPx
        val topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f)

        // Background Circle (Track)
        drawCircle(
            color = color.copy(alpha = 0.1f),
            center = center,
            radius = arcSize / 2f,
            style = Stroke(width = strokeWidthPx)
        )

        // Active Progress Arc
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = topLeft,
            size = Size(arcSize, arcSize),
            style = Stroke(
                width = strokeWidthPx, 
                cap = StrokeCap.Round
            )
        )
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
