package com.example.snits

import android.app.Activity
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

// ==========================================
// 0. APP INTRO / ONBOARDING SCREEN (First Time Launch)
// ==========================================
data class IntroPageData(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val badgeText: String
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AppIntroScreen(onFinishIntro: () -> Unit) {
    val scope = rememberCoroutineScope()
    val pages = listOf(
        IntroPageData(
            title = "Welcome to SNITS Portal",
            description = "Your official digital campus platform for real-time attendance, timetables, notes, and academic updates.",
            icon = Icons.Default.School,
            badgeText = "SNITS College Portal"
        ),
        IntroPageData(
            title = "Smart Attendance Tracker",
            description = "Mark daily class attendance in 1-click, view your attendance percentage %, and export detailed Excel reports.",
            icon = Icons.Default.Assessment,
            badgeText = "Attendance & Reports"
        ),
        IntroPageData(
            title = "Class Timetables & Notes",
            description = "Access year-specific class schedules, teacher timetables, lecture notes, and assignments anytime, anywhere.",
            icon = Icons.Default.Schedule,
            badgeText = "Timetables & Study Notes"
        )
    )

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { pages.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onFinishIntro) {
                    Text("Skip", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val data = pages[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(110.dp),
                        shadowElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = data.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = data.badgeText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = data.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = data.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        val width = if (pagerState.currentPage == iteration) 24.dp else 8.dp
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .background(color, shape = RoundedCornerShape(4.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinishIntro()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (pagerState.currentPage == pages.size - 1) Icons.Default.ArrowForward else Icons.Default.ChevronRight,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

// ==========================================
// COMMON UI: ALIGNED PORTAL TOP HEADER
// ==========================================
@Composable
fun PortalTopHeader(
    portalTitle: String,
    userName: String,
    userRoleOrRoll: String,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var isUpdatingPassword by remember { mutableStateOf(false) }

    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Password") },
            text = {
                Column {
                    Text("Enter your new password (minimum 6 characters):", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPassword.trim().length < 6) {
                            Toast.makeText(context, "Password kam se kam 6 characters ka hona chahiye!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isUpdatingPassword = true
                        val user = FirebaseAuth.getInstance().currentUser
                        user?.updatePassword(newPassword.trim())
                            ?.addOnSuccessListener {
                                isUpdatingPassword = false
                                showChangePasswordDialog = false
                                newPassword = ""
                                Toast.makeText(context, "Password Updated Successfully!", Toast.LENGTH_SHORT).show()
                            }
                            ?.addOnFailureListener { e ->
                                isUpdatingPassword = false
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    },
                    enabled = !isUpdatingPassword
                ) {
                    if (isUpdatingPassword) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    } else {
                        Text("Update")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(6.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(46.dp),
                        shadowElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "SNITS Logo",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = portalTitle,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "SNITS College Portal (Supabase DB)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = { showChangePasswordDialog = true },
                        modifier = Modifier.size(38.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Change Password",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    FilledTonalButton(
                        onClick = onLogout,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Logout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = userName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (userRoleOrRoll.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = userRoleOrRoll,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. AUTH SCREEN (Email/Pass + Firebase Phone SMS OTP)
// ==========================================
@Composable
fun AuthScreen(onLoginSuccess: (role: String, rollNo: String, name: String) -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    var loginMode by remember { mutableIntStateOf(0) } // 0 = Email, 1 = Phone SMS OTP

    // Email state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Phone SMS OTP state
    var phoneNumber by remember { mutableStateOf("+91") }
    var smsCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }

    fun processPhoneSignIn(credential: PhoneAuthCredential) {
        isLoading = true
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val uid = result.user!!.uid
                val userPhone = result.user?.phoneNumber ?: phoneNumber

                scope.launch {
                    var userRole = "student"
                    var userRoll = "PHONE_USER"
                    var userName = "Student User"

                    try {
                        val users = withContext(Dispatchers.IO) {
                            SupabaseManager.client.postgrest["users"]
                                .select { filter { eq("uid", uid) } }
                                .decodeList<SupabaseUser>()
                        }

                        if (users.isNotEmpty()) {
                            val user = users[0]
                            userRole = user.role
                            userRoll = user.rollNo
                            userName = user.name
                        } else {
                            val newUser = SupabaseUser(
                                uid = uid,
                                name = "User ($userPhone)",
                                email = "$userPhone@college.com",
                                role = "student",
                                rollNo = "STUDENT",
                                year = "1st Year"
                            )
                            try {
                                withContext(Dispatchers.IO) {
                                    SupabaseManager.client.postgrest["users"].upsert(newUser)
                                }
                            } catch (_: Exception) {}
                        }
                    } catch (_: Exception) {
                    } finally {
                        isLoading = false
                        onLoginSuccess(userRole, userRoll, userName)
                    }
                }
            }
            .addOnFailureListener { e ->
                isLoading = false
                Toast.makeText(context, "Verification Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp),
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "SNITS Logo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SNITS College Portal",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Sign in to your account",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // LOGIN MODE SEGMENTED TOGGLE (Email vs Phone SMS)
            PrimaryTabRow(
                selectedTabIndex = loginMode,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = loginMode == 0,
                    onClick = { loginMode = 0 },
                    text = { Text("Email Login", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = loginMode == 1,
                    onClick = { loginMode = 1 },
                    text = { Text("Phone SMS OTP", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.elevatedCardElevation(6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    if (loginMode == 0) {
                        // --- EMAIL & PASSWORD LOGIN ---
                        Text(
                            text = "Email & Password Sign In",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("College Email ID") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else {
                            Button(
                                onClick = {
                                    val cleanEmail = email.trim().lowercase()
                                    val cleanPass = password.trim()

                                    if (cleanEmail.isBlank() || cleanPass.isBlank()) {
                                        Toast.makeText(context, "Email aur Password dono bharein!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isLoading = true

                                    auth.signInWithEmailAndPassword(cleanEmail, cleanPass)
                                        .addOnSuccessListener { result ->
                                            val uid = result.user!!.uid

                                            scope.launch {
                                                var userRole = "student"
                                                var userRoll = cleanEmail.substringBefore("@").uppercase()
                                                var userName = cleanEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }

                                                try {
                                                    val users = withContext(Dispatchers.IO) {
                                                        SupabaseManager.client.postgrest["users"]
                                                            .select { filter { eq("uid", uid) } }
                                                            .decodeList<SupabaseUser>()
                                                    }

                                                    if (users.isNotEmpty()) {
                                                        val user = users[0]
                                                        userRole = user.role
                                                        userRoll = user.rollNo
                                                        userName = user.name
                                                    } else {
                                                        val emailUsers = withContext(Dispatchers.IO) {
                                                            SupabaseManager.client.postgrest["users"]
                                                                .select { filter { eq("email", cleanEmail) } }
                                                                .decodeList<SupabaseUser>()
                                                        }

                                                        if (emailUsers.isNotEmpty()) {
                                                            val foundUser = emailUsers[0]
                                                            userRole = foundUser.role
                                                            userRoll = foundUser.rollNo
                                                            userName = foundUser.name

                                                            val syncedUser = SupabaseUser(
                                                                uid = uid,
                                                                name = foundUser.name,
                                                                email = cleanEmail,
                                                                role = foundUser.role,
                                                                rollNo = foundUser.rollNo,
                                                                year = foundUser.year
                                                            )
                                                            try {
                                                                withContext(Dispatchers.IO) {
                                                                    SupabaseManager.client.postgrest["users"].upsert(syncedUser)
                                                                }
                                                            } catch (_: Exception) {}
                                                        } else {
                                                            val derivedRole = when {
                                                                cleanEmail.contains("admin") -> "admin"
                                                                cleanEmail.contains("teacher") -> "teacher"
                                                                else -> "student"
                                                            }
                                                            userRole = derivedRole
                                                            val newUser = SupabaseUser(
                                                                uid = uid,
                                                                name = userName,
                                                                email = cleanEmail,
                                                                role = userRole,
                                                                rollNo = if (userRole == "student") userRoll else if (userRole == "admin") "ADMIN" else "TEACHER",
                                                                year = "1st Year"
                                                            )
                                                            try {
                                                                withContext(Dispatchers.IO) {
                                                                    SupabaseManager.client.postgrest["users"].upsert(newUser)
                                                                }
                                                            } catch (_: Exception) {}
                                                        }
                                                    }
                                                } catch (_: Exception) {
                                                } finally {
                                                    isLoading = false
                                                    onLoginSuccess(userRole, userRoll, userName)
                                                }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            Toast.makeText(context, "Login Failed: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Login, contentDescription = "Login")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Login with Email", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // --- PHONE SMS OTP VERIFICATION LOGIN ---
                        Text(
                            text = "Firebase SMS OTP Sign In",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number (+91 9876543210)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
                            singleLine = true,
                            enabled = !isOtpSent,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (isOtpSent) {
                            OutlinedTextField(
                                value = smsCode,
                                onValueChange = { smsCode = it },
                                label = { Text("Enter 6-Digit SMS OTP") },
                                leadingIcon = { Icon(Icons.Default.Sms, contentDescription = "OTP") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else {
                            if (!isOtpSent) {
                                Button(
                                    onClick = {
                                        if (phoneNumber.trim().length < 10) {
                                            Toast.makeText(context, "Sahi Phone Number (+91) likhein!", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        if (activity == null) {
                                            Toast.makeText(context, "Activity error!", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        isLoading = true

                                        val formattedPhone = if (!phoneNumber.startsWith("+")) "+$phoneNumber" else phoneNumber

                                        val options = PhoneAuthOptions.newBuilder(auth)
                                            .setPhoneNumber(formattedPhone.trim())
                                            .setTimeout(60L, TimeUnit.SECONDS)
                                            .setActivity(activity)
                                            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                                    processPhoneSignIn(credential)
                                                }

                                                override fun onVerificationFailed(e: FirebaseException) {
                                                    isLoading = false
                                                    Toast.makeText(context, "OTP Error: ${e.message}", Toast.LENGTH_LONG).show()
                                                }

                                                override fun onCodeSent(
                                                    vId: String,
                                                    token: PhoneAuthProvider.ForceResendingToken
                                                ) {
                                                    isLoading = false
                                                    verificationId = vId
                                                    isOtpSent = true
                                                    Toast.makeText(context, "SMS OTP sent to $formattedPhone", Toast.LENGTH_SHORT).show()
                                                }
                                            })
                                            .build()

                                        PhoneAuthProvider.verifyPhoneNumber(options)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Sms, contentDescription = "Send OTP")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Send SMS OTP", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (smsCode.trim().length < 6) {
                                            Toast.makeText(context, "6-digit OTP code likhein!", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        val credential = PhoneAuthProvider.getCredential(verificationId, smsCode.trim())
                                        processPhoneSignIn(credential)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = "Verify OTP")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Verify OTP & Login", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = {
                                        isOtpSent = false
                                        smsCode = ""
                                    },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Change Phone Number")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Firebase Auth SMS OTP Verification & Supabase Database.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. ADMIN DASHBOARD (Full Access + User Registration)
// ==========================================
@Composable
fun AdminDashboard(adminName: String, onLogout: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Class Attendance", "Timetable", "Notes & Materials", "Register Users")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            PortalTopHeader(
                portalTitle = "SNITS Admin Panel",
                userName = adminName,
                userRoleOrRoll = "Admin",
                onLogout = onLogout
            )

            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> ClassAttendanceSection()
                1 -> TimetableManagementSection()
                2 -> MaterialsManagementSection()
                3 -> UserRegistrationSection()
            }
        }
    }
}

// ==========================================
// 3. TEACHER DASHBOARD (Attendance, Teacher Timetable, Notes)
// ==========================================
@Composable
fun TeacherDashboard(teacherName: String, onLogout: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Class Attendance", "My Timetable", "Notes & Materials")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            PortalTopHeader(
                portalTitle = "SNITS Teacher Panel",
                userName = teacherName,
                userRoleOrRoll = "Teacher",
                onLogout = onLogout
            )

            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> ClassAttendanceSection()
                1 -> TeacherTimetableSection()
                2 -> MaterialsManagementSection()
            }
        }
    }
}

// Helper function to fetch all attendance with 100% column name resilience
suspend fun fetchAllAttendanceFromSupabase(): List<SupabaseAttendance> {
    return withContext(Dispatchers.IO) {
        val response = SupabaseManager.client.postgrest["attendance"].select()
        val jsonArray = response.decodeList<JsonObject>()
        jsonArray.map { obj ->
            val docId = obj["docId"]?.jsonPrimitive?.contentOrNull
                ?: obj["docid"]?.jsonPrimitive?.contentOrNull
                ?: obj["id"]?.jsonPrimitive?.contentOrNull
                ?: ""

            val rollNo = obj["rollNo"]?.jsonPrimitive?.contentOrNull
                ?: obj["rollno"]?.jsonPrimitive?.contentOrNull
                ?: obj["roll_no"]?.jsonPrimitive?.contentOrNull
                ?: ""

            val date = obj["date"]?.jsonPrimitive?.contentOrNull ?: ""
            val subject = obj["subject"]?.jsonPrimitive?.contentOrNull ?: ""
            val status = obj["status"]?.jsonPrimitive?.contentOrNull ?: ""
            val year = obj["year"]?.jsonPrimitive?.contentOrNull ?: "1st Year"
            val timestamp = obj["timestamp"]?.jsonPrimitive?.longOrNull ?: System.currentTimeMillis()

            SupabaseAttendance(
                docId = docId,
                rollNo = rollNo,
                date = date,
                subject = subject,
                status = status,
                year = year,
                timestamp = timestamp
            )
        }
    }
}

// --- CLASS ATTENDANCE SECTION ---
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ClassAttendanceSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val todayDate = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()) }

    var date by remember { mutableStateOf(todayDate) }
    var subject by remember { mutableStateOf("") }
    var expandedSubjectDropdown by remember { mutableStateOf(false) }
    var selectedYearFilter by remember { mutableStateOf("All Years") }
    var isSaving by remember { mutableStateOf(false) }

    var studentList by remember { mutableStateOf(listOf<SupabaseUser>()) }
    val attendanceStatusMap = remember { mutableStateMapOf<String, Boolean>() }

    var filterRoll by remember { mutableStateOf("") }
    var searchedRecords by remember { mutableStateOf(listOf<SupabaseAttendance>()) }
    var isSearching by remember { mutableStateOf(false) }
    val studentNameMap = remember { mutableStateMapOf<String, String>() }

    val subjectOptions = listOf("C", "C++", "Python", "Java", "DBMS", "Maths", "Physics", "Web Development")
    val yearOptions = listOf("All Years", "1st Year", "2nd Year", "3rd Year", "4th Year")

    fun loadStudents() {
        scope.launch {
            try {
                val list = withContext(Dispatchers.IO) {
                    SupabaseManager.client.postgrest["users"]
                        .select()
                        .decodeList<SupabaseUser>()
                }
                val studentsOnly = list.filter { it.role == "student" }.sortedBy { it.rollNo }
                studentList = studentsOnly

                list.forEach { u -> studentNameMap[u.rollNo] = u.name }

                studentsOnly.forEach {
                    if (!attendanceStatusMap.containsKey(it.rollNo)) {
                        attendanceStatusMap[it.rollNo] = true
                    }
                }
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        loadStudents()
    }

    val filteredStudentList = if (selectedYearFilter == "All Years") {
        studentList
    } else {
        studentList.filter { it.year == selectedYearFilter }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Daily Class Attendance Sheet", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Select Year & Subject from dropdown to mark daily attendance.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(14.dp))

            Text("1. Select Student Year:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                yearOptions.forEach { yr ->
                    FilterChip(
                        selected = selectedYearFilter == yr,
                        onClick = { selectedYearFilter = yr },
                        label = { Text(yr, fontSize = 12.sp) },
                        leadingIcon = if (selectedYearFilter == yr) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (DD-MM-YYYY)") },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Date") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text("2. Subject Name (1-Click Dropdown):", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            ExposedDropdownMenuBox(
                expanded = expandedSubjectDropdown,
                onExpandedChange = { expandedSubjectDropdown = !expandedSubjectDropdown }
            ) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Select or Type Subject Name") },
                    leadingIcon = { Icon(Icons.Default.Book, contentDescription = "Subject") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubjectDropdown) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                        .fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                ExposedDropdownMenu(
                    expanded = expandedSubjectDropdown,
                    onDismissRequest = { expandedSubjectDropdown = false }
                ) {
                    subjectOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, fontSize = 14.sp) },
                            onClick = {
                                subject = option
                                expandedSubjectDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        filteredStudentList.forEach { attendanceStatusMap[it.rollNo] = true }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Mark All Present", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = {
                        filteredStudentList.forEach { attendanceStatusMap[it.rollNo] = false }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Mark All Absent", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            if (filteredStudentList.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No students found for $selectedYearFilter in Supabase DB.",
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Students List ($selectedYearFilter)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                        Text("${filteredStudentList.size} Students", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                filteredStudentList.forEach { student ->
                    val isPresent = attendanceStatusMap[student.rollNo] ?: true

                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isPresent) Color(0xFFF1F8E9) else Color(0xFFFFEBEE)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${student.rollNo} - ${student.name}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = student.year,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (isPresent) "Status: Present" else "Status: Absent",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isPresent) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }

                            Row {
                                FilterChip(
                                    selected = isPresent,
                                    onClick = { attendanceStatusMap[student.rollNo] = true },
                                    label = { Text("Present") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF2E7D32),
                                        selectedLabelColor = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                FilterChip(
                                    selected = !isPresent,
                                    onClick = { attendanceStatusMap[student.rollNo] = false },
                                    label = { Text("Absent") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFC62828),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Button(
                        onClick = {
                            if (date.isBlank() || subject.isBlank()) {
                                Toast.makeText(context, "Date aur Subject dono bharein!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isSaving = true

                            scope.launch {
                                try {
                                    val attendanceBatch = filteredStudentList.map { student ->
                                        SupabaseAttendance(
                                            docId = "${student.rollNo}_${date.trim()}_${subject.trim()}",
                                            rollNo = student.rollNo,
                                            date = date.trim(),
                                            subject = subject.trim(),
                                            status = if (attendanceStatusMap[student.rollNo] == true) "Present" else "Absent",
                                            year = student.year,
                                            timestamp = System.currentTimeMillis()
                                        )
                                    }

                                    withContext(Dispatchers.IO) {
                                        SupabaseManager.client.postgrest["attendance"].upsert(attendanceBatch)
                                    }

                                    isSaving = false
                                    Toast.makeText(context, "Attendance Saved to Supabase for ${filteredStudentList.size} Students!", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    isSaving = false
                                    Toast.makeText(context, "Supabase Save Error: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Attendance to Supabase", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // SECTION 2: ALL ATTENDANCE RECORDS (VIEW FULL ATTENDANCE, EXPORT TO EXCEL/CSV & SEARCH)
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("2. All Attendance Records", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("View full attendance records, search by Roll No, or download report in Excel/CSV format.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = filterRoll,
                    onValueChange = { filterRoll = it.uppercase() },
                    label = { Text("Roll No (e.g. 22CS01)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
                Button(
                    onClick = {
                        if (filterRoll.isBlank()) {
                            Toast.makeText(context, "Roll No likhein!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSearching = true
                        scope.launch {
                            try {
                                val allRecords = fetchAllAttendanceFromSupabase()
                                val matched = allRecords.filter { it.rollNo.equals(filterRoll.trim(), ignoreCase = true) }
                                isSearching = false
                                searchedRecords = matched
                                if (matched.isEmpty()) {
                                    Toast.makeText(context, "Roll No ${filterRoll.trim()} ka koi record nahi mila!", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                isSearching = false
                                Toast.makeText(context, "Search Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Search")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = {
                        isSearching = true
                        scope.launch {
                            try {
                                val records = fetchAllAttendanceFromSupabase()
                                isSearching = false
                                searchedRecords = records
                                if (records.isEmpty()) {
                                    Toast.makeText(context, "Database me abhi koi attendance record nahi hai!", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Database se ${records.size} attendance records load hue!", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                isSearching = false
                                Toast.makeText(context, "Fetch Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ListAlt, contentDescription = "Full Attendance", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Full Attendance", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        scope.launch {
                            try {
                                isSearching = true
                                val records = fetchAllAttendanceFromSupabase()
                                isSearching = false
                                searchedRecords = records
                                if (records.isEmpty()) {
                                    Toast.makeText(context, "Database me export karne ke liye koi attendance record nahi h!", Toast.LENGTH_LONG).show()
                                } else {
                                    downloadAttendanceReport(context, records, studentNameMap)
                                }
                            } catch (e: Exception) {
                                isSearching = false
                                Toast.makeText(context, "Export Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Download Excel", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download Excel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isSearching) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (searchedRecords.isEmpty()) {
                Text("Click 'Full Attendance' or search by Roll No to view/download records.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Records Found:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                        Text("${searchedRecords.size} Records", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                searchedRecords.forEach { record ->
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                val name = studentNameMap[record.rollNo] ?: ""
                                Text("${record.rollNo} ${if (name.isNotBlank()) "($name)" else ""} | ${record.date}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Subject: ${record.subject} | Year: ${record.year}", fontSize = 12.sp)
                                Text(
                                    text = "Status: ${record.status}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (record.status == "Present") Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }
                            Row {
                                TextButton(onClick = {
                                    val newStatus = if (record.status == "Present") "Absent" else "Present"
                                    scope.launch {
                                        try {
                                            val updated = record.copy(status = newStatus)
                                            withContext(Dispatchers.IO) {
                                                SupabaseManager.client.postgrest["attendance"].upsert(updated)
                                            }
                                            searchedRecords = searchedRecords.map {
                                                if (it.docId == record.docId) updated else it
                                            }
                                            Toast.makeText(context, "Updated to $newStatus!", Toast.LENGTH_SHORT).show()
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "Update error!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }) {
                                    Text("Change")
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        try {
                                            withContext(Dispatchers.IO) {
                                                SupabaseManager.client.postgrest["attendance"]
                                                    .delete { filter { eq("docId", record.docId) } }
                                            }
                                            searchedRecords = searchedRecords.filter { it.docId != record.docId }
                                            Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show()
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "Delete error!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Download Attendance Sheet to CSV / Excel File with System Notification & Direct Open Action
fun downloadAttendanceReport(
    context: android.content.Context,
    attendanceList: List<SupabaseAttendance>,
    studentMap: Map<String, String>
) {
    if (attendanceList.isEmpty()) {
        Toast.makeText(context, "Pehle attendance records load karein!", Toast.LENGTH_SHORT).show()
        return
    }

    // Check & Request POST_NOTIFICATIONS runtime permission on Android 13+ (SDK 33+)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        val activity = context as? android.app.Activity
        if (activity != null && androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            androidx.core.app.ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }
    }

    try {
        val fileName = "SNITS_Attendance_Report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"

        val csvBuilder = StringBuilder()
        csvBuilder.append("Roll No,Student Name,Date,Subject,Academic Year,Status\n")

        for (item in attendanceList) {
            val studentName = studentMap[item.rollNo] ?: "N/A"
            csvBuilder.append("\"${item.rollNo}\",\"$studentName\",\"${item.date}\",\"${item.subject}\",\"${item.year}\",\"${item.status}\"\n")
        }

        // Save locally in external files dir for FileProvider Intent
        val reportsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Reports").apply { mkdirs() }
        val localFile = File(reportsDir, fileName)
        localFile.writeText(csvBuilder.toString())

        // Save to public Downloads via MediaStore if available
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { output ->
                    output.write(csvBuilder.toString().toByteArray())
                }
            }
        }

        // Generate FileProvider Content URI to open in Excel / Sheets
        val fileUri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            localFile
        )

        val openIntent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "text/csv")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            openIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        // Notification Action Button
        val openAction = androidx.core.app.NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_view,
            "OPEN REPORT 📊",
            pendingIntent
        ).build()

        // Post System Notification
        val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channelId = "snits_downloads_channel"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Attendance Reports Download",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for downloaded attendance reports"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Download Successful 📊")
            .setContentText("Report saved ($fileName). Tap or click 'OPEN REPORT'.")
            .setStyle(
                androidx.core.app.NotificationCompat.BigTextStyle().bigText(
                    "Attendance report saved ($fileName).\n\nTap this notification or click 'OPEN REPORT' below to open the file in Excel / Sheets."
                )
            )
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MAX)
            .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .addAction(openAction)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)

        Toast.makeText(context, "Download Successful! Tap Notification or 'OPEN REPORT' to open in Excel.", Toast.LENGTH_LONG).show()

    } catch (e: Exception) {
        Toast.makeText(context, "Download Error: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

// --- TIMETABLE MANAGEMENT SECTION (ADMIN) ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimetableManagementSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetYear by remember { mutableStateOf("1st Year") }
    var targetRole by remember { mutableStateOf("student") }
    var rollNo by remember { mutableStateOf("ALL") }

    var timetableList by remember { mutableStateOf(listOf<SupabaseTimetable>()) }
    var isPublishing by remember { mutableStateOf(false) }

    var selectedYearFilter by remember { mutableStateOf("All") }

    val yearOptions = listOf("1st Year", "2nd Year", "3rd Year", "4th Year", "Teachers", "All")

    fun loadTimetables() {
        scope.launch {
            try {
                val list = withContext(Dispatchers.IO) {
                    SupabaseManager.client.postgrest["timetables"]
                        .select()
                        .decodeList<SupabaseTimetable>()
                }
                timetableList = list.sortedByDescending { it.timestamp }
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        loadTimetables()
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Publish College Timetable / Schedule", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Publish year-wise student timetables or teacher schedules stored in Supabase DB.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(12.dp))

            Text("Select Target Group / Year:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                yearOptions.forEach { yr ->
                    FilterChip(
                        selected = targetYear == yr,
                        onClick = {
                            targetYear = yr
                            targetRole = if (yr == "Teachers") "teacher" else "student"
                        },
                        label = { Text(yr, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Timetable Title (e.g. 1st Year CS Weekly Schedule)") },
                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = "Schedule") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Timetable Details / Subject Hours / PDF Drive Link") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = "Details") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = rollNo,
                onValueChange = { rollNo = it.uppercase() },
                label = { Text("Target Roll No ('ALL' or specific Roll No)") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = "Roll") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (isPublishing) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        if (title.isBlank() || description.isBlank()) {
                            Toast.makeText(context, "Title aur Description dono bharein!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isPublishing = true
                        scope.launch {
                            try {
                                val newTt = SupabaseTimetable(
                                    id = UUID.randomUUID().toString(),
                                    title = title.trim(),
                                    description = description.trim(),
                                    targetYear = targetYear,
                                    targetRole = targetRole,
                                    rollNo = if (rollNo.isBlank()) "ALL" else rollNo.trim().uppercase(),
                                    timestamp = System.currentTimeMillis()
                                )
                                withContext(Dispatchers.IO) {
                                    SupabaseManager.client.postgrest["timetables"].insert(newTt)
                                }
                                isPublishing = false
                                title = ""
                                description = ""
                                rollNo = "ALL"
                                Toast.makeText(context, "Timetable Published Successfully!", Toast.LENGTH_SHORT).show()
                                loadTimetables()
                            } catch (e: Exception) {
                                isPublishing = false
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Publish, contentDescription = "Publish")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publish Timetable", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text("Full Database Timetables (${timetableList.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(modifier = Modifier.height(4.dp))

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        (listOf("All") + yearOptions).distinct().forEach { yr ->
            FilterChip(
                selected = selectedYearFilter == yr,
                onClick = { selectedYearFilter = yr },
                label = { Text(yr, fontSize = 11.sp) }
            )
        }
    }

    val filteredList = if (selectedYearFilter == "All") {
        timetableList
    } else {
        timetableList.filter { it.targetYear == selectedYearFilter }
    }

    Spacer(modifier = Modifier.height(6.dp))

    if (filteredList.isEmpty()) {
        Text("No timetables found in database.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        filteredList.forEach { item ->
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = item.targetYear,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (item.rollNo != "ALL") {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "Roll: ${item.rollNo}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(item.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    IconButton(
                        onClick = {
                            scope.launch {
                                try {
                                    withContext(Dispatchers.IO) {
                                        SupabaseManager.client.postgrest["timetables"].delete { filter { eq("id", item.id) } }
                                    }
                                    Toast.makeText(context, "Timetable Deleted!", Toast.LENGTH_SHORT).show()
                                    loadTimetables()
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Delete error!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// --- TEACHER TIMETABLE SECTION ---
@Composable
fun TeacherTimetableSection() {
    val scope = rememberCoroutineScope()
    var teacherTimetables by remember { mutableStateOf(listOf<SupabaseTimetable>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val allTt = withContext(Dispatchers.IO) {
                    SupabaseManager.client.postgrest["timetables"]
                        .select()
                        .decodeList<SupabaseTimetable>()
                }
                teacherTimetables = allTt.filter {
                    it.targetYear == "Teachers" || it.targetRole == "teacher" || it.targetYear == "All"
                }
                isLoading = false
            } catch (_: Exception) {
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Teacher Schedule & Timetable", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (teacherTimetables.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("No teacher schedule published in database yet.", fontSize = 13.sp, modifier = Modifier.padding(12.dp))
            }
        } else {
            teacherTimetables.forEach { item ->
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// --- STUDENT TIMETABLE SECTION ---
@Composable
fun StudentTimetableSection(studentRollNo: String) {
    val scope = rememberCoroutineScope()
    var studentYear by remember { mutableStateOf("1st Year") }
    var studentTimetables by remember { mutableStateOf(listOf<SupabaseTimetable>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(studentRollNo) {
        scope.launch {
            try {
                val users = withContext(Dispatchers.IO) {
                    SupabaseManager.client.postgrest["users"]
                        .select { filter { eq("rollNo", studentRollNo) } }
                        .decodeList<SupabaseUser>()
                }
                if (users.isNotEmpty()) {
                    studentYear = users[0].year
                }

                val allTt = withContext(Dispatchers.IO) {
                    SupabaseManager.client.postgrest["timetables"]
                        .select()
                        .decodeList<SupabaseTimetable>()
                }

                val myTt = allTt.filter {
                    it.targetYear == studentYear ||
                    it.targetYear == "All" ||
                    it.rollNo == studentRollNo ||
                    it.rollNo == "ALL"
                }

                isLoading = false
                studentTimetables = myTt
            } catch (_: Exception) {
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My Class Timetable ($studentYear)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                Text(studentYear, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (studentTimetables.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No timetable published for $studentYear yet.",
                    fontSize = 13.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            studentTimetables.forEach { item ->
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// --- TEACHER TAB 1: NOTES, ASSIGNMENTS & MATERIALS MANAGEMENT ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MaterialsManagementSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf("Notes") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var materialsList by remember { mutableStateOf(listOf<SupabaseMaterial>()) }

    val categories = listOf("Notes", "Assignment", "Daily Activity")

    fun loadMaterials() {
        scope.launch {
            try {
                val list = withContext(Dispatchers.IO) {
                    SupabaseManager.client.postgrest["materials"]
                        .select()
                        .decodeList<SupabaseMaterial>()
                }
                materialsList = list
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        loadMaterials()
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Publish College Notes & Assignments", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Select Category:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("$selectedCategory Title / Subject") },
                leadingIcon = { Icon(Icons.Default.Title, contentDescription = "Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Details / Google Drive PDF Link / Instructions") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = "Details") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank()) {
                        Toast.makeText(context, "Details aur Title dono bharein!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    scope.launch {
                        try {
                            val newMaterial = SupabaseMaterial(
                                id = UUID.randomUUID().toString(),
                                category = selectedCategory,
                                title = title.trim(),
                                description = description.trim(),
                                timestamp = System.currentTimeMillis()
                            )
                            withContext(Dispatchers.IO) {
                                SupabaseManager.client.postgrest["materials"].insert(newMaterial)
                            }
                            Toast.makeText(context, "$selectedCategory Published to Supabase!", Toast.LENGTH_SHORT).show()
                            title = ""
                            description = ""
                            loadMaterials()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Publish Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Publish, contentDescription = "Publish")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Publish $selectedCategory", fontWeight = FontWeight.Bold)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text("Manage Published Notes & Assignments", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(modifier = Modifier.height(4.dp))

    if (materialsList.isEmpty()) {
        Text("Abhi koi material published nahi hai.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        materialsList.forEach { item ->
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = item.category.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(text = item.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = {
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    SupabaseManager.client.postgrest["materials"].delete { filter { eq("id", item.id) } }
                                }
                                Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show()
                                loadMaterials()
                            } catch (_: Exception) {
                                Toast.makeText(context, "Delete Error!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// --- USER REGISTRATION SECTION (RESTRICTED TO ADMIN ONLY) ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserRegistrationSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regRole by remember { mutableStateOf("student") }
    var regRollNo by remember { mutableStateOf("") }
    var regYear by remember { mutableStateOf("1st Year") }
    var isRegistering by remember { mutableStateOf(false) }

    var selectedRoleFilter by remember { mutableStateOf("All") }
    var selectedYearFilter by remember { mutableStateOf("All Years") }
    var userList by remember { mutableStateOf(listOf<SupabaseUser>()) }

    var userToEdit by remember { mutableStateOf<SupabaseUser?>(null) }
    var userToDelete by remember { mutableStateOf<SupabaseUser?>(null) }

    val yearsList = listOf("1st Year", "2nd Year", "3rd Year", "4th Year")

    fun loadUsers() {
        scope.launch {
            try {
                val list = withContext(Dispatchers.IO) {
                    SupabaseManager.client.postgrest["users"]
                        .select()
                        .decodeList<SupabaseUser>()
                }
                userList = list
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        loadUsers()
    }

    // EDIT USER DIALOG
    if (userToEdit != null) {
        val user = userToEdit!!
        var editName by remember { mutableStateOf(user.name) }
        var editRollNo by remember { mutableStateOf(user.rollNo) }
        var editYear by remember { mutableStateOf(user.year) }
        var editEmail by remember { mutableStateOf(user.email) }
        var isUpdating by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { userToEdit = null },
            title = { Text("Edit Registered User (Supabase DB)") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (user.role == "student") {
                        OutlinedTextField(
                            value = editRollNo,
                            onValueChange = { editRollNo = it.uppercase() },
                            label = { Text("Student Roll No") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Academic Year:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            yearsList.forEach { yr ->
                                FilterChip(
                                    selected = editYear == yr,
                                    onClick = { editYear = yr },
                                    label = { Text(yr, fontSize = 11.sp) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isUpdating = true
                        scope.launch {
                            try {
                                val updatedUser = SupabaseUser(
                                    uid = user.uid,
                                    name = editName.trim(),
                                    email = editEmail.trim().lowercase(),
                                    role = user.role,
                                    rollNo = if (user.role == "student") editRollNo.trim().uppercase() else "TEACHER",
                                    year = if (user.role == "student") editYear else "N/A"
                                )
                                withContext(Dispatchers.IO) {
                                    SupabaseManager.client.postgrest["users"].upsert(updatedUser)
                                }
                                isUpdating = false
                                userToEdit = null
                                Toast.makeText(context, "User profile updated in Supabase!", Toast.LENGTH_SHORT).show()
                                loadUsers()
                            } catch (e: Exception) {
                                isUpdating = false
                                Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = !isUpdating
                ) {
                    if (isUpdating) CircularProgressIndicator(modifier = Modifier.size(16.dp)) else Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToEdit = null }) { Text("Cancel") }
            }
        )
    }

    // DELETE USER DIALOG
    if (userToDelete != null) {
        val user = userToDelete!!
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Delete Registered User") },
            text = { Text("Are you sure you want to delete ${user.name} (${user.email}) from Supabase DB?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    SupabaseManager.client.postgrest["users"].delete { filter { eq("uid", user.uid) } }
                                }
                                userToDelete = null
                                Toast.makeText(context, "User deleted from Supabase DB!", Toast.LENGTH_SHORT).show()
                                loadUsers()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Delete failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete User")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) { Text("Cancel") }
            }
        )
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Register New Student / Teacher (Admin Only)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = regName,
                onValueChange = { regName = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("User Role: ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                RadioButton(selected = regRole == "student", onClick = { regRole = "student" })
                Text("Student", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(12.dp))
                RadioButton(selected = regRole == "teacher", onClick = { regRole = "teacher" })
                Text("Teacher", fontSize = 14.sp)
            }

            if (regRole == "student") {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Select Academic Year:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    yearsList.forEach { yr ->
                        FilterChip(
                            selected = regYear == yr,
                            onClick = { regYear = yr },
                            label = { Text(yr, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = regRollNo,
                    onValueChange = { regRollNo = it.uppercase() },
                    label = { Text("Student Roll No (e.g. 22CS01)") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = "RollNo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = regEmail,
                onValueChange = { regEmail = it },
                label = { Text("Email ID") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = regPassword,
                onValueChange = { regPassword = it },
                label = { Text("Password (Min 6 chars)") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (isRegistering) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        if (regName.isBlank() || regEmail.isBlank() || regPassword.isBlank()) {
                            Toast.makeText(context, "Name, Email aur Password sabhi bharein!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (regRole == "student" && regRollNo.isBlank()) {
                            Toast.makeText(context, "Student Roll No likhein!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isRegistering = true

                        createNewUserByTeacher(
                            context = context,
                            email = regEmail,
                            pass = regPassword,
                            name = regName,
                            role = regRole,
                            rollNo = regRollNo,
                            year = regYear,
                            onSuccess = {
                                isRegistering = false
                                Toast.makeText(context, "New $regRole Registered Successfully in Supabase!", Toast.LENGTH_SHORT).show()
                                regName = ""
                                regEmail = ""
                                regPassword = ""
                                regRollNo = ""
                                loadUsers()
                            },
                            onError = { err ->
                                isRegistering = false
                                Toast.makeText(context, "Registration Error: $err", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Register")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Register $regRole", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text("Registered Users List (${userList.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(modifier = Modifier.height(4.dp))

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listOf("All", "student", "teacher").forEach { roleFilter ->
            FilterChip(
                selected = selectedRoleFilter == roleFilter,
                onClick = { selectedRoleFilter = roleFilter },
                label = { Text(if (roleFilter == "All") "All Roles" else roleFilter.replaceFirstChar { it.uppercase() }, fontSize = 12.sp) }
            )
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        (listOf("All Years") + yearsList).forEach { yrFilter ->
            FilterChip(
                selected = selectedYearFilter == yrFilter,
                onClick = { selectedYearFilter = yrFilter },
                label = { Text(yrFilter, fontSize = 11.sp) }
            )
        }
    }

    val filteredUsers = userList.filter { user ->
        val roleMatch = selectedRoleFilter == "All" || user.role == selectedRoleFilter
        val yearMatch = selectedYearFilter == "All Years" || user.year == selectedYearFilter
        roleMatch && yearMatch
    }

    Spacer(modifier = Modifier.height(6.dp))

    if (filteredUsers.isEmpty()) {
        Text("No users found for selected filters.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        filteredUsers.forEach { user ->
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (user.role == "teacher") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = user.role.uppercase(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text("Email: ${user.email}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (user.role == "student") {
                            Text("Roll No: ${user.rollNo} | Year: ${user.year}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Row {
                        IconButton(onClick = { userToEdit = user }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit User", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { userToDelete = user }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete User", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

fun createNewUserByTeacher(
    context: android.content.Context,
    email: String,
    pass: String,
    name: String,
    role: String,
    rollNo: String,
    year: String = "1st Year",
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.Main)
    try {
        val appName = "RegistrationApp_${System.currentTimeMillis()}"
        val secondaryApp = FirebaseApp.initializeApp(context, FirebaseApp.getInstance().options, appName)
        val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)

        val cleanEmail = email.trim().lowercase()

        secondaryAuth.createUserWithEmailAndPassword(cleanEmail, pass.trim())
            .addOnSuccessListener { result ->
                val newUid = result.user!!.uid
                val newUser = SupabaseUser(
                    uid = newUid,
                    name = name.trim(),
                    email = cleanEmail,
                    role = role,
                    rollNo = if (role == "student") rollNo.trim().uppercase() else "TEACHER",
                    year = if (role == "student") year else "N/A"
                )

                scope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            SupabaseManager.client.postgrest["users"].upsert(newUser)
                        }
                        secondaryAuth.signOut()
                        secondaryApp.delete()
                        onSuccess()
                    } catch (e: Exception) {
                        secondaryAuth.signOut()
                        secondaryApp.delete()
                        onError(e.message ?: "Failed to save user in Supabase")
                    }
                }
            }
            .addOnFailureListener { e ->
                secondaryApp.delete()
                onError(e.message ?: "Failed to create user account in Firebase Auth")
            }
    } catch (e: Exception) {
        onError(e.message ?: "Error initializing user creation")
    }
}

// ==========================================
// 4. STUDENT DASHBOARD
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentDashboard(
    studentName: String,
    studentRollNo: String,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var attendanceList by remember { mutableStateOf(listOf<SupabaseAttendance>()) }
    var materialsList by remember { mutableStateOf(listOf<SupabaseMaterial>()) }
    var selectedFilter by remember { mutableStateOf("All") }
    var isLoadingAttendance by remember { mutableStateOf(true) }

    val filters = listOf("All", "Notes", "Assignment", "Daily Activity")

    LaunchedEffect(studentRollNo) {
        if (studentRollNo.isNotBlank()) {
            scope.launch {
                try {
                    val list = fetchAllAttendanceFromSupabase().filter { it.rollNo.equals(studentRollNo.trim(), ignoreCase = true) }
                    isLoadingAttendance = false
                    attendanceList = list
                } catch (_: Exception) {
                    isLoadingAttendance = false
                }
            }
        } else {
            isLoadingAttendance = false
        }

        scope.launch {
            try {
                val list = withContext(Dispatchers.IO) {
                    SupabaseManager.client.postgrest["materials"]
                        .select()
                        .decodeList<SupabaseMaterial>()
                }
                materialsList = list
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            PortalTopHeader(
                portalTitle = "SNITS Student Portal",
                userName = studentName,
                userRoleOrRoll = "Roll: $studentRollNo",
                onLogout = onLogout
            )

            // --- SECTION 1: MY ATTENDANCE SUMMARY ---
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("My Daily Attendance Summary", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val totalClasses = attendanceList.size
                    val presentCount = attendanceList.count { it.status == "Present" }
                    val absentCount = attendanceList.count { it.status == "Absent" }
                    val percentage = if (totalClasses > 0) (presentCount * 100) / totalClasses else 0

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (percentage >= 75) Color(0xFFF1F8E9) else Color(0xFFFFEBEE)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Overall Attendance Rate", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "$percentage%",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (percentage >= 75) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Classes: $totalClasses", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("Present: $presentCount", fontSize = 13.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                Text("Absent: $absentCount", fontSize = 13.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Detailed Attendance Records:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    if (isLoadingAttendance) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else if (attendanceList.isEmpty()) {
                        Text("No attendance records found yet.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        attendanceList.forEach { record ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(record.date, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Subject: ${record.subject}", fontSize = 13.sp)
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (record.status == "Present") Color(0xFF2E7D32) else Color(0xFFC62828)
                                ) {
                                    Text(
                                        text = record.status.uppercase(),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- SECTION 2: MY CLASS TIMETABLE ---
            Spacer(modifier = Modifier.height(16.dp))
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    StudentTimetableSection(studentRollNo = studentRollNo)
                }
            }

            // --- SECTION 3: VIEW NOTES, ASSIGNMENTS & ACTIVITIES ---
            Spacer(modifier = Modifier.height(16.dp))
            Text("College Notes & Assignments", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val filteredList = if (selectedFilter == "All") {
                materialsList
            } else {
                materialsList.filter { it.category == selectedFilter }
            }

            if (filteredList.isEmpty()) {
                Text("No $selectedFilter available currently.", modifier = Modifier.padding(8.dp), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                filteredList.forEach { item ->
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = item.category.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.description,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
