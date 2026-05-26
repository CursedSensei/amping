package com.pinghtdog.amping.ui.onboarding

/**
 * Represents the 15 steps of the Onboarding & Authentication flow.
 */
sealed class OnboardingRoute(val route: String) {
    object Splash : OnboardingRoute("ob01_splash")
    object LanguageAccessibility : OnboardingRoute("ob02_language_access")
    object ClinicCodeEntry : OnboardingRoute("ob03_clinic_code")
    object BasicProfile : OnboardingRoute("ob04_basic_profile")
    object AgeProfileConfirmation : OnboardingRoute("ob05_age_profile_confirm")
    object GuardianSetup : OnboardingRoute("ob06_guardian_setup")
    object LiteracyAssessment : OnboardingRoute("ob07_literacy_assessment")
    object PermissionsHub : OnboardingRoute("ob08_permissions_hub")
    object CompanionIntro : OnboardingRoute("ob09_companion_intro")
    object StreakTutorial : OnboardingRoute("ob10_streak_tutorial")
    object VideoRecordTutorial : OnboardingRoute("ob11_video_tutorial")
    object ProviderIntro : OnboardingRoute("ob12_provider_intro")
    object NotificationSetup : OnboardingRoute("ob13_notification_setup")
    object SummaryReview : OnboardingRoute("ob14_summary_review")
    object WelcomeComplete : OnboardingRoute("ob15_welcome_complete")

    companion object {
        val startDestination = Splash.route
    }
}
