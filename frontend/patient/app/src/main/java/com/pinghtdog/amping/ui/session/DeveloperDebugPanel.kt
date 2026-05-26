package com.pinghtdog.amping.ui.session

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pinghtdog.amping.data.model.SessionPhase
import com.pinghtdog.amping.ui.theme.CyanPrimary
import com.pinghtdog.amping.ui.theme.DarkNavy
import com.pinghtdog.amping.ui.theme.RedPenalty

@Composable
fun DeveloperDebugPanel(
    viewModel: SessionViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val rotateArrow by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "rotate")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.White.copy(0.3f), Color.White.copy(0.1f))
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xDD1E293B) // High tech semi-transparent dark navy
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.BugReport,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DEVELOPER DEMO CONTROLLER",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Icon(
                    Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .rotate(rotateArrow)
                        .size(24.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(
                        Modifier,
                        DividerDefaults.Thickness,
                        color = Color.White.copy(alpha = 0.15f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Patient Profile Selection
                    Text(
                        text = "Patient Profile (Swaps UX Voice & Styling)",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileButton("Youth (Leo)", active = uiState.activeProfile == "youth") {
                            viewModel.selectProfile("youth")
                        }
                        ProfileButton("Senior (Lola)", active = uiState.activeProfile == "senior") {
                            viewModel.selectProfile("senior")
                        }
                        ProfileButton("Adult (Standard)", active = uiState.activeProfile == "adult") {
                            viewModel.selectProfile("adult")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Active Phase Override
                    Text(
                        text = "Force Transition Phase (Jetpack Compose MVVM State)",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PhaseSelectorButton(
                                "1. Conversing",
                                active = uiState.currentPhase == SessionPhase.CONVERSATION && uiState.emergencyState == null,
                                modifier = Modifier.weight(1f)
                            ) {
                                viewModel.forcePhase(SessionPhase.CONVERSATION)
                            }
                            PhaseSelectorButton(
                                "2. Checklist",
                                active = uiState.currentPhase == SessionPhase.SYMPTOM_LOGGING,
                                modifier = Modifier.weight(1f)
                            ) {
                                viewModel.forcePhase(SessionPhase.SYMPTOM_LOGGING)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PhaseSelectorButton(
                                "3. VDOT Record",
                                active = uiState.currentPhase == SessionPhase.VDOT_CAPTURE,
                                modifier = Modifier.weight(1f)
                            ) {
                                viewModel.forcePhase(SessionPhase.VDOT_CAPTURE)
                            }
                            PhaseSelectorButton(
                                "4. VDOT Review",
                                active = uiState.currentPhase == SessionPhase.VDOT_REVIEW,
                                modifier = Modifier.weight(1f)
                            ) {
                                viewModel.forcePhase(SessionPhase.VDOT_REVIEW)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PhaseSelectorButton(
                                "5. Complete Success",
                                active = uiState.currentPhase == SessionPhase.SUCCESS,
                                modifier = Modifier.weight(1f)
                            ) {
                                viewModel.forcePhase(SessionPhase.SUCCESS)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Clinical Crisis Override
                    Text(
                        text = "Critical Clinical Overrides",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.triggerEmergencyOverride("Emergency Self-Harm threat detected by clinical supervisor override.")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RedPenalty
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Simulate Self-Harm / Crisis Tool Call",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Modal Backend Integration & Diagnostics
                    Text(
                        text = "Modal Backend Integration",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Real Network Mode (Modal)",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "JWT Handshake + Ktor WebSocket Stream",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = uiState.isNetworkMode,
                            onCheckedChange = { viewModel.toggleNetworkMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyanPrimary,
                                checkedTrackColor = CyanPrimary.copy(alpha = 0.4f)
                            )
                        )
                    }

                    // Display visual error block if Ktor encounters communication faults
                    uiState.networkError?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = RedPenalty.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, RedPenalty, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "BACKEND ERROR DETECTED",
                                        color = RedPenalty,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "DISMISS",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp,
                                        modifier = Modifier.clickable { viewModel.dismissNetworkError() }
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = error,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(
                        Modifier,
                        DividerDefaults.Thickness,
                        color = Color.White.copy(alpha = 0.15f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))


                    // Telemetry Output View
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TelemetryText("Phase: ${uiState.currentPhase}")
                        TelemetryText("Symptoms: ${uiState.selectedSymptoms.size}")
                        TelemetryText("Emergency: ${if (uiState.emergencyState != null) "ACTIVE" else "None"}")
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.ProfileButton(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) CyanPrimary else Color.White.copy(alpha = 0.08f))
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = if (active) CyanPrimary else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (active) DarkNavy else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Composable
fun PhaseSelectorButton(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) Color(0xFF10B981) else Color.White.copy(alpha = 0.08f))
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = if (active) Color(0xFF10B981) else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (active) Color.White else Color.LightGray,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

@Composable
fun TelemetryText(text: String) {
    Text(
        text = text,
        color = Color.Gray,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium
    )
}
