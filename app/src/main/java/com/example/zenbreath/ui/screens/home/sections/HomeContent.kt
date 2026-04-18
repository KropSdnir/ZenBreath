package com.example.zenbreath.ui.screens.home.sections

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.zenbreath.ui.components.ZenBreathFireTimer
import com.example.zenbreath.ui.components.ZenBreathFireTimerUiState
import com.example.zenbreath.ui.components.SessionItem
import com.example.zenbreath.ui.screens.home.ZenBreathHomeUiState

@Composable
fun HomeContent(
    uiState: ZenBreathHomeUiState,
    onDateSelected: (Long) -> Unit,
    onUpdateTimer: (Int) -> Unit,
    onUpdateReps: (Int) -> Unit,
    onUpdateTarget: (Int) -> Unit,
    onStartStopClick: () -> Unit,
    onWorkoutToggle: () -> Unit,
    onResetRepsClick: () -> Unit,
    onDeleteSession: (Long) -> Unit,
    windowWidthSizeClass: WindowWidthSizeClass,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Workout Lifecycle & Control (Zen Mode only: Hidden)
            androidx.compose.animation.AnimatedVisibility(
                visible = !uiState.isZenMode,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
            ) {
                Column {
                    // Workout Lifecycle Timestamps & Duration
                    // Fixed-height row: all three slots always rendered to prevent vertical layout shifts.
                    if (uiState.workoutStartTime > 0) {
                        val timeFormat = remember { java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            // MANUAL TUNING: CenterVertically keeps all three labels on the same baseline
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Start Time — always visible once session begins
                            Text(
                                text = "S: ${timeFormat.format(java.util.Date(uiState.workoutStartTime))}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Duration (Center) — placeholder "—" keeps the slot reserved before session ends
                            val durationText = if (uiState.workoutEndTime > 0) {
                                val durSecs = (uiState.workoutEndTime - uiState.workoutStartTime) / 1000
                                if (durSecs >= 60) "${durSecs / 60}m ${durSecs % 60}s" else "${durSecs}s"
                            } else {
                                "—"
                            }
                            Text(
                                text = durationText,
                                // MANUAL TUNING: Same style as start/end so row height never changes
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (uiState.workoutEndTime > 0)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.outline
                            )

                            // End Time — placeholder keeps SpaceBetween alignment stable
                            val endText = when {
                                uiState.workoutEndTime > 0 ->
                                    "E: ${timeFormat.format(java.util.Date(uiState.workoutEndTime))}"
                                uiState.isWorkoutActive -> "E: --:--:--"
                                else -> "E: --:--:--"
                            }
                            Text(
                                text = endText,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (uiState.workoutEndTime > 0)
                                    MaterialTheme.colorScheme.secondary
                                else
                                    MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    // Consolidated Dynamic Session Button (Approved Refinement #2)
                    val isEndAction = uiState.isWorkoutActive
                    Button(
                        onClick = onWorkoutToggle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = if (isEndAction) {
                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                        } else {
                            ButtonDefaults.buttonColors()
                        },
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Icon(
                            imageVector = if (isEndAction) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = if (isEndAction) "End Session" else "Start Session",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isEndAction) "End Session" else "Start Session")
                    }
                }
            }

            // Circular Timer
            ZenBreathFireTimer(
                uiState = ZenBreathFireTimerUiState(
                    remainingMillis = uiState.remainingTime,
                    totalMillis = uiState.timerDuration,
                    isFinished = uiState.isRunning && if (uiState.isCountUp) false else uiState.remainingTime <= 0L,
                    isCountUp = uiState.isCountUp,
                    isZenMode = uiState.isZenMode,
                    targetSeconds = uiState.targetSeconds,
                    color = Color(uiState.timerColor),
                    currentHeartRate = uiState.currentHeartRate
                ),
                onToggleTimer = onStartStopClick,
                modifier = Modifier.size(280.dp)
            )

            // Settings & Meta (Zen Mode only: Hidden)
            androidx.compose.animation.AnimatedVisibility(
                visible = !uiState.isZenMode,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Rep Counter (Moved Up)
                    RepCounter(
                        currentRep = uiState.currentRep,
                        totalReps = uiState.totalReps,
                        isRunning = uiState.isRunning,
                        onResetClick = onResetRepsClick,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    // Combined Settings Row (Timer/Reps)
                    SessionSettingsRow(
                        timerDurationSeconds = uiState.timerDuration / 1000,
                        totalReps = uiState.totalReps,
                        targetSeconds = uiState.targetSeconds,
                        onUpdateTimer = onUpdateTimer,
                        onUpdateReps = onUpdateReps,
                        onUpdateTarget = onUpdateTarget,
                        isRunning = uiState.isRunning,
                        isCountUp = uiState.isCountUp,
                        windowWidthSizeClass = windowWidthSizeClass,
                        modifier = Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = MaterialTheme.shapes.medium
                        ).padding(8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    SessionHistoryHeader()
                }
            }
        }
        
        if (!uiState.isZenMode) {
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
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
