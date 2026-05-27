package com.pinghtdog.amping.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pinghtdog.amping.ui.components.AnimatedGabby
import com.pinghtdog.amping.ui.components.GabbyState
import com.pinghtdog.amping.ui.theme.*

@Composable
fun StreakTutorialScreen(
    onNext: () -> Unit,
    viewModel: StreakTutorialViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Your Streak",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = DarkNavy
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Streak Counter
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(GoldYellow),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${uiState.activeDaysCount}",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavy
                )
                Text(
                    text = "DAYS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavy
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 7-Day Calendar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            (1..7).forEach { day ->
                val isActive = day <= uiState.activeDaysCount
                val bgColor by animateColorAsState(
                    targetValue = if (isActive) GoldYellow else Color.White,
                    animationSpec = tween(500)
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "D$day",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) DarkNavy else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Gabby Voiceover Placeholder
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedGabby(state = GabbyState.SPEAKING, modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = uiState.gabbyMessage,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp,
                    color = DarkNavy
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // FAQ Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.toggleFaq() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "What if I miss a day?",
                        fontWeight = FontWeight.Bold,
                        color = DarkNavy
                    )
                    Icon(
                        imageVector = if (uiState.isFaqExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
                AnimatedVisibility(visible = uiState.isFaqExpanded) {
                    Text(
                        text = "Don't worry! We have a grace window to help you keep your momentum. The most important thing is your health.",
                        modifier = Modifier.padding(top = 12.dp),
                        fontSize = 14.sp,
                        color = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Text("Next", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
