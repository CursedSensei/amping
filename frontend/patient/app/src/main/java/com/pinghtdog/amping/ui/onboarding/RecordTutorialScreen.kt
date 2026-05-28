package com.pinghtdog.amping.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pinghtdog.amping.ui.theme.CyanPrimary
import com.pinghtdog.amping.ui.theme.DarkNavy
import com.pinghtdog.amping.ui.theme.LightBackground
import com.pinghtdog.amping.ui.theme.TextMuted

@Composable
fun RecordTutorialScreen(
    onReady: () -> Unit,
    onPractice: () -> Unit,
    viewModel: RecordTutorialViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val step = uiState.currentStep

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "How to Record",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DarkNavy
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Step Indicator
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            uiState.steps.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 8.dp)
                        .clip(CircleShape)
                        .background(if (index <= uiState.currentStepIndex) CyanPrimary else Color.LightGray)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Illustration Placeholder
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = step.icon,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = CyanPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = step.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkNavy
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = step.description,
            fontSize = 16.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.currentStepIndex > 0) {
                OutlinedButton(
                    onClick = { viewModel.previousStep() },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Back")
                }
            }

            Button(
                onClick = { 
                    if (uiState.isLastStep) {
                        // Keep state or handle next
                    } else {
                        viewModel.nextStep()
                    }
                },
                modifier = Modifier
                    .weight(if (uiState.currentStepIndex > 0) 2f else 1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                enabled = !uiState.isLastStep
            ) {
                Text("Next Step")
            }
        }

        if (uiState.isLastStep) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onReady,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("I'm Ready!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onPractice,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Try Practice Mode", color = CyanPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
