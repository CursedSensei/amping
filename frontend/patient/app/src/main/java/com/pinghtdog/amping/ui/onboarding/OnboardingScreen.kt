package com.pinghtdog.amping.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pinghtdog.amping.ui.components.GabbyIdle
import com.pinghtdog.amping.ui.theme.CyanPrimary
import com.pinghtdog.amping.ui.theme.DarkNavy
import com.pinghtdog.amping.ui.theme.LightBackground
import com.pinghtdog.amping.ui.theme.RedPenalty

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel() // Injects the ViewModel automatically
) {
    // Observe the state from the ViewModel
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // 1. Introduce Gabby!
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            GabbyIdle(modifier = Modifier.size(110.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome to Amping",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = DarkNavy
        )
        Text(
            text = "I'm Gabby, your health companion. Let's get your profile set up!",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Name Input (Passes typing events to ViewModel)
        OutlinedTextField(
            value = state.name,
            onValueChange = { viewModel.updateName(it) },
            label = { Text("What should I call you?") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = state.showValidationError && state.name.isBlank()
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Select your age group",
            fontWeight = FontWeight.Bold,
            color = DarkNavy,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 3. Age Group Selector (Passes clicks to ViewModel)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AgeGroup.values().forEach { group ->
                val isSelected = state.selectedAgeGroup == group

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) CyanPrimary.copy(alpha = 0.1f) else Color.White)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) CyanPrimary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.selectAgeGroup(group) }
                        .padding(16.dp)
                ) {
                    Text(
                        text = group.displayName,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) CyanPrimary else DarkNavy
                    )
                }
            }
        }

        if (state.showValidationError) {
            Text(
                text = "Please enter a name and select an age group.",
                color = RedPenalty,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 4. Submit Button (Lets ViewModel decide if we can proceed)
        Button(
            onClick = { viewModel.onContinueClicked(onSuccess = onOnboardingComplete) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.canProceed) CyanPrimary else Color.LightGray
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Continue", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}