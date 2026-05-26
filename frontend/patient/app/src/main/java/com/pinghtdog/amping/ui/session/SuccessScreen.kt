package com.pinghtdog.amping.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pinghtdog.amping.ui.theme.DarkNavy
import com.pinghtdog.amping.ui.theme.GoldYellow
import com.pinghtdog.amping.ui.theme.TextDark

@Composable
fun SuccessScreen(
    viewModel: SessionViewModel,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0EA5E9), // Vibrant Sky Blue
            Color(0xFF0284C7)  // Deep Blue
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Celebratory Stars
        Icon(
            Icons.Filled.Stars,
            contentDescription = "Success Celebration",
            tint = GoldYellow,
            modifier = Modifier.size(110.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Ingestion Streak Message
        Text(
            text = "Day ${uiState.streakCount} Complete!",
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        val encouragingMessage = when (uiState.activeProfile) {
            "youth" -> "Crushed it, Leo! Your streak is locked in. Let's conquer tomorrow! 🚀"
            "senior" -> "Splendid work, Lola. You are doing a wonderful job caring for your health. ❤️"
            else -> "Daily TB medication compliance successfully logged. Your streak record is secure."
        }

        Text(
            text = encouragingMessage,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 17.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // XP Reward Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "+${uiState.xpEarned} XP",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )
                    Text(
                        text = "Daily Ingestion Reward",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                }

                Icon(
                    Icons.Filled.EmojiEvents,
                    "Badge Trophy",
                    tint = GoldYellow,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Finish Session / Back to Dashboard Action
        Button(
            onClick = {
                viewModel.resetSession()
                onGoHome()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Text(
                "Return to Dashboard",
                fontSize = 17.sp,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        }
    }
}
