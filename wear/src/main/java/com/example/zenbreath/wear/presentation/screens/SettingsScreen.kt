package com.example.zenbreath.wear.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import com.example.zenbreath.wear.data.local.ZenBreathSettingsManager
import kotlinx.coroutines.launch

import androidx.compose.ui.res.painterResource
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.MaterialTheme as WearMaterialTheme

@Composable
fun SettingsScreen(
    settingsManager: ZenBreathSettingsManager,
    onBack: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val scope = rememberCoroutineScope()
    val isAmbientEnabled by settingsManager.isAmbientModeEnabled.collectAsState(initial = true)
    val duration by settingsManager.timerDuration.collectAsState(initial = 60000L)
    val reps by settingsManager.totalReps.collectAsState(initial = 10)
    val isCountUp by settingsManager.isCountUp.collectAsState(initial = false)

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Settings",
                style = WearMaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Ambient Mode Switch
        item {
            SwitchButton(
                checked = isAmbientEnabled,
                onCheckedChange = {
                    scope.launch {
                        settingsManager.setAmbientModeEnabled(it)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                label = { Text("Ambient Mode") }
            )
        }

        // Timer Mode (Count Up) Switch
        item {
            SwitchButton(
                checked = isCountUp,
                onCheckedChange = {
                    scope.launch {
                        settingsManager.setTimerMode(it)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Count Up") },
                secondaryLabel = { Text(if (isCountUp) "Stopwatch" else "Goal-based") }
            )
        }

        // Duration Adjustment
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Duration: ${duration / 1000}s",
                    style = WearMaterialTheme.typography.labelMedium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    IconButton(onClick = {
                        scope.launch { settingsManager.setDuration((duration - 5000L).coerceAtLeast(5000L)) }
                    }) {
                        Icon(painter = painterResource(id = android.R.drawable.ic_input_delete), contentDescription = "Decrease")
                    }
                    
                    IconButton(onClick = {
                        scope.launch { settingsManager.setDuration((duration + 5000L).coerceAtMost(1800000L)) }
                    }) {
                        Icon(painter = painterResource(id = android.R.drawable.ic_input_add), contentDescription = "Increase")
                    }
                }
            }
        }

        // Reps Adjustment
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Total Reps: $reps",
                    style = WearMaterialTheme.typography.labelMedium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    IconButton(onClick = {
                        scope.launch { settingsManager.setReps((reps - 1).coerceAtLeast(1)) }
                    }) {
                        Icon(painter = painterResource(id = android.R.drawable.ic_input_delete), contentDescription = "Decrease")
                    }
                    
                    IconButton(onClick = {
                        scope.launch { settingsManager.setReps((reps + 1).coerceAtMost(50)) }
                    }) {
                        Icon(painter = painterResource(id = android.R.drawable.ic_input_add), contentDescription = "Increase")
                    }
                }
            }
        }

        item {
            Button(
                onClick = onBack,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Return")
            }
        }
    }
}
