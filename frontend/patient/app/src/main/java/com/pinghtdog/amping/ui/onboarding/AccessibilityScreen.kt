package com.pinghtdog.amping.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pinghtdog.amping.ui.theme.CyanPrimary
import com.pinghtdog.amping.ui.theme.DarkNavy
import com.pinghtdog.amping.ui.theme.LightBackground
import com.pinghtdog.amping.ui.theme.TextMuted

@Composable
fun AccessibilityScreen(
    onConfirm: () -> Unit,
    onChangeSetting: (String) -> Unit,
    viewModel: AccessibilityViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Final Check",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DarkNavy
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Does everything look correct?",
            fontSize = 16.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(32.dp))

        val settings = listOf(
            "Language" to uiState.language,
            "Text Size" to uiState.textSize,
            "Voice Mode" to uiState.voiceMode,
            "Reminder" to uiState.reminderTime,
            "Profile Type" to uiState.profileType
        )

        settings.forEach { (label, value) ->
            SettingsSummaryCard(
                label = label,
                value = value,
                onChange = { onChangeSetting(label) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Text("Everything looks good", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SettingsSummaryCard(
    label: String,
    value: String,
    onChange: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = label, fontSize = 12.sp, color = TextMuted)
                Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
            }
            TextButton(onClick = onChange) {
                Text("Change", color = CyanPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
