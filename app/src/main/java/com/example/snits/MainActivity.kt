package com.example.snits

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
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("splash") }
                    var studentRollNo by remember { mutableStateOf("") }
                    var userName by remember { mutableStateOf("") }

                    // Persistent Session Check (Auto-Login)
                    LaunchedEffect(Unit) {
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (currentUser != null) {
                            try {
                                val email = currentUser.email?.lowercase() ?: ""
                                val users = withContext(Dispatchers.IO) {
                                    SupabaseManager.client.postgrest["users"]
                                        .select { filter { eq("uid", currentUser.uid) } }
                                        .decodeList<SupabaseUser>()
                                }
                                if (users.isNotEmpty()) {
                                    val user = users[0]
                                    studentRollNo = user.rollNo
                                    userName = user.name
                                    currentScreen = if (email.contains("admin") || user.role.lowercase() == "admin") {
                                        "admin"
                                    } else if (user.role.lowercase() == "teacher") {
                                        "teacher"
                                    } else {
                                        "student"
                                    }
                                } else if (email.contains("admin")) {
                                    userName = "Admin"
                                    currentScreen = "admin"
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

                    when (currentScreen) {
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
                                    currentScreen = when (role.lowercase()) {
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
