package com.example.zenbreath.ui.screens.home.sections

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.zenbreath.ui.components.FireTimer
import com.example.zenbreath.ui.components.FireTimerUiState
import com.example.zenbreath.ui.components.SessionItem
import com.example.zenbreath.ui.screens.home.HomeUiState

@Composable
fun HomeContent(
    uiState: HomeUiState,
    onDateSelected: (Long) -> Unit,
    onTimerDurationEdit: () -> Unit,
    onRepCountEdit: () -> Unit,
    onStartStopClick: () -> Unit,
    onResetRepsClick: () -> Unit,
    onDeleteSession: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            HomeHeader(
                selectedDate = uiState.selectedDate,
                onDateSelected = onDateSelected
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            FireTimer(
                uiState = FireTimerUiState(
                    remainingMillis = uiState.remainingTime,
                    totalMillis = uiState.timerDuration,
                    isFinished = uiState.remainingTime <= 0L,
                    color = Color(uiState.timerColor)
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            TimerDurationSelector(
                timerDurationSeconds = uiState.timerDuration / 1000,
                onEditClick = onTimerDurationEdit,
                isRunning = uiState.isRunning
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            RepSelector(
                totalReps = uiState.totalReps,
                onEditClick = onRepCountEdit,
                isRunning = uiState.isRunning
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Heart Rate: ${if (uiState.currentHeartRate > 0) "${uiState.currentHeartRate} BPM" else "---"}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            StartStopButton(
                isRunning = uiState.isRunning,
                onClick = onStartStopClick
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            RepCounter(
                currentRep = uiState.currentRep,
                totalReps = uiState.totalReps,
                isRunning = uiState.isRunning,
                onResetClick = onResetRepsClick
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SessionHistoryHeader()
        }
        
        itemsIndexed(
            items = uiState.filteredSessions,
            key = { _, session -> session.id }
        ) { index, session ->
            SessionItem(
                session = session,
                index = index,
                onDelete = onDeleteSession
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
