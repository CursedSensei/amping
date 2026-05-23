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
fun SuccessScreen(onGoHome: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(CyanPrimary).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Stars, contentDescription = "Success", tint = GoldYellow, modifier = Modifier.size(100.dp))
        Spacer(modifier = Modifier.height(24.dp))

        Text("Day 5 Complete!", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
        Text("Amazing work. See you tomorrow!", color = Color.White, fontSize = 18.sp)

        Spacer(modifier = Modifier.height(32.dp))

        // XP Earned Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("+120 XP", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                Icon(Icons.Filled.EmojiEvents, "Badge", tint = GoldYellow, modifier = Modifier.size(40.dp))
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onGoHome,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
        ) {
            Text("Go Home", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}