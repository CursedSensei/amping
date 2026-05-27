package com.pinghtdog.amping.ui.session

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
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
import androidx.compose.ui.viewinterop.AndroidView
import com.pinghtdog.amping.ui.theme.CyanPrimary
import com.pinghtdog.amping.ui.theme.LightBackground
import com.pinghtdog.amping.ui.theme.TextDark
import com.pinghtdog.amping.ui.theme.TextMuted

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VideoReviewScreen(
    viewModel: SessionViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

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
                text = "STAGE 3 OF 3: VIDEO VERIFICATION",
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Review your Check-in Video",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Confirm that your swallowing action was clearly captured in the clip before submitting.",
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Video Player Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            val player = videoViewRef ?: return@clickable
                            if (isPlaying) {
                                player.pause()
                                isPlaying = false
                            } else {
                                player.start()
                                isPlaying = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Real native VideoView wrapper
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                videoViewRef = this
                                setOnCompletionListener {
                                    isPlaying = false
                                }
                            }
                        },
                        update = { videoView ->
                            val path = uiState.recordedVideoPath
                            if (path != null) {
                                try {
                                    videoView.setVideoURI(Uri.parse(path))
                                    videoView.seekTo(1) // Seek to 1ms to load preview frame
                                } catch (e: Exception) {
                                    android.util.Log.e("VideoReview", "Error loading video: ${e.message}", e)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                    )

                    // Simulated/Interactive Play Overlay Button
                    if (!isPlaying) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f))
                                .clickable {
                                    videoViewRef?.start()
                                    isPlaying = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = "Play Video",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Text(
                        text = "Touch to Play/Pause Preview",
                        color = Color.LightGray.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Uploading states or Confirm buttons
            AnimatedContent(targetState = uiState.isUploading, label = "upload_state") { isUploading ->
                if (isUploading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            progress = uiState.uploadProgress,
                            color = CyanPrimary,
                            strokeWidth = 6.dp,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Uploading dose verification: ${(uiState.uploadProgress * 100).toInt()}%",
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                videoViewRef?.pause()
                                isPlaying = false
                                viewModel.uploadVideo()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Text(
                                "Looks good — Upload Video",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                videoViewRef?.stopPlayback()
                                videoViewRef = null
                                isPlaying = false
                                viewModel.startCameraRecording()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.LightGray),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark)
                        ) {
                            Text("Retake Video", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
