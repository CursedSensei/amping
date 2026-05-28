package com.pinghtdog.amping.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pinghtdog.amping.ui.theme.CyanPrimary
import com.pinghtdog.amping.ui.theme.DarkNavy
import com.pinghtdog.amping.ui.theme.LightBackground
import com.pinghtdog.amping.ui.theme.RedPenalty
import com.pinghtdog.amping.ui.theme.TextDark
import com.pinghtdog.amping.ui.theme.TextMuted
import com.pinghtdog.amping.ui.components.GabbyIdle
import com.pinghtdog.amping.ui.components.AnimatedGabby
import com.pinghtdog.amping.ui.components.GabbyState
import com.pinghtdog.amping.ui.components.PetBackground
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun DashBoard(
    onStartSession: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var gabbyState by remember { mutableStateOf(GabbyState.IDLE) }

    Scaffold(
        bottomBar = { CustomBottomNavigation(onGabbyClick = onStartSession) },
        containerColor = LightBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Interactive Server Toggle Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkNavy)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.firstname} (Patient Profile)",
                    color = CyanPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.toggleNetworkMode(!uiState.isNetworkMode) }
                ) {
                    Text(
                        text = "Connection: ",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = if (uiState.isNetworkMode) "LOCAL SERVER" else "OFFLINE MOCK",
                        color = if (uiState.isNetworkMode) CyanPrimary else RedPenalty,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Loading / Error Banners
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = CyanPrimary)
                }
            }

            uiState.errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = RedPenalty.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        color = RedPenalty,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
            }

            // Hero Header
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "HERO DASHBOARD",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Hi, ${uiState.firstname}!",
                            color = TextDark,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE1F5FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.SportsSoccer, contentDescription = "Avatar", tint = Color.Blue)
                    }
                }
            }

            // --- GABBY ROOM AREA ---
//            Box(
//                modifier = Modifier.fillMaxWidth(),
//                contentAlignment = Alignment.Center
//            ) {
//                // Background layer
//                PetBackground(modifier = Modifier.matchParentSize())
//
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
                    Spacer(modifier = Modifier.height(150.dp))

                    // Central Gabby - Like Pou
                    AnimatedGabby(
                        state = gabbyState,
                        modifier = Modifier
                            .size(320.dp)
                            .clickable {
                                gabbyState = if (gabbyState == GabbyState.IDLE) GabbyState.SPEAKING else GabbyState.IDLE
                            }
                    )

                    Spacer(modifier = Modifier.height(30.dp))
//                }
//            }
//            // --- END GABBY ROOM AREA ---

            // Section Header for Stats
            Text(
                text = "YOUR PROGRESS",
                color = TextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Streak Widget
            StreakCircularWidget(
                streak = uiState.currentStreak,
                bestStreak = uiState.bestStreak
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Penalty Warning Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = CardDefaults.cardColors(containerColor = RedPenalty),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = "Warning",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PENALTY WARNING",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Complete your log to save your streak! Window closing soon.",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StreakCircularWidget(streak: Int, bestStreak: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {
        val total = if (bestStreak > 0) bestStreak else 1
        val ratio = streak.toFloat() / total
        val sweepAngle = (ratio * 360f).coerceIn(0f, 360f)

        Canvas(modifier = Modifier.size(200.dp)) {
            // Background Track
            drawArc(
                color = Color.Black.copy(alpha = 0.05f),
                startAngle = 0f, sweepAngle = 360f, useCenter = false,
                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
            )
            // Quota Arc (Blue)
            drawArc(
                color = CyanPrimary,
                startAngle = -90f, sweepAngle = sweepAngle, useCenter = false,
                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
            )
            // Grace Arc (Red)
            drawArc(
                color = RedPenalty,
                startAngle = -90f, sweepAngle = 45f, useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Center Text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$streak", fontSize = 72.sp, fontWeight = FontWeight.Black, color = TextDark)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Text(
                    text = "DAY STREAK",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun CustomBottomNavigation(onGabbyClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        // Bottom Bar Background
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(72.dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Navigate Home */ }) { 
                    Icon(Icons.Outlined.Home, null, tint = CyanPrimary) 
                }
                
                Spacer(modifier = Modifier.width(72.dp)) // Space for Gabby FAB
                
                IconButton(onClick = { /* Navigate Settings */ }) { 
                    Icon(Icons.Outlined.Settings, null, tint = TextMuted) 
                }
            }
        }

        // Center Gabby FAB
        FloatingActionButton(
            onClick = onGabbyClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(72.dp)
                .padding(bottom = 8.dp),
            shape = CircleShape,
            containerColor = CyanPrimary
        ) {
            GabbyIdle(
                modifier = Modifier.size(120.dp)
            )
        }
    }
}
