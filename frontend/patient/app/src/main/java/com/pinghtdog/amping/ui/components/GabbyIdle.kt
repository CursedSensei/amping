package com.pinghtdog.amping.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

@Composable
fun GabbyIdle(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "blob_animations")

    // 1. Float and Squish (3s loop)
    val floatY by transition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    val squishX by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.97f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "squishX"
    )
    val squishY by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "squishY"
    )

    // 2. Blink (4s loop with a quick snap)
    val eyeScaleY by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                1f at 0
                1f at 1840 // 46%
                0.1f at 1880 // 47% (blink closed)
                1f at 1920 // 48% (blink open)
                1f at 4000
            }
        ),
        label = "blink"
    )

    // 3. Gasp (2s loop)
    val mouthScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gasp"
    )

    val blobPathString = "M 100 35 C 150 30, 180 60, 175 105 C 170 155, 140 175, 95 175 C 45 175, 20 145, 25 95 C 30 45, 50 40, 100 35 Z"
    val blobPath = PathParser().parsePathString(blobPathString).toPath()

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(200.dp)) {
        Canvas(modifier = Modifier.size(200.dp)) {

            // 1. Calculate scaling factor to map the 200x200 SVG viewBox to the actual physical Canvas size
            val scaleX = size.width / 200f
            val scaleY = size.height / 200f

            // 2. Apply global scale so we can use raw SVG coordinates inside safely!
            scale(scaleX, scaleY, pivot = androidx.compose.ui.geometry.Offset.Zero) {

                // Apply Float and Squish to the entire blob group
                translate(top = floatY) {

                    // The pivot for squishing is the center of the blob
                    scale(scaleX = squishX, scaleY = squishY, pivot = androidx.compose.ui.geometry.Offset(100f, 105f)) {

                        // Shadow Body
                        translate(left = -4f, top = 3f) {
                            drawPath(path = blobPath, color = Color(0xFF0B8AE2))
                        }

                        // Main Body
                        drawPath(path = blobPath, color = Color(0xFF14B5FF))

                        // Left Eye
                        scale(scaleX = 1f, scaleY = eyeScaleY, pivot = androidx.compose.ui.geometry.Offset(78f, 75f)) {
                            drawOval(
                                color = Color.White,
                                topLeft = androidx.compose.ui.geometry.Offset(70f, 64f),
                                size = androidx.compose.ui.geometry.Size(16f, 22f)
                            )
                        }

                        // Right Eye
                        scale(scaleX = 1f, scaleY = eyeScaleY, pivot = androidx.compose.ui.geometry.Offset(122f, 75f)) {
                            drawOval(
                                color = Color.White,
                                topLeft = androidx.compose.ui.geometry.Offset(114f, 64f),
                                size = androidx.compose.ui.geometry.Size(16f, 22f)
                            )
                        }

                        // Mouth
                        scale(scaleX = mouthScale, scaleY = mouthScale, pivot = androidx.compose.ui.geometry.Offset(100f, 115f)) {
                            drawCircle(
                                color = Color(0xFF0E2940),
                                radius = 18f,
                                center = androidx.compose.ui.geometry.Offset(100f, 115f)
                            )
                        }
                    }
                }
            }
        }
    }
}