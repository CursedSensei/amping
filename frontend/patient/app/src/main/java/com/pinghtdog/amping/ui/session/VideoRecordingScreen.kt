package com.pinghtdog.amping.ui.session

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pinghtdog.amping.ui.theme.RedPenalty
import com.pinghtdog.amping.ui.theme.CyanPrimary
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun VideoRecordingScreen(
    viewModel: SessionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: hasCameraPermission
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: hasAudioPermission
        
        hasCameraPermission = cameraGranted
        hasAudioPermission = audioGranted
        
        if (!cameraGranted || !audioGranted) {
            Toast.makeText(context, "Permissions denied. Redirecting to settings to enable camera/mic.", Toast.LENGTH_LONG).show()
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (!hasCameraPermission || !hasAudioPermission) {
        PermissionRequiredScreen(
            onRequestPermissions = {
                permissionLauncher.launch(
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                )
            },
            modifier = modifier
        )
    } else {
        CameraRecordingContent(
            viewModel = viewModel,
            context = context,
            lifecycleOwner = lifecycleOwner,
            modifier = modifier
        )
    }
}

@Composable
fun PermissionRequiredScreen(
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.CameraAlt,
                contentDescription = null,
                tint = CyanPrimary,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Permissions Required",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "To securely verify your daily dose compliance, Gabby needs temporary access to your camera and microphone.",
                color = Color.LightGray,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(36.dp))
            Button(
                onClick = onRequestPermissions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Grant Camera & Mic Access",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun CameraRecordingContent(
    viewModel: SessionViewModel,
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    modifier: Modifier = Modifier
) {
    var isRecording by remember { mutableStateOf(false) }
    var secondsElapsed by remember { mutableStateOf(0) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var isFrontCamera by remember { mutableStateOf(true) }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    // Dynamic Binding Effect based on front/back camera switch
    LaunchedEffect(isFrontCamera) {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val recorder = Recorder.Builder()
            .setQualitySelector(
                QualitySelector.from(
                    Quality.SD,
                    FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
                )
            )
            .build()
        val capture = VideoCapture.withOutput(recorder)
        videoCapture = capture

        val cameraSelector = when {
            isFrontCamera && cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }
            cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }
            else -> {
                CameraSelector.DEFAULT_BACK_CAMERA // Fallback
            }
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                capture
            )
        } catch (e: Exception) {
            android.util.Log.e("CameraX", "Binding camera failed: ${e.message}", e)
        }
    }

    // Recording seconds timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            secondsElapsed = 0
            while (isRecording) {
                delay(1000)
                secondsElapsed++
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Real Camera Viewfinder Preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Guidance Card from Gabby
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 110.dp, start = 24.dp, end = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xAA000000)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Gabby's Secure Ingestion Check",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Hold up your medicine pill, swallow it in front of the camera, then open your mouth to confirm.",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }

        // Recording Time Counter Badge
        if (isRecording) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 244.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(RedPenalty)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "REC  00:${secondsElapsed.toString().padStart(2, '0')}",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        // Camera Control Buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close / Cancel Button
            IconButton(
                onClick = {
                    activeRecording?.stop()
                    viewModel.forcePhase(com.pinghtdog.amping.data.model.SessionPhase.CONVERSATION)
                },
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.12f), CircleShape)
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Cancel",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Pulsing Recording Trigger
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                val pulsingRing = rememberInfiniteTransition(label = "pulse")
                val ringScale by pulsingRing.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 800, easing = EaseInOutBack),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )

                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            val controller = videoCapture
                            if (controller == null) {
                                Toast.makeText(context, "Camera not ready yet", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }

                            if (activeRecording == null) {
                                val filename = "vdot_${System.currentTimeMillis()}.mp4"

                                try {
                                    val pendingRecording = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        val contentValues = ContentValues().apply {
                                            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                                            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                                            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Amping")
                                        }
                                        val mediaStoreOutput = MediaStoreOutputOptions.Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                                            .setContentValues(contentValues)
                                            .build()
                                        controller.output.prepareRecording(context, mediaStoreOutput)
                                    } else {
                                        val localFile = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), filename)
                                        val fileOutput = FileOutputOptions.Builder(localFile).build()
                                        controller.output.prepareRecording(context, fileOutput)
                                    }

                                    activeRecording = pendingRecording
                                        .withAudioEnabled()
                                        .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                                            when (recordEvent) {
                                                is VideoRecordEvent.Start -> {
                                                    isRecording = true
                                                }
                                                is VideoRecordEvent.Finalize -> {
                                                    isRecording = false
                                                    activeRecording = null
                                                    if (!recordEvent.hasError()) {
                                                        val savedUri = recordEvent.outputResults.outputUri
                                                        viewModel.completeRecording(savedUri.toString())
                                                    } else {
                                                        val errorMsg = "Recording failed: error code ${recordEvent.error}"
                                                        android.util.Log.e("CameraX", errorMsg)
                                                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        }
                                } catch (securityException: SecurityException) {
                                    Toast.makeText(context, "Microphone permission required for audio", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Failed to start recording: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // Stop filming
                                activeRecording?.stop()
                                activeRecording = null
                            }
                        }
                        .border(
                            width = 6.dp,
                            color = if (isRecording) RedPenalty.copy(alpha = 0.2f * ringScale) else Color.White,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isRecording) 28.dp else 56.dp)
                            .clip(if (isRecording) RoundedCornerShape(8.dp) else CircleShape)
                            .background(RedPenalty)
                    )
                }
            }

            // Flip Camera Toggle Button
            IconButton(
                onClick = {
                    if (activeRecording == null) {
                        isFrontCamera = !isFrontCamera
                    } else {
                        Toast.makeText(context, "Cannot flip camera during recording", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.12f), CircleShape)
            ) {
                Icon(
                    Icons.Filled.FlipCameraAndroid,
                    contentDescription = "Flip Camera",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
