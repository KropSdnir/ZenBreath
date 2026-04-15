package com.example.zenbreath.wear.presentation.screens

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.MaterialTheme as WearMaterialTheme
import com.example.zenbreath.wear.data.sync.WearSyncManager
import com.example.zenbreath.wear.presentation.viewmodel.SessionViewModel
import kotlinx.coroutines.launch

@Composable
fun WearHomeScreen(
    syncManager: WearSyncManager,
    sessionViewModel: SessionViewModel,
    onStartSession: () -> Unit
) {
    val sessionState by syncManager.observeActiveSession().collectAsState(initial = Pair(false, 0L))
    val currentHeartRate by sessionViewModel.currentHeartRate.collectAsState()
    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    // Start background syncing of HR immediately when Home Screen is visible
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        sessionViewModel.syncHeartRateToPhone(syncManager)
    }
    
    // Ensure heart rate is synced to phone even when not in session
    // This is now handled by syncHeartRateToPhone inside the ViewModel

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .onRotaryScrollEvent {
                coroutineScope.launch {
                    listState.scrollBy(it.verticalScrollPixels)
                }
                true
            }
            .focusRequester(focusRequester)
            .focusable(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "ZenBreath",
                style = WearMaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Text(
                text = if (currentHeartRate > 0) "$currentHeartRate BPM" else "-- BPM",
                style = WearMaterialTheme.typography.displaySmall,
                color = WearMaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            val buttonText = if (sessionState.first) "Active on Phone" else "Start"
            
            // MANUAL TUNING: 52.dp touch target for Ultra
            Button(
                onClick = onStartSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = buttonText)
            }
        }
        
        item {
            Text(
                text = if (sessionState.first) "Syncing..." else "Ready",
                style = WearMaterialTheme.typography.bodySmall,
                color = WearMaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
