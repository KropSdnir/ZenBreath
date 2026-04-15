package com.example.zenbreath.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.zenbreath.ui.components.dialogs.ColorPickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zenbreath.ui.components.FireTimer
import com.example.zenbreath.ui.components.FireTimerUiState
import com.example.zenbreath.ui.components.SessionItem
import com.example.zenbreath.ui.components.dialogs.RepCountDialog
import com.example.zenbreath.ui.components.dialogs.TimerDurationDialog
import com.example.zenbreath.ui.screens.home.sections.HomeHeader
import com.example.zenbreath.ui.screens.home.sections.RepCounter
import com.example.zenbreath.ui.screens.home.sections.RepSelector
import com.example.zenbreath.ui.screens.home.sections.SessionHistoryHeader
import com.example.zenbreath.ui.screens.home.sections.StartStopButton
import com.example.zenbreath.ui.screens.home.sections.TimerDurationSelector
import com.example.zenbreath.viewmodel.BreathingViewModel

/**
 * Main home screen for breathing tracking app.
 * Refactored for scalability following the 500-line threshold rule.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BreathingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // State observation with 2026 Best Practices (collectAsStateWithLifecycle)
    val timerDuration by viewModel.timerDuration.collectAsStateWithLifecycle()
    val remainingTime by viewModel.remainingTime.collectAsStateWithLifecycle()
    val totalReps by viewModel.totalReps.collectAsStateWithLifecycle()
    val currentRep by viewModel.currentRep.collectAsStateWithLifecycle()
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val currentHeartRate by viewModel.currentHeartRate.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val filteredSessions by viewModel.filteredSessions.collectAsStateWithLifecycle()
    val recentSessions by viewModel.recentSessions.collectAsStateWithLifecycle(initialValue = emptyList())
    val timerColor by viewModel.timerColor.collectAsStateWithLifecycle()
    
    var showTimerDialog by remember { mutableStateOf(false) }
    var showRepDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("ZenBreath") },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Settings")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Change Timer Color") },
                            onClick = {
                                showMenu = false
                                showColorDialog = true
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val filePath = viewModel.exportToCSV(context, recentSessions)
                    Toast.makeText(context, "Sessions exported to: $filePath", Toast.LENGTH_LONG).show()
                }
            ) {
                Icon(Icons.Default.Share, contentDescription = "Export to CSV")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                HomeHeader(
                    selectedDate = selectedDate,
                    onDateSelected = { viewModel.setSelectedDate(it) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                FireTimer(
                    uiState = FireTimerUiState(
                        remainingMillis = remainingTime,
                        totalMillis = timerDuration,
                        isFinished = remainingTime <= 0L,
                        color = Color(timerColor)
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                TimerDurationSelector(
                    timerDurationSeconds = timerDuration / 1000,
                    onEditClick = { showTimerDialog = true },
                    isRunning = isRunning
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                RepSelector(
                    totalReps = totalReps,
                    onEditClick = { showRepDialog = true },
                    isRunning = isRunning
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Heart Rate: ${if (currentHeartRate > 0) "$currentHeartRate BPM" else "---"}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                StartStopButton(
                    isRunning = isRunning,
                    onClick = {
                        if (isRunning) viewModel.stopExercise() else viewModel.startExercise()
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                RepCounter(
                    currentRep = currentRep,
                    totalReps = totalReps,
                    isRunning = isRunning,
                    onResetClick = { viewModel.resetReps() }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                SessionHistoryHeader()
            }
            
            itemsIndexed(
                items = filteredSessions,
                key = { _, session -> session.id }
            ) { index, session ->
                SessionItem(
                    session = session,
                    index = index,
                    onDelete = { viewModel.deleteSession(it) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
    
    // Dialogs moved to dedicated components
    if (showTimerDialog) {
        TimerDurationDialog(
            currentDuration = timerDuration,
            onDismiss = { showTimerDialog = false },
            onConfirm = { 
                viewModel.setTimerDuration(it)
                showTimerDialog = false 
            }
        )
    }
    
    if (showRepDialog) {
        RepCountDialog(
            currentReps = totalReps,
            onDismiss = { showRepDialog = false },
            onConfirm = { 
                viewModel.setTotalReps(it)
                showRepDialog = false 
            }
        )
    }

    if (showColorDialog) {
        ColorPickerDialog(
            initialColor = Color(timerColor),
            onDismiss = { showColorDialog = false },
            onConfirm = { color ->
                viewModel.setTimerColor(color.toArgb().toLong())
                showColorDialog = false
            }
        )
    }
}
