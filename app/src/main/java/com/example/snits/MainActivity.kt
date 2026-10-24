package com.example.snits

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.snits.ui.theme.SNITSTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SNITSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("auth") }
                    var studentRollNo by remember { mutableStateOf("") }
                    var userName by remember { mutableStateOf("") }

                    when (currentScreen) {
                        "auth" -> {
                            AuthScreen(
                                onLoginSuccess = { role, rollNo, name ->
                                    studentRollNo = rollNo
                                    userName = name
                                    currentScreen = if (role == "teacher") "teacher" else "student"
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
