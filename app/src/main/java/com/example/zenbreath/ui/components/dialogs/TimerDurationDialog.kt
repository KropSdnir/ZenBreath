package com.example.zenbreath.ui.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Dialog for selecting timer duration
 */
@Composable
fun TimerDurationDialog(
    currentDuration: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val durations = listOf(
        30000L to "30 seconds",
        60000L to "1 minute",
        120000L to "2 minutes",
        180000L to "3 minutes",
        300000L to "5 minutes"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text("Select Timer Duration") },
        text = {
            Column {
                durations.forEach { (duration, label) ->
                    Text(
                        text = label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConfirm(duration) }
                            .padding(vertical = 12.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (duration == currentDuration) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
