package com.pinghtdog.amping.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

@Composable
fun GabbySpeak(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "blob_animations")

    // --- 1. Body Movement (3.5s loop) ---
    val bodyTransY by transition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3500
                0f at 0
                -6f at 1050 // 30%
                -2f at 2450 // 70%
                0f at 3500
            }
        ), label = "bodyY"
    )
    val bodyScaleX by transition.animateFloat(
        initialValue = 1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3500
                1f at 0
                0.96f at 1050
                1.02f at 2450
                1f at 3500
            }
        ), label = "bodyScaleX"
    )
    val bodyScaleY by transition.animateFloat(
        initialValue = 1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3500
                1f at 0
                1.04f at 1050
                0.98f at 2450
                1f at 3500
            }
        ), label = "bodyScaleY"
    )
    val bodyRotation by transition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3500
                0f at 0
                4f at 1050
                -3f at 2450
                0f at 3500
            }
        ), label = "bodyRotate"
    )

    // --- 2. Talking Mouth (1.8s loop) ---
    val mouthScaleX by transition.animateFloat(
        initialValue = 0.6f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1800
                0.6f at 0
                1.2f at 180  // 10%
                0.8f at 360  // 20%
                1.1f at 540  // 30%
                0.5f at 720  // 40%
                1.3f at 900  // 50%
                0.9f at 1080 // 60%
                0.7f at 1260 // 70%
                1.2f at 1440 // 80%
                0.8f at 1620 // 90%
                0.6f at 1800 // 100%
            }
        ), label = "mouthX"
    )
    val mouthScaleY by transition.animateFloat(
        initialValue = 0.2f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1800
                0.2f at 0
                0.9f at 180
                1.3f at 360
                0.5f at 540
                0.2f at 720
                1.1f at 900
                0.8f at 1080
                0.3f at 1260
                0.7f at 1440
                1.2f at 1620
                0.2f at 1800
            }
        ), label = "mouthY"
    )

    // --- 3. Blinking (4s loop) ---
    val eyeScaleY by transition.animateFloat(
        initialValue = 1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                1f at 0
                1f at 1840   // 46%
                0.1f at 1880 // 47%
                1f at 1920   // 48%
                1f at 4000
            }
        ), label = "blink"
    )

    // --- 4. Eyebrows (3.5s loop) ---
    val browTransY by transition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3500
                0f at 0
                -4f at 1050 // 30%
                2f at 2450  // 70%
                0f at 3500
            }
        ), label = "browY"
    )
    val browLeftRotate by transition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3500
                0f at 0
                -10f at 1050
                5f at 2450
                0f at 3500
            }
        ), label = "browLeftRotate"
    )
    val browRightRotate by transition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3500
                0f at 0
                10f at 1050
                -5f at 2450
                0f at 3500
            }
        ), label = "browRightRotate"
    )

    // --- Paths ---
    val blobPath = PathParser().parsePathString("M 100 35 C 150 30, 180 60, 175 105 C 170 155, 140 175, 95 175 C 45 175, 20 145, 25 95 C 30 45, 50 40, 100 35 Z").toPath()
    val browLeftPath = PathParser().parsePathString("M 68 55 Q 78 48 88 55").toPath()
    val browRightPath = PathParser().parsePathString("M 112 55 Q 122 48 132 55").toPath()

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(200.dp)) {
        Canvas(modifier = Modifier.size(200.dp)) {
            // Unify Canvas scaling to match 200x200 viewBox
            val scaleXCanvas = size.width / 200f
            val scaleYCanvas = size.height / 200f

            scale(scaleXCanvas, scaleYCanvas, pivot = Offset.Zero) {

                // Apply global body transforms
                translate(top = bodyTransY) {
                    scale(scaleX = bodyScaleX, scaleY = bodyScaleY, pivot = Offset(100f, 105f)) {
                        rotate(degrees = bodyRotation, pivot = Offset(100f, 105f)) {

                            // Shadow Body
                            translate(left = -4f, top = 3f) {
                                drawPath(path = blobPath, color = Color(0xFF0B8AE2))
                            }

                            // Main Body
                            drawPath(path = blobPath, color = Color(0xFF14B5FF))

                            // Left Brow
                            translate(top = browTransY) {
                                rotate(degrees = browLeftRotate, pivot = Offset(78f, 55f)) {
                                    drawPath(
                                        path = browLeftPath,
                                        color = Color(0xFF0E2940),
                                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                                    )
                                }
                            }

                            // Right Brow
                            translate(top = browTransY) {
                                rotate(degrees = browRightRotate, pivot = Offset(122f, 55f)) {
                                    drawPath(
                                        path = browRightPath,
                                        color = Color(0xFF0E2940),
                                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                                    )
                                }
                            }

                            // Left Eye
                            scale(scaleX = 1f, scaleY = eyeScaleY, pivot = Offset(78f, 75f)) {
                                drawOval(
                                    color = Color.White,
                                    topLeft = Offset(70f, 64f),
                                    size = Size(16f, 22f)
                                )
                            }

                            // Right Eye
                            scale(scaleX = 1f, scaleY = eyeScaleY, pivot = Offset(122f, 75f)) {
                                drawOval(
                                    color = Color.White,
                                    topLeft = Offset(114f, 64f),
                                    size = Size(16f, 22f)
                                )
                            }

                            // Mouth (Now uses drawOval to support independent X/Y scaling for talking)
                            scale(scaleX = mouthScaleX, scaleY = mouthScaleY, pivot = Offset(100f, 115f)) {
                                drawOval(
                                    color = Color(0xFF0E2940),
                                    topLeft = Offset(84f, 99f), // Center 100,115 minus radius 16
                                    size = Size(32f, 32f)       // rx=16 * 2, ry=16 * 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}