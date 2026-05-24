package com.pinghtdog.amping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pinghtdog.amping.ui.HomeDashboardScreen
import com.pinghtdog.amping.ui.SessionLaunchScreen
import com.pinghtdog.amping.ui.SuccessScreen
import com.pinghtdog.amping.ui.SymptomReportScreen
import com.pinghtdog.amping.ui.VideoRecordingScreen
import com.pinghtdog.amping.ui.VideoReviewScreen
// Note: If you have a custom theme file, import it here (e.g., com.pinghtdog.amping.ui.theme.AmpingTheme)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Replace MaterialTheme with AmpingTheme{} if you have one generated in your theme folder
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AmpingAppNavigation()
                }
            }
        }
    }
}

@Composable
fun AmpingAppNavigation() {
    // This creates the controller that handles navigating between screens
    val navController = rememberNavController()

    // The NavHost maps text "routes" to your actual Compose screens
    NavHost(navController = navController, startDestination = "home") {

        // 1. Home Dashboard
        composable("home") {
            HomeDashboardScreen(
                onStartSession = { navController.navigate("session_launch") }
            )
        }

        // 2. Session Launch (Gabby Greeting & Mood)
        composable("session_launch") {
            SessionLaunchScreen(
                onMoodSelected = { navController.navigate("symptom_report") }
            )
        }

        // 3. Symptom Report
        composable("symptom_report") {
            SymptomReportScreen(
                onSymptomsLogged = { navController.navigate("video_record") }
            )
        }

        // 4. Video Recording
        composable("video_record") {
            VideoRecordingScreen(
                onVideoRecorded = { navController.navigate("video_review") }
            )
        }

        // 5. Video Review & Upload
        composable("video_review") {
            VideoReviewScreen(
                onUpload = { navController.navigate("success") }
            )
        }

        // 6. Success & Reward Celebration
        composable("success") {
            SuccessScreen(
                onGoHome = {
                    // Navigates back to home and clears the backstack so the user
                    // doesn't press 'back' and end up on the success screen again.
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}