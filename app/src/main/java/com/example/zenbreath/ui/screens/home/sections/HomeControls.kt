package com.example.zenbreath.ui.screens.home.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
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
fun TimerDurationSelector(
    timerDurationSeconds: Long,
    onEditClick: () -> Unit,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Timer: ${timerDurationSeconds}s",
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(
            onClick = onEditClick,
            enabled = !isRunning
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit timer")
        }
    }
}

@Composable
fun RepSelector(
    totalReps: Int,
    onEditClick: () -> Unit,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Rep: $totalReps",
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(
            onClick = onEditClick,
            enabled = !isRunning
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit reps")
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
