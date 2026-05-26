package com.pinghtdog.amping.ui.onboarding

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation

fun NavGraphBuilder.onboardingNavGraph(navController: NavHostController, onOnboardingComplete: () -> Unit) {
    navigation(
        startDestination = OnboardingRoute.Splash.route,
        route = "onboarding"
    ) {
        composable(OnboardingRoute.Splash.route) {
            SplashScreen(onNext = { navController.navigate(OnboardingRoute.LanguageAccessibility.route) })
        }
        composable(OnboardingRoute.LanguageAccessibility.route) {
            LanguageAccessibilityScreen(
                onNext = { navController.navigate(OnboardingRoute.ClinicCodeEntry.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(OnboardingRoute.ClinicCodeEntry.route) {
            ClinicCodeEntryScreen(
                onNext = { 
                    // For now, let's just skip to the end or proceed to next placeholder
                    // In a real app, this would go to OB-04
                    navController.navigate(OnboardingRoute.BasicProfile.route) 
                },
                onBack = { navController.popBackStack() }
            )
        }
        
        // Placeholders for the rest of the flow to ensure the graph is valid
        composable(OnboardingRoute.BasicProfile.route) { 
            BasicProfileScreen(
                onNext = { navController.navigate(OnboardingRoute.AgeProfileConfirmation.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(OnboardingRoute.AgeProfileConfirmation.route) {
            // Mocking age as 25 (Adult) for now to test the Standard flow. 
            // In a real app, this would be retrieved from a ViewModel or passed as an argument.
            AgeProfileConfirmationScreen(
                age = 25, 
                onNext = { isChildOrSenior ->
                    if (isChildOrSenior) {
                        navController.navigate(OnboardingRoute.GuardianSetup.route)
                    } else {
                        navController.navigate(OnboardingRoute.LiteracyAssessment.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(OnboardingRoute.GuardianSetup.route) {
            GuardianSetupScreen(
                onNext = { navController.navigate(OnboardingRoute.LiteracyAssessment.route) },
                onSkip = { navController.navigate(OnboardingRoute.LiteracyAssessment.route) }
            )
        }
        composable(OnboardingRoute.LiteracyAssessment.route) {
            LiteracyAssessmentScreen(
                onNext = { navController.navigate(OnboardingRoute.PermissionsHub.route) }
            )
        }
        composable(OnboardingRoute.PermissionsHub.route) {
            PermissionsHubScreen(
                onNext = { navController.navigate(OnboardingRoute.CompanionIntro.route) }
            )
        }
        composable(OnboardingRoute.CompanionIntro.route) {
            CompanionIntroScreen(
                onNext = { navController.navigate(OnboardingRoute.StreakTutorial.route) }
            )
        }
        composable(OnboardingRoute.StreakTutorial.route) {
            StreakTutorialScreen(
                onNext = { navController.navigate(OnboardingRoute.VideoRecordTutorial.route) }
            )
        }
        composable(OnboardingRoute.VideoRecordTutorial.route) {
            VideoRecordTutorialScreen(
                onNext = { navController.navigate(OnboardingRoute.ProviderIntro.route) }
            )
        }
        composable(OnboardingRoute.ProviderIntro.route) {
            ProviderIntroScreen(
                onNext = { navController.navigate(OnboardingRoute.NotificationSetup.route) }
            )
        }
        composable(OnboardingRoute.NotificationSetup.route) {
            NotificationSetupScreen(
                onNext = { navController.navigate(OnboardingRoute.SummaryReview.route) }
            )
        }
        composable(OnboardingRoute.SummaryReview.route) {
            SummaryReviewScreen(
                onNext = { navController.navigate(OnboardingRoute.WelcomeComplete.route) }
            )
        }
        composable(OnboardingRoute.WelcomeComplete.route) {
            WelcomeCompleteScreen(
                onBegin = { onOnboardingComplete() }
            )
        }
    }
}
