package com.pinghtdog.amping.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pinghtdog.amping.ui.theme.CyanPrimary
import com.pinghtdog.amping.ui.theme.DarkNavy
import com.pinghtdog.amping.ui.theme.LightBackground
import com.pinghtdog.amping.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onSave: () -> Unit,
    viewModel: NotificationsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Reminders",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DarkNavy
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Time Picker Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            onClick = { showTimePicker = true }
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Daily Dose Time",
                        fontSize = 14.sp,
                        color = TextMuted
                    )
                    Text(
                        text = String.format("%02d:%02d", uiState.reminderHour, uiState.reminderMinute),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavy
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Notification Style
        Text(
            text = "Notification Style",
            modifier = Modifier.align(Alignment.Start),
            fontWeight = FontWeight.Bold,
            color = DarkNavy
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            NotificationStyle.values().forEach { style ->
                val isSelected = uiState.selectedStyle == style
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) CyanPrimary.copy(alpha = 0.1f) else Color.White)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) CyanPrimary else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.updateStyle(style) }
                        .padding(16.dp)
                ) {
                    Text(
                        text = style.displayName,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) CyanPrimary else DarkNavy
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Survey Frequency
        Text(
            text = "How often should we check in?",
            modifier = Modifier.align(Alignment.Start),
            fontWeight = FontWeight.Bold,
            color = DarkNavy
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SurveyFrequency.values().forEach { freq ->
                val isSelected = uiState.selectedFrequency == freq
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) CyanPrimary.copy(alpha = 0.1f) else Color.White)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) CyanPrimary else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.updateFrequency(freq) }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = freq.displayName,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) CyanPrimary else DarkNavy
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Text("Save Preferences", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        if (showTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = uiState.reminderHour,
                initialMinute = uiState.reminderMinute
            )
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.updateTime(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }) { Text("Confirm") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                },
                text = {
                    TimePicker(state = timePickerState)
                }
            )
        }
    }
}
