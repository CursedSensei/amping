package com.pinghtdog.amping.ui


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pinghtdog.amping.ui.theme.CyanPrimary
import com.pinghtdog.amping.ui.theme.DarkNavy
import com.pinghtdog.amping.ui.theme.LightBackground
import com.pinghtdog.amping.ui.theme.RedPenalty
import com.pinghtdog.amping.ui.theme.TextDark
import com.pinghtdog.amping.ui.theme.TextMuted
import com.pinghtdog.amping.ui.components.GabbyIdle


@Composable
fun DashBoard(onStartSession: () -> Unit) {
    Scaffold(
        bottomBar = { CustomBottomNavigation(onGabbyClick = onStartSession) },
        containerColor = LightBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Profile Switcher Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkNavy)
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Leo (Kid - 12)",
                    color = CyanPrimary,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Text(
                    text = "Lola (Senior - 68)",
                    color = Color.LightGray
                )
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
                            text = "Hi, Leo!",
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

            Spacer(modifier = Modifier.height(24.dp))

            GabbyIdle(
                modifier = Modifier.size(120.dp)
            )

            // Streak Widget
            StreakCircularWidget()

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
        }
    }
}

@Composable
fun StreakCircularWidget() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {
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
                startAngle = -90f, sweepAngle = 220f, useCenter = false,
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
            Text(text = "4", fontSize = 72.sp, fontWeight = FontWeight.Black, color = TextDark)
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
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /*TODO*/ }) { Icon(Icons.Outlined.CalendarToday, null, tint = TextMuted) }
                IconButton(onClick = { /*TODO*/ }) { Icon(Icons.Outlined.TrendingUp, null, tint = TextMuted) }
                Spacer(modifier = Modifier.width(56.dp)) // Space for FAB
                IconButton(onClick = { /*TODO*/ }) { Icon(Icons.Outlined.Person, null, tint = TextMuted) }
                IconButton(onClick = { /*TODO*/ }) { Icon(Icons.Outlined.Settings, null, tint = TextMuted) }
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
            Icon(Icons.Filled.Face, contentDescription = "Talk to Gabby", tint = Color.White, modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
fun GabbyIdle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(CyanPrimary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.Face,
            contentDescription = "Gabby Idle",
            tint = Color.White,
            modifier = Modifier.size(80.dp)
        )
    }
}