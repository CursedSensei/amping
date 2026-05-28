package com.pinghtdog.amping.ui.session

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pinghtdog.amping.ui.theme.CyanPrimary
import com.pinghtdog.amping.ui.theme.LightBackground
import com.pinghtdog.amping.ui.theme.TextDark
import com.pinghtdog.amping.ui.theme.TextMuted
import com.pinghtdog.amping.ui.theme.RedPenalty
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OfflineQueueScreen(
    viewModel: SessionViewModel,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val gabbyMessage = when (uiState.activeProfile) {
        "youth" -> "Hey champion! The internet went a bit spotty, so I've saved your check-in video safely on your device! No worries at all—it will automatically sync in the background when your connection returns!"
        "senior" -> "Don't worry, ${uiState.firstname} dear. The connection is a bit weak right now, but your check-in video is saved safely on your device. It will upload automatically in the background when the signal gets stronger."
        else -> "Connection offline. Your VDOT encrypted video container has been safely cached into the local background sync queue. Transmission will resume automatically when internet connectivity returns."
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(72.dp)) // debug panel reserve

            // Wifi Off / Offline Header
            Icon(
                Icons.Filled.SignalWifiOff,
                contentDescription = "Offline Queue",
                tint = RedPenalty,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Saved for Later",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Comforting Card from Gabby
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GABBY'S SYNC COMPANION",
                        color = CyanPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = gabbyMessage,
                        color = TextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Queue List Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Local Sandbox Cache (${uiState.offlineQueue.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                
                Text(
                    text = "Encrypted (AES-256)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = CyanPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cached Queue list
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                if (uiState.offlineQueue.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.CloudQueue,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Queue empty — All videos synced!",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.offlineQueue) { entry ->
                            QueueItemRow(entry = entry)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { viewModel.syncOfflineQueue() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Retry Upload Now",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        // Allow user to bypass/finish check-in while uploading occurs in the background
                        viewModel.forcePhase(com.pinghtdog.amping.data.model.SessionPhase.SUCCESS)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.LightGray),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark)
                ) {
                    Text(
                        "Finish Session Now",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun QueueItemRow(entry: com.pinghtdog.amping.data.model.QueueEntry) {
    val formatter = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
    val dateStr = formatter.format(Date(entry.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightBackground),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.LockOpen,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Encrypted VDOT Payload",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextDark
                )
                Text(
                    text = dateStr,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val statusColor = when (entry.status) {
                    "Uploading" -> CyanPrimary
                    "Failed" -> RedPenalty
                    else -> Color.Gray
                }
                
                Text(
                    text = entry.status,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = statusColor
                )
                
                Text(
                    text = "Retries: ${entry.retryCount}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}
