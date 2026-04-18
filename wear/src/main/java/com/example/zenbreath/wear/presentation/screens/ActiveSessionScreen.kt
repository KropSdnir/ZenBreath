package com.example.zenbreath.wear.presentation.screens

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import java.util.Locale
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.example.zenbreath.wear.data.sync.ZenBreathSyncManager
import com.example.zenbreath.wear.presentation.viewmodel.ZenBreathSessionViewModel

@Composable
fun ActiveSessionScreen(
    syncManager: ZenBreathSyncManager,
    viewModel: ZenBreathSessionViewModel,
    startTime: Long,
    isAmbient: Boolean = false,
    onStopSession: () -> Unit
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Vibrator::class.java) }
    
    val localBpm by viewModel.currentHeartRate.collectAsState()
    val syncBpm by syncManager.observeHeartRate().collectAsState(initial = 0)
    val focusRequester = remember { FocusRequester() }
    
    val bpm = if (localBpm > 0) localBpm else syncBpm

    LaunchedEffect(Unit) {
        viewModel.startTracking(syncManager)
        focusRequester.requestFocus()
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Trigger haptics on inhale/exhale transitions (Disable in ambient)
    LaunchedEffect(scale, isAmbient) {
        if (!isAmbient) {
            if (scale >= 0.99f) {
                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else if (scale <= 0.61f) {
                vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isAmbient) Color.Black else MaterialTheme.colorScheme.background)
            .onRotaryScrollEvent {
                // Example: Control brightness or scale with crown
                true
            }
            .focusRequester(focusRequester)
            .focusable(),
        contentAlignment = Alignment.Center
    ) {
        if (!isAmbient) {
            TimeText()
        }

        // Breathing Circle (Simplified for ambient)
        Box(
            modifier = Modifier
                .size(140.dp * scale)
                .clip(CircleShape)
                .background(
                    if (isAmbient) Color.DarkGray.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val elapsedMillis = System.currentTimeMillis() - startTime
            val seconds = (elapsedMillis / 1000) % 60
            val minutes = (elapsedMillis / (1000 * 60))
            
            Text(
                text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.displayMedium,
                color = if (isAmbient) Color.White else MaterialTheme.colorScheme.primary
            )

            Text(
                text = if (bpm > 0) "$bpm BPM" else "Measuring...",
                style = MaterialTheme.typography.titleMedium,
                color = if (isAmbient) Color.White else MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Breathe",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = if (isAmbient) Color.Gray else Color.Unspecified
            )
            
            if (!isAmbient) {
                Spacer(modifier = Modifier.height(12.dp))
                
                // MANUAL TUNING: Stop button target size 52dp for Ultra
                Button(
                    onClick = onStopSession,
                    modifier = Modifier.size(52.dp)
                ) {
                    Text("Stop")
                }
            }
        }
    }
}
