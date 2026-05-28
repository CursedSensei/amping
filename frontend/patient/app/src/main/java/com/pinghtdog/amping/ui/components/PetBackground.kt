package com.pinghtdog.amping.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun PetBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "background_particles")

    val floatAnim1 by transition.animateFloat(
        initialValue = 0f, targetValue = 15f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse),
        label = "p1"
    )
    val floatAnim2 by transition.animateFloat(
        initialValue = 0f, targetValue = -20f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Reverse),
        label = "p2"
    )
    val floatAnim3 by transition.animateFloat(
        initialValue = 0f, targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(3500, easing = LinearEasing), RepeatMode.Reverse),
        label = "p3"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // 1. Draw a soft radial gradient in the center
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFE3F2FD), // Very light blue
                    Color(0xFFF4F7FB)  // LightBackground
                ),
                center = Offset(width / 2, height / 2),
                radius = width * 0.8f
            )
        )

        // 2. Draw animated decorative soft circles (bubbles/particles)
        val particleColors = listOf(
            Color(0xFF1DB5F5).copy(alpha = 0.05f),
            Color(0xFF1DB5F5).copy(alpha = 0.03f)
        )

        drawCircle(
            color = particleColors[0],
            radius = 100f,
            center = Offset(width * 0.2f, height * 0.3f + floatAnim1)
        )
        drawCircle(
            color = particleColors[1],
            radius = 150f,
            center = Offset(width * 0.85f, height * 0.2f + floatAnim2)
        )
        drawCircle(
            color = particleColors[0],
            radius = 80f,
            center = Offset(width * 0.75f, height * 0.7f + floatAnim3)
        )
        drawCircle(
            color = particleColors[1],
            radius = 120f,
            center = Offset(width * 0.15f, height * 0.8f + floatAnim1)
        )
    }
}
