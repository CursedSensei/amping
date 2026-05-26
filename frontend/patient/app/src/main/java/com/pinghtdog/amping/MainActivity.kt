package com.pinghtdog.amping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pinghtdog.amping.ui.HomeDashboardScreen
import com.pinghtdog.amping.ui.session.SessionFlowContainer
// Note: If you have a custom theme file, import it here (e.g., com.pinghtdog.amping.ui.theme.AmpingTheme)

import com.pinghtdog.amping.ui.onboarding.onboardingNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            // Replace MaterialTheme with AmpingTheme{} if you have one generated in your theme folder
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(modifier = Modifier.fillMaxSize()) {
        // The NavHost maps text "routes" to your actual Compose screens
        NavHost(navController = navController, startDestination = "onboarding") {

            // 0. Onboarding Flow
            onboardingNavGraph(
                navController = navController,
                onOnboardingComplete = {
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )

            // 1. Home Dashboard
            composable("home") {
                HomeDashboardScreen(
                    onStartSession = { navController.navigate("session_launch") }
                )
            }

            // 2. Interactive AI-Guided Session Flow (Consolidates conversation, symptoms, camera & success)
            composable("session_launch") {
                SessionFlowContainer(
                    onGoHome = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
        }

        // Show Skip Onboarding button if we are in the onboarding flow, 
        // but not on Splash (ob01) or Welcome Complete (ob15)
        if (currentRoute != null && currentRoute.startsWith("ob") && 
            currentRoute != "ob01_splash" && currentRoute != "ob15_welcome_complete") {
            
            TextButton(
                onClick = {
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 16.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Skip Onboarding",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}