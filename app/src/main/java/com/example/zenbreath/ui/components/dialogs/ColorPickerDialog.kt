package com.example.zenbreath.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (Color) -> Unit
) {
    // Current state of the color being picked
    var selectedColor by remember { mutableStateOf(initialColor) }
    
    // Derived hue for the slider
    var hue by remember { 
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(initialColor.toArgb(), hsv)
        mutableStateOf(hsv[0]) 
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Timer Color") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Color Preview
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(selectedColor, CircleShape)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Show Hex instead of Hue
                val hexCode = String.format("#%06X", (selectedColor.toArgb() and 0xFFFFFF))
                Text(
                    text = "Color: $hexCode",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Slider(
                    value = hue,
                    onValueChange = { newHue -> 
                        hue = newHue
                        // Update selected color while preserving saturation and value if it's not the default
                        val hsv = FloatArray(3)
                        android.graphics.Color.colorToHSV(selectedColor.toArgb(), hsv)
                        hsv[0] = newHue
                        // If we are coming from a custom state, we might want to ensure a minimum visibility
                        if (hsv[1] < 0.1f) hsv[1] = 0.8f
                        if (hsv[2] < 0.1f) hsv[2] = 0.9f
                        selectedColor = Color(android.graphics.Color.HSVToColor(hsv))
                    },
                    valueRange = 0f..360f,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        // Reset EXACTLY to Zen Green Primary (#2D6A4F)
                        val zenGreen = Color(0xFF2D6A4F)
                        selectedColor = zenGreen
                        val hsv = FloatArray(3)
                        android.graphics.Color.colorToHSV(zenGreen.toArgb(), hsv)
                        hue = hsv[0]
                    }
                ) {
                    Text("Default")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                TextButton(onClick = { onConfirm(selectedColor) }) {
                    Text("Confirm")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


