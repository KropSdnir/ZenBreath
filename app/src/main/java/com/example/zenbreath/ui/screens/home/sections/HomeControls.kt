package com.example.zenbreath.ui.screens.home.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SessionSettingsRow(
    timerDurationSeconds: Long,
    totalReps: Int,
    targetSeconds: Int,
    onUpdateTimer: (Int) -> Unit,
    onUpdateReps: (Int) -> Unit,
    onUpdateTarget: (Int) -> Unit,
    isRunning: Boolean,
    isCountUp: Boolean,
    windowWidthSizeClass: WindowWidthSizeClass,
    modifier: Modifier = Modifier
) {
    val isNarrow = windowWidthSizeClass == WindowWidthSizeClass.COMPACT

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rep Adjustment (Always on the left)
        AdjustmentControl(
            label = "Reps",
            value = "$totalReps",
            onDecrement = { onUpdateReps(-1) },
            onIncrement = { onUpdateReps(1) },
            enabled = !isRunning,
            compact = isNarrow
        )

        // Timer Adjustment (Only show if NOT count up - i.e. Countdown mode)
        if (!isCountUp) {
            AdjustmentControl(
                label = "Timer (s)",
                value = "$timerDurationSeconds",
                onDecrement = { onUpdateTimer(-5) },
                onIncrement = { onUpdateTimer(5) },
                enabled = !isRunning,
                compact = isNarrow
            )
        }

        // Target Adjustment (Only show if Count Up mode)
        if (isCountUp) {
            AdjustmentControl(
                label = "Target (s)",
                value = "$targetSeconds",
                onDecrement = { onUpdateTarget(-5) },
                onIncrement = { onUpdateTarget(5) },
                enabled = !isRunning,
                compact = isNarrow
            )
        }
    }
}

@Composable
private fun AdjustmentControl(
    label: String,
    value: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    enabled: Boolean,
    compact: Boolean = false
) {
    val iconSize = if (compact) 32.dp else 48.dp
    
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDecrement, 
                    enabled = enabled,
                    modifier = Modifier.size(iconSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown, 
                        contentDescription = "Decrease $label",
                        modifier = Modifier.size(if (compact) 20.dp else 24.dp)
                    )
                }
                Text(
                    text = value,
                    style = if (compact) {
                        MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    } else {
                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    }
                )
                IconButton(
                    onClick = onIncrement, 
                    enabled = enabled,
                    modifier = Modifier.size(iconSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp, 
                        contentDescription = "Increase $label",
                        modifier = Modifier.size(if (compact) 20.dp else 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StartStopButton(
    isRunning: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isRunning) Color.Red else MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = if (isRunning) "STOP" else "START",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RepCounter(
    currentRep: Int,
    totalReps: Int,
    onResetClick: () -> Unit,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Rep Count: $currentRep/$totalReps",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        
        IconButton(
            onClick = onResetClick,
            enabled = !isRunning
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset reps")
        }
    }
}
