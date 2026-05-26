package com.pinghtdog.amping.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.ui.unit.dp
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun OnboardingPlaceholder(
    title: String,
    onNext: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onNext) {
            Text("Next")
        }
        if (onBack != null) {
            TextButton(onClick = onBack) {
                Text("Back")
            }
        }
    }
}

@Composable
fun SplashScreen(onNext: () -> Unit) {
    // Auto-advance logic (max 3s)
    LaunchedEffect(Unit) {
        delay(3000)
        onNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Full-screen Amping logo centered
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Replace with actual logo resource when available
            Text(
                text = "AMPING",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 8.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

        // Animated Gabby silhouette rising from bottom
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(500) // Brief delay before Gabby appears
            visible = true
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 1000)
            ) + fadeIn(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            // Placeholder for Gabby silhouette
            Box(
                modifier = Modifier
                    .size(200.dp, 250.dp)
                    .background(Color.Gray.copy(alpha = 0.3f)) // Silhouette placeholder
            ) {
                Text(
                    "GABBY",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
        }

        // Thin skeleton loader (bottom 5% of screen)
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp), // Positioned just above the footer
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        // App version tag at footer
        Text(
            text = "v1.0.0-alpha",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            color = Color.Gray
        )
    }
}

@Composable
fun LanguageAccessibilityScreen(onNext: () -> Unit, onBack: () -> Unit) {
    var selectedLanguage by remember { mutableStateOf("English") }
    var largeTextEnabled by remember { mutableStateOf(false) }
    var voiceFirstEnabled by remember { mutableStateOf(false) }
    var highContrastEnabled by remember { mutableStateOf(false) }

    val languages = listOf("Filipino", "English", "Bisaya", "Hiligaynon", "Ilocano")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to Amping",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Choose your language and settings",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Language Selection
        Text(
            text = "Language",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        languages.chunked(2).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { lang ->
                    FilterChip(
                        selected = selectedLanguage == lang,
                        onClick = { selectedLanguage = lang },
                        label = { Text(lang) },
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            // Placeholder for Flag Icon
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    )
                }
                if (rowItems.size < 2) Spacer(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Accessibility Toggles
        Text(
            text = "Accessibility",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        AccessibilityToggle(
            label = "Large Text",
            description = "Make text easier to read",
            checked = largeTextEnabled,
            onCheckedChange = { largeTextEnabled = it },
            icon = Icons.Default.TextFormat
        )
        
        AccessibilityToggle(
            label = "Voice-First",
            description = "Read all screens aloud",
            checked = voiceFirstEnabled,
            onCheckedChange = { voiceFirstEnabled = it },
            icon = Icons.Default.VolumeUp
        )

        AccessibilityToggle(
            label = "High Contrast",
            description = "Better color visibility",
            checked = highContrastEnabled,
            onCheckedChange = { highContrastEnabled = it },
            icon = Icons.Default.Contrast
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Continue", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun AccessibilityToggle(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun ClinicCodeEntryScreen(onNext: () -> Unit, onBack: () -> Unit) {
    var clinicCode by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Link Your Clinic",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Your clinic gave you a code or a QR card at enrolment.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(48.dp))

        // QR Scan Button (Primary CTA)
        Button(
            onClick = { /* Mock QR Scan */ },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Scan QR Code", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "— OR —", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))

        // Manual Entry
        OutlinedTextField(
            value = clinicCode,
            onValueChange = { if (it.length <= 6) clinicCode = it.filter { char -> char.isDigit() } },
            label = { Text("Enter Clinic Code Manually") },
            placeholder = { Text("e.g. 123456") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = isError,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            ),
            supportingText = {
                if (isError) {
                    Text(text = "Invalid code — ask your nurse to verify.", color = MaterialTheme.colorScheme.error)
                } else {
                    Text(text = "6-digit number from your clinic")
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (clinicCode.length == 6) {
                    onNext()
                } else {
                    isError = true
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = clinicCode.length == 6
        ) {
            Text("Submit", style = MaterialTheme.typography.titleMedium)
        }

        TextButton(onClick = onBack) {
            Text("Back")
        }
    }
}

@Composable
fun BasicProfileScreen(onNext: () -> Unit, onBack: () -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var birthYear by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("") }
    
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val years = (currentYear - 100..currentYear).map { it.toString() }.reversed()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Tell us about yourself",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { /* Show 'Why do we ask?' info */ }) {
                Icon(Icons.Default.Info, contentDescription = "Why do we ask?")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        // First Name
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            placeholder = { Text("What should Gabby call you?") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Birth Year (Simplifying scroll-wheel for now with a dropdown-like behavior)
        var expandedYear by remember { mutableStateOf(false) }
        @OptIn(ExperimentalMaterial3Api::class)
        ExposedDropdownMenuBox(
            expanded = expandedYear,
            onExpandedChange = { expandedYear = !expandedYear },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = birthYear,
                onValueChange = {},
                readOnly = true,
                label = { Text("Birth Year") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedYear) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedYear,
                onDismissRequest = { expandedYear = false }
            ) {
                years.take(50).forEach { year -> 
                    DropdownMenuItem(
                        text = { Text(year) },
                        onClick = {
                            birthYear = year
                            expandedYear = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sex Selector
        Text(
            text = "Sex",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { sex = "Male" },
                modifier = Modifier.weight(1f),
                colors = if (sex == "Male") ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors()
            ) {
                Text("Male")
            }
            OutlinedButton(
                onClick = { sex = "Female" },
                modifier = Modifier.weight(1f),
                colors = if (sex == "Female") ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors()
            ) {
                Text("Female")
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = firstName.isNotBlank() && birthYear.isNotBlank() && sex.isNotBlank()
        ) {
            Text("Next", style = MaterialTheme.typography.titleMedium)
        }

        TextButton(onClick = onBack) {
            Text("Back")
        }
    }
}

@Composable
fun AgeProfileConfirmationScreen(age: Int, onNext: (Boolean) -> Unit, onBack: () -> Unit) {
    val profileName = when {
        age < 18 -> "Explorer Mode"
        age >= 60 -> "Guided Mode"
        else -> "Standard Mode"
    }
    val isChildOrSenior = age < 18 || age >= 60
    val profileDescription = when {
        age < 18 -> "A fun, badge-filled adventure for young champions."
        age >= 60 -> "A gentle, clear, and high-support guide for your health."
        else -> "A streamlined, efficient way to track your progress."
    }
    val profileIcon = when {
        age < 18 -> Icons.Default.RocketLaunch
        age >= 60 -> Icons.Default.SupportAgent
        else -> Icons.Default.Person
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Gabby greeting placeholder
        Box(modifier = Modifier.size(120.dp).background(Color.Gray.copy(0.2f), MaterialTheme.shapes.medium)) {
            Text("Gabby says Hi!", modifier = Modifier.align(Alignment.Center))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(text = "Profile Assigned", style = MaterialTheme.typography.labelLarge)
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next"
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = profileName, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = profileDescription, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = { onNext(isChildOrSenior) },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Let's Go", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun GuardianSetupScreen(onNext: () -> Unit, onSkip: () -> Unit) {
    var mobileNumber by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(text = "Family Partner", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Text(text = "Who should celebrate your progress with you?", style = MaterialTheme.typography.bodyMedium)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = mobileNumber,
            onValueChange = { mobileNumber = it.filter { c -> c.isDigit() } },
            label = { Text("Mobile Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val relations = listOf("Parent", "Spouse", "Sibling", "Child", "Other")
        Text(text = "Relationship", style = MaterialTheme.typography.titleSmall)
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            relations.take(3).forEach { rel ->
                // 1. Determine the border first
                val chipBorder = if (relationship == rel) {
                    androidx.compose.foundation.BorderStroke(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = Color.LightGray
                    )
                }

                // 2. Pass the pre-calculated border
                SuggestionChip(
                    onClick = { relationship = rel },
                    label = { Text(rel) },
                    border = chipBorder
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(56.dp), enabled = mobileNumber.length >= 10 && relationship.isNotBlank()) {
            Text("Confirm")
        }
        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text("Skip for now", color = Color.Gray)
        }
    }
}

@Composable
fun LiteracyAssessmentScreen(onNext: () -> Unit) {
    var currentTask by remember { mutableIntStateOf(1) }
    
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        when (currentTask) {
            1 -> {
                Text("Tap the glowing button", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { currentTask = 2 }, modifier = Modifier.size(100.dp)) { Text("Tap Me") }
            }
            2 -> {
                Text("Slide the button to the right", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(32.dp))
                // Simple representation for now
                Button(onClick = { currentTask = 3 }) { Text("Slide ->") }
            }
            3 -> {
                Text("Press and hold for 2 seconds", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { onNext() }) { Text("Hold Me") }
            }
        }
    }
}

@Composable
fun PermissionsHubScreen(onNext: () -> Unit) {
    var currentStep by remember { mutableIntStateOf(1) }
    
    val steps = listOf(
        Triple("Camera", Icons.Default.CameraAlt, "Needed to record your daily dose videos."),
        Triple("Microphone", Icons.Default.Mic, "Allows you to talk to Gabby during sessions."),
        Triple("Notifications", Icons.Default.Notifications, "We'll remind you when it's time for your medicine.")
    )
    
    val (title, icon, description) = steps[currentStep - 1]

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = description, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = {
                if (currentStep < steps.size) currentStep++ else onNext()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Allow")
        }
        TextButton(onClick = { if (currentStep < steps.size) currentStep++ else onNext() }) {
            Text("Not Now", color = Color.Gray)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Step $currentStep of ${steps.size}", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun CompanionIntroScreen(onNext: () -> Unit) {
    var interactionCount by remember { mutableIntStateOf(0) }
    val gabbyMessage = when (interactionCount) {
        0 -> "Hi! I'm Gabby. I'll be your health companion."
        1 -> "We're going to get healthy together, step by step."
        else -> "Give me a tap to say hello!"
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated Gabby Placeholder
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                .clickable { if (interactionCount < 2) interactionCount++ },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = gabbyMessage,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (interactionCount >= 2) {
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("Continue")
            }
        } else {
            Text(text = "Tap Gabby to respond", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        }
    }
}

@Composable
fun StreakTutorialScreen(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Whatshot, contentDescription = null, modifier = Modifier.size(120.dp), tint = Color(0xFFE65100))
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "What is a Streak?", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Every day you take your medicine, your streak grows. It's a way to celebrate your consistency!",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "What if I miss a day?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = "Don't worry! Life happens. Gabby will help you get back on track without losing your progress immediately.", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Got it!")
        }
    }
}

@Composable
fun VideoRecordTutorialScreen(onNext: () -> Unit) {
    val steps = listOf(
        "Talk to Gabby" to Icons.Default.ChatBubbleOutline,
        "Camera Activates" to Icons.Default.Videocam,
        "Show Medicine" to Icons.Default.MedicalServices,
        "Show Empty Mouth" to Icons.Default.TagFaces
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "How to Record", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(32.dp))

        steps.forEachIndexed { index, step ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "${index + 1}", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Icon(step.second, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = step.first, style = MaterialTheme.typography.bodyLarge)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(onClick = { /* Practice Mode */ }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Try Practice Mode")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("I'm Ready")
        }
    }
}

@Composable
fun ProviderIntroScreen(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(100.dp).background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.HealthAndSafety, contentDescription = null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.tertiary)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(text = "Your Health Guide", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Nurse Maria", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = "\"I'll be watching your progress. I'm proud of you for starting this journey!\"",
                modifier = Modifier.padding(24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Meet My Guide")
        }
    }
}

@Composable
fun NotificationSetupScreen(onNext: () -> Unit) {
    var reminderTime by remember { mutableStateOf("08:00 AM") }
    var notificationStyle by remember { mutableStateOf("Sound + Banner") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Dose Reminders", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Text(text = "When should Gabby remind you to take your medicine?", textAlign = androidx.compose.ui.text.style.TextAlign.Center)

        Spacer(modifier = Modifier.height(48.dp))

        // Mock Time Picker
        Card(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(text = reminderTime, style = MaterialTheme.typography.displayMedium)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Reminder Style", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
        val styles = listOf("Sound + Banner", "Banner Only", "Gabby Voice")
        styles.forEach { style ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { notificationStyle = style }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = notificationStyle == style, onClick = { notificationStyle = style })
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = style)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Save Preferences")
        }
    }
}

@Composable
fun SummaryReviewScreen(onNext: () -> Unit) {
    val settings = listOf(
        "Language" to "English",
        "Text Size" to "Standard",
        "Voice Mode" to "Off",
        "Reminder" to "8:00 AM",
        "Profile" to "Standard Mode"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())
    ) {
        Text(text = "Review Settings", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Text(text = "Everything looks good? You can change these later in Settings.")

        Spacer(modifier = Modifier.height(24.dp))

        settings.forEach { (label, value) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { /* Navigate back to specific setup */ }) {
                        Text("Change")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Everything Looks Good")
        }
    }
}

@Composable
fun WelcomeCompleteScreen(onBegin: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Celebration,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "You are ready!",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your health journey starts now. Gabby is here to support you.",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onBegin,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimary, contentColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Begin Journey", style = MaterialTheme.typography.titleLarge)
            }
        }
        
        Text(
            text = "Day 1 Streak Starts Today!",
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

private fun Modifier.size(size: Int): Modifier = this.size(size.dp)
