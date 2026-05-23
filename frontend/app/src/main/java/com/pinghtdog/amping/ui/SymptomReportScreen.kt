package com.pinghtdog.amping.ui


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SymptomReportScreen(onSymptomsLogged: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(LightBackground).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Stage 2 of 3", color = TextMuted, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "How has your body felt today?",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val symptoms = listOf("Nausea", "Dizziness", "Rash", "Fatigue", "None of these")
            symptoms.forEach { symptom ->
                Button(
                    onClick = { /* Toggle */ },
                    modifier = Modifier.height(56.dp), // Accessible tap target
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = TextDark),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(2.dp)
                ) {
                    Text(symptom, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onSymptomsLogged,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Text("Continue", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}