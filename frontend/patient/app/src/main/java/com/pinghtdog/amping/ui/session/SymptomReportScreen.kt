package com.pinghtdog.amping.ui.session

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pinghtdog.amping.ui.theme.*

@Composable
fun SymptomReportScreen(
    viewModel: SessionViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val symptoms = listOf("Nausea", "Dizziness", "Rash", "Fatigue", "None of these")

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

            Text(
                text = "STAGE 2 OF 3: SYMPTOM REPORTING",
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "How has your body felt today?",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose any physical effects you are currently experiencing from your tuberculosis treatments.",
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Checkbox Checklist Panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    symptoms.forEach { symptom ->
                        val isChecked = uiState.selectedSymptoms.contains(symptom)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (symptom == "None of these") {
                                        symptoms.forEach { s ->
                                            if (s != "None of these") viewModel.selectSymptom(s, false)
                                        }
                                        viewModel.selectSymptom(symptom, !isChecked)
                                    } else {
                                        viewModel.selectSymptom("None of these", false)
                                        viewModel.selectSymptom(symptom, !isChecked)
                                    }
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Healing,
                                    contentDescription = null,
                                    tint = if (isChecked) CyanPrimary else TextMuted.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = symptom,
                                    color = if (isChecked) TextDark else TextDark.copy(alpha = 0.8f),
                                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 16.sp
                                )
                            }
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (symptom == "None of these") {
                                        symptoms.forEach { s ->
                                            if (s != "None of these") viewModel.selectSymptom(s, false)
                                        }
                                        viewModel.selectSymptom(symptom, checked)
                                    } else {
                                        viewModel.selectSymptom("None of these", false)
                                        viewModel.selectSymptom(symptom, checked)
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = CyanPrimary)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Nausea Severity sliding Card Panel
            AnimatedVisibility(
                visible = uiState.selectedSymptoms.contains("Nausea"),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyanPrimary.copy(alpha = 0.08f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Nausea Severity Assessment",
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SeverityRadioButton(
                                "Mild Nausea",
                                active = uiState.nauseaSeverity == "Mild",
                                modifier = Modifier.weight(1f)
                            ) {
                                viewModel.selectNauseaSeverity("Mild")
                            }
                            SeverityRadioButton(
                                "Severe Nausea",
                                active = uiState.nauseaSeverity == "Severe",
                                modifier = Modifier.weight(1f)
                            ) {
                                viewModel.selectNauseaSeverity("Severe")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Submit Button
            Button(
                onClick = { viewModel.submitSymptoms() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "All logged, continue to VDOT",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SeverityRadioButton(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) CyanPrimary else Color.White)
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = if (active) CyanPrimary else Color.LightGray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (active) Color.White else TextDark,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
