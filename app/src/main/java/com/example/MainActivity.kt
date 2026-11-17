package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.auth.authStateFlow
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainAppScaffold
import com.example.ui.theme.OrgAppTheme
import com.example.util.OrgNotificationHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        OrgNotificationHelper.ensureChannel(this)
        setContent {
            OrgAppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser
    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        key(user.uid) {
            MainAppScaffold(
                currentUserId = user.uid,
                userDisplayName = user.displayName?.takeIf { it.isNotBlank() } ?: "সম্মানিত সদস্য",
                userEmail = user.email ?: ""
            )
        }
    }
}
