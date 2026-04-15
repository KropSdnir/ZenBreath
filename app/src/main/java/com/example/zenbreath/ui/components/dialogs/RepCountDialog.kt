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
 * Dialog for selecting rep count
 */
@Composable
fun RepCountDialog(
    currentReps: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val repCounts = listOf(5, 10, 15, 20, 25, 30)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text("Select Rep Count") },
        text = {
            Column {
                repCounts.forEach { count ->
                    Text(
                        text = "$count reps",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConfirm(count) }
                            .padding(vertical = 12.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (count == currentReps) 
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
