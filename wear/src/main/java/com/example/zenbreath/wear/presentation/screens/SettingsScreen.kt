package com.example.zenbreath.wear.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import com.example.zenbreath.wear.data.local.SettingsManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    onBack: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val scope = rememberCoroutineScope()
    val isAmbientEnabled by settingsManager.isAmbientModeEnabled.collectAsState(initial = true)

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Text(
                text = "Swipe right to return",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            SwitchButton(
                checked = isAmbientEnabled,
                onCheckedChange = {
                    scope.launch {
                        settingsManager.setAmbientModeEnabled(it)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Ambient Mode") },
                secondaryLabel = { Text("Keep screen on") }
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
