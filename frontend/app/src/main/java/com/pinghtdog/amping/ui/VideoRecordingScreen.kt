package com.pinghtdog.amping.ui


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
fun VideoRecordingScreen(onVideoRecorded: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Camera Preview Placeholder
        Text("Camera Preview Active", color = Color.Gray, modifier = Modifier.align(Alignment.Center))

        // Gabby Prompt Overlay
        Card(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp, start = 24.dp, end = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xCC000000))
        ) {
            Text(
                text = "Show me your medicine, then swallow.",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        // Recording Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {}) { Icon(Icons.Filled.Close, "Cancel", tint = Color.White) }

            // Record Button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White, CircleShape)
                    .padding(4.dp)
            ) {
                Button(
                    onClick = onVideoRecorded,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = RedPenalty),
                    modifier = Modifier.fillMaxSize()
                ) {}
            }

            IconButton(onClick = {}) { Icon(Icons.Filled.FlipCameraAndroid, "Flip", tint = Color.White) }
        }
    }
}