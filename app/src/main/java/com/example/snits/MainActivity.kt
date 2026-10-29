package com.example.snits

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import com.example.snits.ui.theme.SNITSTheme
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SNITSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val context = LocalContext.current
                    val prefs = remember { context.getSharedPreferences("snits_app_prefs", Context.MODE_PRIVATE) }
                    var isFirstRun by remember { mutableStateOf(prefs.getBoolean("is_first_run", true)) }

                    var currentScreen by remember { mutableStateOf(if (isFirstRun) "intro" else "splash") }
                    var studentRollNo by remember { mutableStateOf("") }
                    var userName by remember { mutableStateOf("") }

                    // Persistent Session Check (Dynamic Role Check from Supabase DB)
                    LaunchedEffect(currentScreen) {
                        if (currentScreen == "splash") {
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            if (currentUser != null) {
                                try {
                                    val users = withContext(Dispatchers.IO) {
                                        SupabaseManager.client.postgrest["users"]
                                            .select { filter { eq("uid", currentUser.uid) } }
                                            .decodeList<SupabaseUser>()
                                    }
                                    if (users.isNotEmpty()) {
                                        val user = users[0]
                                        studentRollNo = user.rollNo
                                        userName = user.name
                                        currentScreen = when (user.role.lowercase().trim()) {
                                            "admin" -> "admin"
                                            "teacher" -> "teacher"
                                            else -> "student"
                                        }
                                    } else {
                                        currentScreen = "auth"
                                    }
                                } catch (_: Exception) {
                                    currentScreen = "auth"
                                }
                            } else {
                                currentScreen = "auth"
                            }
                        }
                    }

                    when (currentScreen) {
                        "intro" -> {
                            AppIntroScreen {
                                prefs.edit { putBoolean("is_first_run", false) }
                                isFirstRun = false
                                currentScreen = "splash"
                            }
                        }
                        "splash" -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        "auth" -> {
                            AuthScreen(
                                onLoginSuccess = { role, rollNo, name ->
                                    studentRollNo = rollNo
                                    userName = name
                                    currentScreen = when (role.lowercase().trim()) {
                                        "admin" -> "admin"
                                        "teacher" -> "teacher"
                                        else -> "student"
                                    }
                                }
                            )
                        }
                        "admin" -> {
                            AdminDashboard(
                                adminName = userName,
                                onLogout = {
                                    FirebaseAuth.getInstance().signOut()
                                    currentScreen = "auth"
                                }
                            )
                        }
                        "teacher" -> {
                            TeacherDashboard(
                                teacherName = userName,
                                onLogout = {
                                    FirebaseAuth.getInstance().signOut()
                                    currentScreen = "auth"
                                }
                            )
                        }
                        "student" -> {
                            StudentDashboard(
                                studentName = userName,
                                studentRollNo = studentRollNo,
                                onLogout = {
                                    FirebaseAuth.getInstance().signOut()
                                    currentScreen = "auth"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
