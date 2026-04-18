package com.example.zenbreath.ui.screens.home.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.zenbreath.data.ZenBreathSession
import com.example.zenbreath.ui.components.SessionItem

@Composable
fun SessionHistoryHeader(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Session History Test",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun SessionHistoryList(
    sessions: List<ZenBreathSession>,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        item {
            SessionHistoryHeader()
        }
        itemsIndexed(
            items = sessions,
            key = { _, session -> session.id }
        ) { index, session ->
            SessionItem(
                session = session,
                index = index,
                onDelete = onDelete
            )
        }
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
