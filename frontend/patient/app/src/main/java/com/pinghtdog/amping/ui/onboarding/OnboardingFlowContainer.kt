package com.pinghtdog.amping.ui.onboarding

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel

enum class OnboardingStep {
    SPLASH,
    PERMISSIONS,
    MEET_GABBY,
    STREAK_TUTORIAL,
    RECORD_TUTORIAL,
    HEALTH_GUIDE,
    NOTIFICATIONS,
    ACCESSIBILITY,
    WELCOME
}

@Composable
fun OnboardingFlowContainer(
    onOnboardingComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(OnboardingStep.SPLASH) }

    when (currentStep) {
        OnboardingStep.SPLASH -> {
            SplashScreen(
                onInitializationComplete = { currentStep = OnboardingStep.PERMISSIONS }
            )
        }
        OnboardingStep.PERMISSIONS -> {
            PermissionsScreen(
                onPermissionsComplete = { currentStep = OnboardingStep.MEET_GABBY }
            )
        }
        OnboardingStep.MEET_GABBY -> {
            MeetGabbyScreen(
                onContinue = { currentStep = OnboardingStep.STREAK_TUTORIAL }
            )
        }
        OnboardingStep.STREAK_TUTORIAL -> {
            StreakTutorialScreen(
                onNext = { currentStep = OnboardingStep.RECORD_TUTORIAL }
            )
        }
        OnboardingStep.RECORD_TUTORIAL -> {
            RecordTutorialScreen(
                onReady = { currentStep = OnboardingStep.HEALTH_GUIDE },
                onPractice = { /* Handle practice mode if needed */ }
            )
        }
        OnboardingStep.HEALTH_GUIDE -> {
            HealthGuideScreen(
                onAcknowledge = { currentStep = OnboardingStep.NOTIFICATIONS }
            )
        }
        OnboardingStep.NOTIFICATIONS -> {
            NotificationsScreen(
                onSave = { currentStep = OnboardingStep.ACCESSIBILITY }
            )
        }
        OnboardingStep.ACCESSIBILITY -> {
            AccessibilityScreen(
                onConfirm = { currentStep = OnboardingStep.WELCOME },
                onChangeSetting = { /* Handle setting change */ }
            )
        }
        OnboardingStep.WELCOME -> {
            WelcomeScreen(
                onBegin = onOnboardingComplete
            )
        }
    }
}
