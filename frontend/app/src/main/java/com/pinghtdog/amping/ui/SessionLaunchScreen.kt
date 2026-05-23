package com.pinghtdog.amping.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.pinghtdog.amping.ui.theme.RedPenalty
import com.pinghtdog.amping.ui.theme.TextDark
import com.pinghtdog.amping.ui.theme.GoldYellow
import com.pinghtdog.amping.ui.theme.Typography
import com.pinghtdog.amping.ui.theme.TextMuted

@Composable
fun SessionLaunchScreen(onMoodSelected: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Stage 1 of 3", color = TextMuted, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        // Animated/Large Gabby Avatar
        Box(
            modifier = Modifier.size(120.dp).clip(CircleShape).background(CyanPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Face, null, tint = Color.White, modifier = Modifier.size(80.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Hi Leo! Ready for today's check-in? How are you feeling overall?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        // Mood Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MoodButton("Great", Icons.Filled.SentimentVerySatisfied, onMoodSelected)
            MoodButton("Okay", Icons.Filled.SentimentSatisfied, onMoodSelected)
            MoodButton("Bad", Icons.Filled.SentimentVeryDissatisfied, onMoodSelected)
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun MoodButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(64.dp).background(Color.White, CircleShape)
        ) {
            Icon(icon, contentDescription = label, tint = CyanPrimary, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontWeight = FontWeight.Bold, color = TextDark)
    }
}