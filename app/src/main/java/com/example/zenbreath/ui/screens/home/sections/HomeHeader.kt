package com.example.zenbreath.ui.screens.home.sections

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeHeader(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    
    Text(
        text = dateFormat.format(Date(selectedDate)),
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold
        ),
        modifier = modifier
            .padding(vertical = 8.dp)
            .clickable {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = selectedDate

                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val newCalendar = Calendar.getInstance()
                        newCalendar.set(year, month, dayOfMonth)
                        onDateSelected(newCalendar.timeInMillis)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
        color = MaterialTheme.colorScheme.primary
    )
}
