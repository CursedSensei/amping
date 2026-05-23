package com.pinghtdog.amping.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
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
fun VideoReviewScreen(onUpload: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        // Video Player Placeholder
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f).background(Color.DarkGray, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.PlayCircleOutline, "Play", tint = Color.White, modifier = Modifier.size(64.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onUpload,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Text("Looks good — Upload", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { /* Retake */ },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) {
            Text("Retake", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}