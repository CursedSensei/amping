package com.pinghtdog.amping.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class GabbyState {
    IDLE,
    SPEAKING
}

@Composable
fun AnimatedGabby(
    state: GabbyState,
    modifier: Modifier = Modifier
) {
    when (state) {
        GabbyState.IDLE -> GabbyIdle(modifier = modifier)
        GabbyState.SPEAKING -> GabbySpeak(modifier = modifier)
    }
}
