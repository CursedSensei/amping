package com.pinghtdog.amping.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pinghtdog.amping.ui.components.AnimatedGabby
import com.pinghtdog.amping.ui.components.GabbyState
import com.pinghtdog.amping.ui.theme.CyanPrimary
import com.pinghtdog.amping.ui.theme.DarkNavy
import com.pinghtdog.amping.ui.theme.TextMuted

@Composable
fun SplashScreen(
    onInitializationComplete: () -> Unit,
    viewModel: SplashViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startInitialization(onInitializationComplete)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        // Center: Amping Logo (Placeholder)
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AMPING",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = CyanPrimary,
                letterSpacing = 8.sp
            )
            Text(
                text = "Your Adherence Companion",
                fontSize = 14.sp,
                color = TextMuted,
                letterSpacing = 2.sp
            )
        }

        // Bottom: Animated Gabby silhouette rising
        val infiniteTransition = rememberInfiniteTransition(label = "gabby_rise")
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 100f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offsetY"
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = offsetY.dp)
                .padding(bottom = 60.dp)
        ) {
            // Using a silhouette effect by applying a color filter or just using the component if it supports it.
            // For now, just showing Gabby.
            AnimatedGabby(
                state = GabbyState.IDLE,
                modifier = Modifier.size(120.dp)
            )
        }

        // Bottom 5%: Thin skeleton loader
        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.BottomCenter),
                color = CyanPrimary,
                trackColor = Color.Transparent
            )
        }

        // Footer: App version tag
        Text(
            text = uiState.version,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            color = TextMuted.copy(alpha = 0.5f),
            fontSize = 12.sp
        )
    }
}
