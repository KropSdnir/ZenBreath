package com.example.zenbreath.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenbreath.R
import kotlin.math.ceil

/**
 * A shape that creates a pie slice mask for the progress ring
 */
class ProgressPieShape(private val progress: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        if (progress <= 0f) return Outline.Generic(Path())
        if (progress >= 1f) return Outline.Rectangle(Rect(Offset.Zero, size))

        val path = Path().apply {
            moveTo(size.width / 2f, size.height / 2f)
            arcTo(
                rect = Rect(Offset.Zero, size),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 360f * progress,
                forceMoveTo = false
            )
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Circular timer display with a fire ring that fills up in discrete steps
 */
@Composable
fun TimerDisplay(
    remainingTimeMs: Long,
    totalTimeMs: Long,
    modifier: Modifier = Modifier
) {
    val totalSeconds = (totalTimeMs / 1000).coerceAtLeast(1)
    val remainingSeconds = ceil(remainingTimeMs / 1000f).toLong()
    val elapsedSeconds = (totalSeconds - remainingSeconds).coerceIn(0, totalSeconds)
    
    // MANUAL TUNING: Discrete progress calculation (e.g. 1/60th of the circle per second)
    val steppedProgress = elapsedSeconds.toFloat() / totalSeconds.toFloat()
    
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        // 1. Background "Ghost" Ring (Shows the full path at very low opacity)
        Image(
            painter = painterResource(id = R.drawable.fire_ring),
            contentDescription = null,
            alpha = 0.05f,
            modifier = Modifier.fillMaxSize()
        )

        // 2. The Active Fire Progress (Determinate/Stepped progress)
        // Only shows the portion corresponding to elapsed time
        Image(
            painter = painterResource(id = R.drawable.fire_ring),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(ProgressPieShape(steppedProgress))
        )

        // 3. Time Text
        Text(
            text = String.format("%02d:%02d", minutes, seconds),
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp,
                color = Color.White,
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black.copy(alpha = 0.8f),
                    blurRadius = 12f
                )
            )
        )
    }
}
