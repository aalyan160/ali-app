package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val bgColor = Color(0xFF121414)
    val whiteText = Color(0xFFE2E2E2)
    val mutedText = Color(0xFF94A3B8)
    val tealColor = Color(0xFF00DFC1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = whiteText)
            }
            Text("Privacy Policy", color = whiteText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        
        Text(
            "Welcome to LockedIn. We respect your privacy and are committed to protecting your personal data. This privacy policy explains how we collect and use your data when you use the app.",
            color = mutedText,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text("Data Collection", color = tealColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(
            "We collect minimal data required to provide the core functionality of the app, including study session history, tasks, notes, user preferences, and profile statistics.",
            color = mutedText,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text("Supabase Storage", color = tealColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(
            "Your data is securely stored online using Supabase. This includes your authentication credentials, study sessions (for leaderboard calculations and streak tracking), and profile data. We employ robust security measures to protect your information in the database.",
            color = mutedText,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text("Notifications", color = tealColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(
            "The app uses local notifications to send daily reminders and alerts when study or break sessions finish. Reminders are configured locally on your device based on your settings.",
            color = mutedText,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text("User Rights", color = tealColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(
            "You have the right to access, edit, or delete the data we hold about you by contacting support or managing your account in the app.",
            color = mutedText,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
