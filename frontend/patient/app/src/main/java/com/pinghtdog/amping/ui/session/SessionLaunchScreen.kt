package com.pinghtdog.amping.ui.session

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import com.pinghtdog.amping.data.model.Message
import com.pinghtdog.amping.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SessionLaunchScreen(
    viewModel: SessionViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val chatState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startSpeechListening()
        } else {
            Toast.makeText(context, "Microphone permission required for voice recognition", Toast.LENGTH_SHORT).show()
        }
    }

    // Automatically scroll to bottom when chat messages update or assistant is typing
    LaunchedEffect(uiState.chatHistory.size, uiState.assistantTyping) {
        if (uiState.chatHistory.isNotEmpty()) {
            coroutineScope.launch {
                chatState.animateScrollToItem(uiState.chatHistory.size - 1)
            }
        }
    }

    // Profile dependent theme styling
    val (primaryColor, gradientColors) = when (uiState.activeProfile) {
        "youth" -> Pair(
            Color(0xFFFF2B88), // Vibrantly Playful Pink/Magenta
            listOf(Color(0xFFFF2B88), Color(0xFF9C27B0))
        )
        "senior" -> Pair(
            Color(0xFFFF9800), // Warm & Clear Amber/Orange
            listOf(Color(0xFFFF9800), Color(0xFFFF5722))
        )
        else -> Pair(
            CyanPrimary, // Professional & Slick Teal/Cyan
            listOf(CyanPrimary, Color(0xFF007A87))
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Spacer(modifier = Modifier.height(72.dp)) // Reserve space for debug panel

            // Stage Label
            Text(
                text = "STAGE 1 OF 3: CONVERSATION",
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                textAlign = TextAlign.Center
            )

            // Central voice-guided avatar visualizer
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "avatar_pulse")
                
                // Dynamic speed and size based on TTS / Listening status
                val pulseDuration = if (uiState.isTtsSpeaking) 1200 else if (uiState.isListening) 800 else 2400
                val targetScale1 = if (uiState.isTtsSpeaking) 1.5f else if (uiState.isListening) 1.3f else 1.15f
                val targetScale2 = if (uiState.isTtsSpeaking) 2.0f else if (uiState.isListening) 1.6f else 1.3f
                val targetScale3 = if (uiState.isTtsSpeaking) 2.5f else if (uiState.isListening) 2.0f else 1.45f

                val scale1 by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = targetScale1,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = pulseDuration, easing = EaseOutQuad),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale1"
                )
                val alpha1 by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 0.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = pulseDuration, easing = EaseOutQuad),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha1"
                )

                val scale2 by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = targetScale2,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = pulseDuration, delayMillis = pulseDuration / 3, easing = EaseOutQuad),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale2"
                )
                val alpha2 by infiniteTransition.animateFloat(
                    initialValue = 0.35f,
                    targetValue = 0.02f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = pulseDuration, delayMillis = pulseDuration / 3, easing = EaseOutQuad),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha2"
                )

                val scale3 by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = targetScale3,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = pulseDuration, delayMillis = (pulseDuration * 2) / 3, easing = EaseOutQuad),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale3"
                )
                val alpha3 by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 0.01f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = pulseDuration, delayMillis = (pulseDuration * 2) / 3, easing = EaseOutQuad),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha3"
                )

                val glowingColor = if (uiState.isListening) RedPenalty else primaryColor

                // Concentric Ripple Ring 3
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .graphicsLayer(scaleX = scale3, scaleY = scale3, alpha = alpha3)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(glowingColor.copy(alpha = 0.4f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )

                // Concentric Ripple Ring 2
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .graphicsLayer(scaleX = scale2, scaleY = scale2, alpha = alpha2)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(glowingColor.copy(alpha = 0.8f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )

                // Concentric Ripple Ring 1
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .graphicsLayer(scaleX = scale1, scaleY = scale1, alpha = alpha1)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(glowingColor.copy(alpha = 0.9f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )

                // Central Active Avatar Orb
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (uiState.isListening) listOf(RedPenalty, Color(0xFF991B1B)) else gradientColors
                            ),
                            shape = CircleShape
                        )
                        .border(
                            width = 3.dp,
                            color = Color.White,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isListening) Icons.Filled.Mic else Icons.Filled.Face,
                        contentDescription = "Gabby Speaker",
                        tint = Color.White,
                        modifier = Modifier.size(68.dp)
                    )
                }
            }

            // Intent Parsing / Deferred Action Banner
            AnimatedVisibility(
                visible = uiState.pendingToolCallName != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val friendlyName = when (uiState.pendingToolCallName) {
                    "show_symptom_checklist", "transition_to_symptoms" -> "Opening symptom checklist questionnaire..."
                    "transition_to_vdot" -> "Preparing secure medicine check-in..."
                    "trigger_vdot" -> "Opening secure camera recording..."
                    else -> "Executing intent..."
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "CLINICAL INTENT DETECTED",
                                    color = Color(0xFF047857),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = friendlyName,
                                    color = TextDark,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.executePendingToolCall() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = "Skip Voice & Go",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Display last user message during the conversation
            val lastUserMessage = uiState.chatHistory.lastOrNull { it.role == "user" }
            AnimatedVisibility(
                visible = lastUserMessage != null && !lastUserMessage.content.isNullOrBlank() && !lastUserMessage.content.startsWith("VDOT upload complete"),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(primaryColor.copy(alpha = 0.08f))
                            .border(1.dp, primaryColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "You said: \"${lastUserMessage?.content}\"",
                                color = TextDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Glassmorphic Audio Subtitles Drawer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.85f))
                    .border(
                        width = 1.dp,
                        color = (if (uiState.isListening) RedPenalty else primaryColor).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.assistantTyping) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Gabby is formulating a response",
                                color = TextMuted,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            TypingDotRow()
                        }
                    } else {
                        val subtitleText = uiState.currentSubtitleText.ifBlank {
                            when (uiState.activeProfile) {
                                "youth" -> "Yo champion! Tap the mic to chat with Gabby! 🚀"
                                "senior" -> "Hello dear, tap the microphone to talk with me."
                                else -> "Tap the microphone below to begin speaking with Gabby."
                            }
                        }
                        Text(
                            text = subtitleText,
                            color = TextDark,
                            fontSize = 17.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // "I'm Ready" Fallback Button for Intent Parsing Failure
            AnimatedVisibility(
                visible = uiState.conversationStage == 3 && uiState.vdotRepromptCount >= 2 && !uiState.assistantTyping,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }) + expandVertically(),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }) + shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.bypassToolCallToVideoRecording() }
                            .graphicsLayer {
                                shadowElevation = 12f
                                clip = true
                            },
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.linearGradient(gradientColors))
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Videocam,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "I'M READY (START VDOT CAMERA)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }

            // Quick Replies Panel
            AnimatedVisibility(
                visible = uiState.quickReplies.isNotEmpty() && !uiState.assistantTyping,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.quickReplies.forEach { reply ->
                        Button(
                            onClick = {
                                viewModel.sendMessage(reply)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = TextDark
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 38.dp)
                                .border(
                                    width = 1.dp,
                                    color = primaryColor.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = reply, 
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Chat Input Controller
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Speech input trigger
                IconButton(
                    onClick = {
                        keyboardController?.hide()
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        
                        if (hasPermission) {
                            if (uiState.isListening) {
                                viewModel.stopSpeechListening()
                            } else {
                                viewModel.startSpeechListening()
                            }
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (uiState.isListening) RedPenalty.copy(alpha = 0.1f) else primaryColor.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (uiState.isListening) Icons.Filled.MicOff else Icons.Filled.Mic,
                        contentDescription = "Speak to Gabby",
                        tint = if (uiState.isListening) RedPenalty else primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Text Field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask Gabby anything...", color = TextMuted, fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedContainerColor = LightBackground,
                        unfocusedContainerColor = LightBackground
                    ),
                    maxLines = 1
                )

                // Send Button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                            keyboardController?.hide()
                        }
                    },
                    enabled = inputText.isNotBlank() && !uiState.assistantTyping,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (inputText.isNotBlank() && !uiState.assistantTyping) primaryColor else Color.LightGray.copy(alpha = 0.3f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Speech Transcription Visualizer Overlay
        AnimatedVisibility(
            visible = uiState.isListening,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Listening to you...",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Gabby is transcribing your voice...",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    // Bouncing Speech Waves
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(5) { index ->
                            SpeechBar(index = index, tint = primaryColor)
                        }
                    }
                }
            }
        }

        // Clinical Crisis / Safety Emergency Alert Dialog Card
        AnimatedVisibility(
            visible = uiState.emergencyState != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(RedPenalty.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = RedPenalty,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "CRITICAL CARE ACTIVE",
                            color = RedPenalty,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.emergencyState ?: "Your care team has been notified.",
                            color = TextDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { /* Call Helpline */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RedPenalty),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Phone, "Call Emergency Care", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CALL CARE TEAM NOW", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { viewModel.resetSession() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
                        ) {
                            Text("Dismiss / Reset Session", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message, activeProfile: String) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // Gabby Avatar Indicator
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CyanPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Face,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) DarkNavy else Color.White,
            tonalElevation = 1.dp,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = message.content,
                    color = if (isUser) Color.White else TextDark,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
fun TypingIndicatorBubble() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CyanPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Face, null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            modifier = Modifier.width(60.dp),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val infiniteTransition = rememberInfiniteTransition(label = "dots")
                    val dy by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = -6f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 300, delayMillis = index * 100, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_dy"
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .offset(y = dy.dp)
                            .background(CyanPrimary, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun SpeechBar(index: Int, tint: Color = CyanPrimary) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val scaleY by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400 + (index * 80)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "barScale"
    )

    Box(
        modifier = Modifier
            .width(6.dp)
            .height(60.dp)
            .background(tint, RoundedCornerShape(3.dp))
            .padding(vertical = (30 * (1f - scaleY)).dp)
    )
}

@Composable
fun TypingDotRow() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "dots")
            val dy by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 300, delayMillis = index * 100, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_dy"
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .offset(y = dy.dp)
                    .background(CyanPrimary, CircleShape)
            )
        }
    }
}
