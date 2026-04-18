package com.example.zenbreath.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import com.example.zenbreath.ui.components.dialogs.ColorPickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zenbreath.ui.screens.home.AdaptiveHomeScreen
import com.example.zenbreath.ui.screens.history.WorkoutHistoryScreen
import com.example.zenbreath.viewmodel.ZenBreathViewModel

/**
 * Main home screen for breathing tracking app.
 * Refactored for scalability following the 500-line threshold rule.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ZenBreathViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // UDF: Single state observation
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showColorDialog by remember { mutableStateOf(false) }
    var showZenIconDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    IconButton(onClick = { viewModel.toggleZenMode() }) {
                        Icon(
                            painter = painterResource(id = com.example.zenbreath.R.drawable.ic_zen_fire_ring),
                            contentDescription = "Toggle Zen Mode",
                            tint = if (uiState.isZenMode) {
                                Color(0xFFE65100)
                            } else if (uiState.zenIconFollowsTimer) {
                                Color(uiState.timerColor)
                            } else {
                                Color(0xFF2D6A4F)
                            }
                        )
                    }
                },
                actions = {
                    // Date Selector moved to the right
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable {
                                val calendar = java.util.Calendar.getInstance()
                                calendar.timeInMillis = uiState.selectedDate

                                android.app.DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val newCalendar = java.util.Calendar.getInstance()
                                        newCalendar.set(year, month, dayOfMonth)
                                        viewModel.setSelectedDate(newCalendar.timeInMillis)
                                    },
                                    calendar.get(java.util.Calendar.YEAR),
                                    calendar.get(java.util.Calendar.MONTH),
                                    calendar.get(java.util.Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                    ) {
                        val dateFormat = remember { java.text.SimpleDateFormat("MM/dd", java.util.Locale.getDefault()) }
                        Text(
                            text = dateFormat.format(java.util.Date(uiState.selectedDate)),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("History") },
                            onClick = {
                                showMenu = false
                                viewModel.toggleHistory()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Timer Color") },
                            onClick = {
                                showMenu = false
                                showColorDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Zen Icon Color") },
                            onClick = {
                                showMenu = false
                                showZenIconDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (uiState.isCountUp) "Set Default: Count Down" else "Set Default: Count Up") },
                            onClick = {
                                showMenu = false
                                viewModel.toggleTimerMode()
                            }
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isHistoryVisible) {
                WorkoutHistoryScreen(
                    workouts = uiState.workouts,
                    onBack = { viewModel.toggleHistory() },
                    onDelete = { /* Add delete logic if needed */ },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AdaptiveHomeScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
    
    if (showColorDialog) {
        ColorPickerDialog(
            initialColor = Color(uiState.timerColor),
            onDismiss = { showColorDialog = false },
            onConfirm = { color ->
                viewModel.setTimerColor(color.toArgb().toLong())
                showColorDialog = false
            }
        )
    }

    if (showZenIconDialog) {
        AlertDialog(
            onDismissRequest = { showZenIconDialog = false },
            title = { Text("Zen Icon Color") },
            text = {
                Column {
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setZenIconFollowsTimer(false)
                                showZenIconDialog = false
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = !uiState.zenIconFollowsTimer,
                            onClick = {
                                viewModel.setZenIconFollowsTimer(false)
                                showZenIconDialog = false
                            }
                        )
                        Text("App theme color (Deep Zen Green)")
                    }
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setZenIconFollowsTimer(true)
                                showZenIconDialog = false
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = uiState.zenIconFollowsTimer,
                            onClick = {
                                viewModel.setZenIconFollowsTimer(true)
                                showZenIconDialog = false
                            }
                        )
                        Text("Timer color")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showZenIconDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
