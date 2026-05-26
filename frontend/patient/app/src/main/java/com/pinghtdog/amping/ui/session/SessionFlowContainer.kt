package com.pinghtdog.amping.ui.session

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.pinghtdog.amping.data.model.SessionPhase

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SessionFlowContainer(
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        // Dynamic layout transition animated smoothly
        AnimatedContent(
            targetState = uiState.currentPhase,
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) with fadeOut(animationSpec = tween(400))
            },
            label = "phase_transitions"
        ) { phase ->
            when (phase) {
                SessionPhase.CONVERSATION -> {
                    SessionLaunchScreen(viewModel = viewModel)
                }
                SessionPhase.SYMPTOM_LOGGING -> {
                    SymptomReportScreen(viewModel = viewModel)
                }
                SessionPhase.VDOT_CAPTURE -> {
                    VideoRecordingScreen(viewModel = viewModel)
                }
                SessionPhase.VDOT_REVIEW -> {
                    VideoReviewScreen(viewModel = viewModel)
                }
                SessionPhase.SUCCESS -> {
                    SuccessScreen(
                        viewModel = viewModel,
                        onGoHome = onGoHome
                    )
                }
            }
        }

        // Floating Developer Debug Panel (Highest z-index)
        DeveloperDebugPanel(
            viewModel = viewModel,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
