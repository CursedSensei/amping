package com.pinghtdog.amping.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pinghtdog.amping.ui.components.AnimatedGabby
import com.pinghtdog.amping.ui.components.GabbyIdle
import com.pinghtdog.amping.ui.components.GabbyState
import com.pinghtdog.amping.ui.components.PetBackground
import com.pinghtdog.amping.ui.theme.CyanPrimary
import com.pinghtdog.amping.ui.theme.DarkNavy
import com.pinghtdog.amping.ui.theme.LightBackground
import com.pinghtdog.amping.ui.theme.RedPenalty
import com.pinghtdog.amping.ui.theme.TextDark
import com.pinghtdog.amping.ui.theme.TextMuted

@Composable
fun DashBoard(
    onStartSession: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

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

            // Debug Profile Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkNavy.copy(alpha = 0.9f))
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileType.values().forEach { type ->
                    val isActive = uiState.profileType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isActive) CyanPrimary else Color.White.copy(alpha = 0.1f))
                            .clickable { viewModel.selectProfile(type) }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type.name,
                            color = if (isActive) DarkNavy else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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

            // Hero Header — profile-aware
            if (uiState.profileType == ProfileType.KIDS) {
                KidsHeroHeader(
                    firstname = uiState.firstname,
                    streak = uiState.currentStreak,
                    currentDay = uiState.currentDay
                )
            } else {
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
            }

            // --- PROFILE SPECIFIC CONTENT ---
            when (uiState.profileType) {
                ProfileType.KIDS -> {
                    Spacer(modifier = Modifier.height(24.dp))

                    QuestMapCard(
                        streak = uiState.currentStreak,
                        currentDay = uiState.currentDay,
                        totalRegimenDays = uiState.totalRegimenDays,
                        onQuestClick = onStartSession
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KidsStatCard(
                            modifier = Modifier.weight(1f),
                            emoji = "🔥",
                            value = "${uiState.currentStreak}",
                            label = "FIRE STREAK"
                        )
                        KidsStatCard(
                            modifier = Modifier.weight(1f),
                            emoji = "🛡️",
                            value = "${((uiState.currentDay - 1) % 7 + 1)}/7",
                            label = "DAYS SAVED"
                        )
                    }
                }
                ProfileType.ADULTS -> {
                    Spacer(modifier = Modifier.height(32.dp))
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
                        currentDay = uiState.currentDay,
                        totalRegimenDays = uiState.totalRegimenDays,
                        gracePeriodHours = uiState.gracePeriodHours
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    WeeklyMissionsCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    )
                }
                ProfileType.SENIORS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TODAY'S STATUS:",
                            color = TextMuted,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (uiState.isTodayTaken) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.clickable { viewModel.toggleTodayTaken() }
                        ) {
                            Text(
                                text = if (uiState.isTodayTaken) "TAKEN" else "NOT YET\nTAKEN",
                                color = if (uiState.isTodayTaken) Color(0xFF2E7D32) else RedPenalty,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                lineHeight = 56.sp,
                                modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp)
                            )
                        }
                    }
                }
            }
            // --- END PROFILE SPECIFIC CONTENT ---

            Spacer(modifier = Modifier.height(24.dp))

            // Penalty Warning Card
            if (!uiState.isTodayTaken) Card(
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
fun StreakCircularWidget(
    streak: Int,
    currentDay: Int,
    totalRegimenDays: Int,
    gracePeriodHours: Long
) {
    val progressSweep = (currentDay.toFloat() / totalRegimenDays.coerceAtLeast(1)) * 330f
    val graceSweep = ((gracePeriodHours.toFloat() / (7 * 24)) * 60f).coerceIn(0f, 60f)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(220.dp)) {
                val inset = 30.dp.toPx()
                val innerTopLeft = Offset(inset, inset)
                val innerSize = Size(size.width - 2 * inset, size.height - 2 * inset)

                // Outer background track (progress)
                drawArc(
                    color = Color.Black.copy(alpha = 0.06f),
                    startAngle = 0f, sweepAngle = 360f, useCenter = false,
                    style = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Round)
                )
                // Progress Arc (Blue) – outer ring
                if (progressSweep > 0f) {
                    drawArc(
                        color = CyanPrimary,
                        startAngle = -90f, sweepAngle = progressSweep, useCenter = false,
                        style = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                // Inner background track (grace period)
                drawArc(
                    color = Color.Black.copy(alpha = 0.06f),
                    startAngle = 0f, sweepAngle = 360f, useCenter = false,
                    topLeft = innerTopLeft, size = innerSize,
                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                )
                // Grace Arc (Red) – inner ring
                if (graceSweep > 0f) {
                    drawArc(
                        color = RedPenalty,
                        startAngle = -90f, sweepAngle = graceSweep, useCenter = false,
                        topLeft = innerTopLeft, size = innerSize,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Labels overlay
            Box(modifier = Modifier.fillMaxSize()) {
                ArcLabel(
                    label = "PROGRESS",
                    color = CyanPrimary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 32.dp, end = 16.dp)
                )
                ArcLabel(
                    label = "GRACE PERIOD",
                    color = RedPenalty,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 72.dp, start = 8.dp)
                )
            }

            // Center Text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$streak", fontSize = 72.sp, fontWeight = FontWeight.Black, color = TextDark)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LightBackground),
                    elevation = CardDefaults.cardElevation(0.dp)
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
}

@Composable
fun ArcLabel(label: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
        }
    }
}

enum class DayState { DONE, MISSED, UPCOMING_DASHED, UPCOMING_SOLID }

@Composable
fun WeeklyMissionsCard(modifier: Modifier = Modifier) {
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")
    val states = listOf(
        DayState.DONE, DayState.MISSED, DayState.DONE,
        DayState.DONE, DayState.DONE, DayState.DONE, DayState.DONE
    )
    val doneCount = states.count { it == DayState.DONE }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WEEKLY MISSIONS",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = TextDark,
                    letterSpacing = 1.sp
                )
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE57F))
                ) {
                    Text(
                        text = "$doneCount/7 DONE",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = Color(0xFF8B6914)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                labels.zip(states).forEach { (label, state) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        when (state) {
                            DayState.DONE -> Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✓", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                            DayState.MISSED -> Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(RedPenalty),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✕", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                            DayState.UPCOMING_DASHED -> Canvas(modifier = Modifier.size(36.dp)) {
                                drawCircle(
                                    color = Color(0xFFBDBDBD),
                                    radius = size.minDimension / 2 - 3.dp.toPx(),
                                    style = Stroke(
                                        width = 3.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                                    )
                                )
                            }
                            DayState.UPCOMING_SOLID -> Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0E0E0))
                            ) {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KidsHeroHeader(firstname: String, streak: Int, currentDay: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyanPrimary)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Hi, $firstname!",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun QuestMapCard(
    streak: Int,
    currentDay: Int,
    totalRegimenDays: Int,
    onQuestClick: () -> Unit
) {
    val mapGreen = Color(0xFF3EC97A)
    val mapDarkGreen = Color(0xFF2BA05A)
    val questPink = Color(0xFFFF4081)
    val nodeGold = Color(0xFFFFB800)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = mapGreen),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("🗺", fontSize = 22.sp)
                Text(
                    text = "QUEST MAP",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 2.sp
                )
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .padding(bottom = 20.dp)
            ) {
                val w = maxWidth
                val h = maxHeight

                // Node positions (fractional)
                val n1x = w * 0.18f;  val n1y = h * 0.52f  // completed – campfire
                val n2x = w * 0.50f;  val n2y = h * 0.65f  // current   – Gabby
                val n3x = w * 0.82f;  val n3y = h * 0.38f  // future    – castle

                // Dashed path between nodes
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val path = Path().apply {
                        moveTo(size.width * 0.18f, size.height * 0.52f)
                        cubicTo(
                            size.width * 0.28f, size.height * 0.10f,
                            size.width * 0.42f, size.height * 0.90f,
                            size.width * 0.50f, size.height * 0.65f
                        )
                        cubicTo(
                            size.width * 0.58f, size.height * 0.40f,
                            size.width * 0.72f, size.height * 0.10f,
                            size.width * 0.82f, size.height * 0.38f
                        )
                    }
                    drawPath(
                        path = path,
                        color = mapDarkGreen,
                        style = Stroke(
                            width = 7.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f)
                        )
                    )
                }

                // Node 1 — completed (campfire, gold)
                Box(
                    modifier = Modifier
                        .offset(x = n1x - 28.dp, y = n1y - 28.dp)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(nodeGold),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏕️", fontSize = 24.sp)
                }

                // Node 2 — current quest (Gabby as hero)
                Box(
                    modifier = Modifier
                        .offset(x = n2x - 40.dp, y = n2y - 40.dp)
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(questPink)
                        .clickable { onQuestClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            GabbyIdle(modifier = Modifier.size(44.dp))
                            Text(
                                text = "QUEST",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = questPink
                            )
                        }
                    }
                }

                // Node 3 — future/locked (castle, dark green)
                Box(
                    modifier = Modifier
                        .offset(x = n3x - 26.dp, y = n3y - 26.dp)
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(mapDarkGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏰", fontSize = 22.sp)
                }
            }
        }
    }
}

@Composable
fun KidsStatCard(modifier: Modifier = Modifier, emoji: String, value: String, label: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 28.sp)
            Text(
                text = value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = TextDark
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
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
