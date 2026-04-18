package com.example.zenbreath.wear.presentation.screens

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme as WearMaterialTheme
import androidx.wear.compose.material3.Text
import com.example.zenbreath.wear.data.local.ZenBreathSettingsManager
import com.example.zenbreath.wear.data.sync.ZenBreathSyncManager
import com.example.zenbreath.wear.presentation.components.ZenBreathWatchFireTimer
import com.example.zenbreath.wear.presentation.components.ZenBreathWatchFireTimerUiState
import com.example.zenbreath.wear.presentation.viewmodel.ZenBreathSessionViewModel
import kotlinx.coroutines.delay

@Composable
fun WearHomeScreen(
    syncManager: ZenBreathSyncManager,
    sessionViewModel: ZenBreathSessionViewModel,
    settingsManager: ZenBreathSettingsManager,
    onStartSession: () -> Unit,
    onStopSession: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val sessionStatus by syncManager.observeActiveSession().collectAsState(initial = Triple(false, 0L, 1))
    val currentHeartRate by sessionViewModel.currentHeartRate.collectAsState()
    val timerColor by settingsManager.timerColor.collectAsState(initial = 0xFF2D6A4FL)
    val timerDuration by settingsManager.timerDuration.collectAsState(initial = 60000L)
    val totalReps by settingsManager.totalReps.collectAsState(initial = 10)
    val targetSecs by settingsManager.targetSeconds.collectAsState(initial = 30)
    val currentRep = sessionStatus.third
    
    val focusRequester = remember { FocusRequester() }

    val isCountUp by settingsManager.isCountUp.collectAsState(initial = true)
    
    var displayTime by remember { mutableStateOf(0L) }
    
    LaunchedEffect(sessionStatus, timerDuration, isCountUp) {
        if (sessionStatus.first) {
            val startTime = sessionStatus.second
            while (true) {
                val currentTime = System.currentTimeMillis()
                val elapsed = (currentTime - startTime).coerceAtLeast(0)
                
                if (isCountUp) {
                    displayTime = elapsed
                } else {
                    displayTime = (timerDuration - elapsed).coerceAtLeast(0)
                    if (displayTime <= 0) break
                }
                delay(50)
            }
        } else {
            displayTime = if (isCountUp) 0L else timerDuration
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        sessionViewModel.syncHeartRateToPhone(syncManager)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ZenBreathWatchFireTimer(
                uiState = ZenBreathWatchFireTimerUiState(
                    remainingMillis = displayTime,
                    totalMillis = timerDuration,
                    isFinished = sessionStatus.first && if (isCountUp) false else displayTime <= 0L,
                    isCountUp = isCountUp,
                    targetSeconds = targetSecs,
                    color = Color(timerColor),
                    currentHeartRate = currentHeartRate
                ),
                onToggleTimer = {
                    if (sessionStatus.first) onStopSession() else onStartSession()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Rep: $currentRep/$totalReps",
                style = WearMaterialTheme.typography.labelLarge,
                color = WearMaterialTheme.colorScheme.onSurface
            )
        }
    }
}
