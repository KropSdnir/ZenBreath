package com.example.zenbreath.ui.screens.home

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import com.example.zenbreath.ui.screens.home.sections.HomeContent
import com.example.zenbreath.ui.screens.home.sections.SessionHistoryList
import com.example.zenbreath.viewmodel.ZenBreathViewModel
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AdaptiveHomeScreen(
    viewModel: ZenBreathViewModel,
    modifier: Modifier = Modifier,
    adaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val windowSizeClass = adaptiveInfo.windowSizeClass.windowWidthSizeClass
    val isExpanded = windowSizeClass == WindowWidthSizeClass.EXPANDED
    
    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()

    if (isExpanded) {
        // Expanded layout (e.g., Z Fold6 unfolded or Tablet)
        Row(modifier = modifier.fillMaxSize()) {
            HomeContent(
                uiState = uiState,
                onDateSelected = { viewModel.setSelectedDate(it) },
                onUpdateTimer = { viewModel.updateTimerDuration(it) },
                onUpdateReps = { viewModel.updateTotalReps(it) },
                onUpdateTarget = { viewModel.updateTargetSeconds(it) },
                windowWidthSizeClass = windowSizeClass,
                onStartStopClick = {
                    if (uiState.isRunning) viewModel.stopExercise() else viewModel.startExercise()
                },
                onWorkoutToggle = {
                    if (uiState.isWorkoutActive) viewModel.stopWorkout() else viewModel.startWorkout()
                },
                onResetRepsClick = { viewModel.resetReps() },
                onDeleteSession = { viewModel.deleteSession(it) },
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            )
            
            VerticalDivider(modifier = Modifier.fillMaxHeight().width(1.dp))
            
            SessionHistoryList(
                sessions = uiState.filteredSessions,
                onDelete = { viewModel.deleteSession(it) },
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            )
        }
    } else {
        // Compact layout (e.g., Z Fold6 folded or Phone)
        ListDetailPaneScaffold(
            modifier = modifier,
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                HomeContent(
                    uiState = uiState,
                    onDateSelected = { viewModel.setSelectedDate(it) },
                    onUpdateTimer = { viewModel.updateTimerDuration(it) },
                    onUpdateReps = { viewModel.updateTotalReps(it) },
                    onUpdateTarget = { viewModel.updateTargetSeconds(it) },
                    windowWidthSizeClass = windowSizeClass,
                onStartStopClick = {
                    if (uiState.isRunning) viewModel.stopExercise() else viewModel.startExercise()
                },
                onWorkoutToggle = {
                    if (uiState.isWorkoutActive) viewModel.stopWorkout() else viewModel.startWorkout()
                },
                onResetRepsClick = { viewModel.resetReps() },
                onDeleteSession = { viewModel.deleteSession(it) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                )
            },
            detailPane = {
                SessionHistoryList(
                    sessions = uiState.filteredSessions,
                    onDelete = { viewModel.deleteSession(it) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
        )
    }
}
