package com.pinghtdog.amping.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
fun WelcomeScreen(
    onBegin: () -> Unit,
    viewModel: WelcomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Celebration Animation Placeholder
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedGabby(
                state = GabbyState.SPEAKING,
                modifier = Modifier.size(300.dp)
            )
            
            // Celebration "Sparkles" Placeholder
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = GoldYellow,
                modifier = Modifier
                    .size(48.dp)
                    .offset(x = (-120).dp, y = (-120).dp)
            )
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = CyanPrimary,
                modifier = Modifier
                    .size(32.dp)
                    .offset(x = 100.dp, y = (-150).dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = uiState.welcomeMessage,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Streak Badge
        Surface(
            color = GoldYellow,
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = DarkNavy,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = uiState.streakBadgeText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = DarkNavy
                )
            }
        }

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = onBegin,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Text("Begin", fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
