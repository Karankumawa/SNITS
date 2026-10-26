package com.example.snits

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            // ROW 1: Header Brand Logo + Title on Left, Action Buttons Group on Right
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
                            Text(
                                text = "SN",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
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
                            text = "SNITS College Portal",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Action Buttons Group (Key Icon + Logout Button)
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

            // ROW 2: Welcome User Profile Banner (Surface pill container)
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
// 1. AUTH SCREEN (Login Only for Public Users)
// ==========================================
@Composable
fun AuthScreen(onLoginSuccess: (role: String, rollNo: String, name: String) -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

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

            Spacer(modifier = Modifier.height(24.dp))

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
                    Text(
                        text = "Portal Login",
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

                                        db.collection("users").document(uid).get()
                                            .addOnSuccessListener { doc ->
                                                if (doc.exists()) {
                                                    isLoading = false
                                                    val role = doc.getString("role") ?: "student"
                                                    val savedRoll = doc.getString("rollNo") ?: ""
                                                    val name = doc.getString("name") ?: "User"
                                                    onLoginSuccess(role, savedRoll, name)
                                                } else {
                                                    db.collection("users").whereEqualTo("email", cleanEmail).get()
                                                        .addOnSuccessListener { queryResult ->
                                                            isLoading = false
                                                            if (!queryResult.isEmpty) {
                                                                val foundDoc = queryResult.documents[0]
                                                                val role = foundDoc.getString("role") ?: "student"
                                                                val savedRoll = foundDoc.getString("rollNo") ?: ""
                                                                val name = foundDoc.getString("name") ?: "User"

                                                                val userMap = hashMapOf(
                                                                    "uid" to uid,
                                                                    "name" to name,
                                                                    "email" to cleanEmail,
                                                                    "rollNo" to savedRoll,
                                                                    "role" to role
                                                                )
                                                                db.collection("users").document(uid).set(userMap)

                                                                onLoginSuccess(role, savedRoll, name)
                                                            } else {
                                                                val defaultRole = if (cleanEmail.contains("teacher")) "teacher" else "student"
                                                                val defaultRoll = cleanEmail.substringBefore("@").uppercase()
                                                                val defaultName = cleanEmail.substringBefore("@").replace(".", " ")

                                                                val userMap = hashMapOf(
                                                                    "uid" to uid,
                                                                    "name" to defaultName,
                                                                    "email" to cleanEmail,
                                                                    "rollNo" to if (defaultRole == "student") defaultRoll else "TEACHER",
                                                                    "role" to defaultRole
                                                                )
                                                                db.collection("users").document(uid).set(userMap)
                                                                onLoginSuccess(defaultRole, defaultRoll, defaultName)
                                                            }
                                                        }
                                                        .addOnFailureListener {
                                                            isLoading = false
                                                            val defaultRole = if (cleanEmail.contains("teacher")) "teacher" else "student"
                                                            val defaultRoll = cleanEmail.substringBefore("@").uppercase()
                                                            val defaultName = cleanEmail.substringBefore("@").replace(".", " ")
                                                            onLoginSuccess(defaultRole, defaultRoll, defaultName)
                                                        }
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
                            Text("Login to Portal", fontWeight = FontWeight.Bold)
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
                        text = "New Student / Teacher registrations are managed by Admin/Teachers.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. TEACHER DASHBOARD
// ==========================================
@Composable
fun TeacherDashboard(teacherName: String, onLogout: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Class Attendance", "Notes & Materials", "Register User")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
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
            0 -> ClassAttendanceSection(db = db, context = context)
            1 -> MaterialsManagementSection(db = db, context = context)
            2 -> UserRegistrationSection(db = db, context = context)
        }
    }
}

data class StudentAttendanceState(
    val rollNo: String,
    val name: String,
    var isPresent: Boolean = true
)

// --- TEACHER TAB 0: BATCH CLASS ATTENDANCE SHEET ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClassAttendanceSection(db: FirebaseFirestore, context: android.content.Context) {
    val todayDate = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()) }

    var date by remember { mutableStateOf(todayDate) }
    var subject by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    var studentList by remember { mutableStateOf(listOf<StudentAttendanceState>()) }
    val attendanceStatusMap = remember { mutableStateMapOf<String, Boolean>() }

    var filterRoll by remember { mutableStateOf("") }
    var searchedRecords by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        db.collection("users")
            .whereEqualTo("role", "student")
            .addSnapshotListener { value, _ ->
                if (value != null) {
                    val list = mutableListOf<StudentAttendanceState>()
                    for (doc in value) {
                        val roll = doc.getString("rollNo") ?: ""
                        val name = doc.getString("name") ?: ""
                        if (roll.isNotBlank()) {
                            list.add(StudentAttendanceState(roll, name, true))
                            if (!attendanceStatusMap.containsKey(roll)) {
                                attendanceStatusMap[roll] = true
                            }
                        }
                    }
                    studentList = list.sortedBy { it.rollNo }
                }
            }
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
            Text("Select Date & Subject to mark attendance for all registered students.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (DD-MM-YYYY)") },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Date") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject Name (e.g. DBMS, Java)") },
                leadingIcon = { Icon(Icons.Default.Book, contentDescription = "Subject") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        studentList.forEach { attendanceStatusMap[it.rollNo] = true }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Mark All Present", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = {
                        studentList.forEach { attendanceStatusMap[it.rollNo] = false }
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

            if (studentList.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No registered students found. Register students from the 'Register User' tab.",
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
                    Text("Registered Students List", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                        Text("${studentList.size} Students", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                studentList.forEach { student ->
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
                                Text(
                                    text = "${student.rollNo} - ${student.name}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
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
                            var savedCount = 0
                            val totalToSave = studentList.size

                            studentList.forEach { student ->
                                val status = if (attendanceStatusMap[student.rollNo] == true) "Present" else "Absent"
                                val docId = "${student.rollNo}_${date.trim()}_${subject.trim()}"
                                val map = hashMapOf(
                                    "rollNo" to student.rollNo,
                                    "date" to date.trim(),
                                    "subject" to subject.trim(),
                                    "status" to status,
                                    "timestamp" to System.currentTimeMillis()
                                )
                                db.collection("attendance").document(docId).set(map)
                                    .addOnSuccessListener {
                                        savedCount++
                                        if (savedCount >= totalToSave) {
                                            isSaving = false
                                            Toast.makeText(context, "Attendance Saved for $savedCount Students!", Toast.LENGTH_LONG).show()
                                        }
                                    }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Attendance for All Students", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("2. Search & Edit Past Attendance", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = filterRoll,
                    onValueChange = { filterRoll = it.uppercase() },
                    label = { Text("Search by Roll No (e.g. 22CS01)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (filterRoll.isBlank()) {
                            Toast.makeText(context, "Roll No likhein!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSearching = true
                        db.collection("attendance")
                            .whereEqualTo("rollNo", filterRoll.trim())
                            .get()
                            .addOnSuccessListener { result ->
                                isSearching = false
                                val list = mutableListOf<Map<String, Any>>()
                                for (doc in result) {
                                    val item = mutableMapOf<String, Any>()
                                    item["id"] = doc.id
                                    item["rollNo"] = doc.getString("rollNo") ?: ""
                                    item["date"] = doc.getString("date") ?: ""
                                    item["subject"] = doc.getString("subject") ?: ""
                                    item["status"] = doc.getString("status") ?: ""
                                    list.add(item)
                                }
                                searchedRecords = list
                            }
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Search")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isSearching) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (searchedRecords.isEmpty()) {
                Text("Search box me Roll No daal kar purani attendance dekhein aur modify karein.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                searchedRecords.forEach { record ->
                    val id = record["id"].toString()
                    val recRoll = record["rollNo"].toString()
                    val recDate = record["date"].toString()
                    val recSub = record["subject"].toString()
                    val recStatus = record["status"].toString()

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
                                Text("$recRoll | $recDate", fontWeight = FontWeight.Bold)
                                Text("Subject: $recSub", fontSize = 13.sp)
                                Text(
                                    text = "Status: $recStatus",
                                    fontWeight = FontWeight.Bold,
                                    color = if (recStatus == "Present") Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }
                            Row {
                                TextButton(onClick = {
                                    val newStatus = if (recStatus == "Present") "Absent" else "Present"
                                    db.collection("attendance").document(id).update("status", newStatus)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Updated to $newStatus!", Toast.LENGTH_SHORT).show()
                                            searchedRecords = searchedRecords.map {
                                                if (it["id"] == id) it.toMutableMap().apply { put("status", newStatus) } else it
                                            }
                                        }
                                }) {
                                    Text("Change")
                                }
                                IconButton(onClick = {
                                    db.collection("attendance").document(id).delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show()
                                            searchedRecords = searchedRecords.filter { it["id"] != id }
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

// --- TEACHER TAB 1: NOTES, TIMETABLE & MATERIALS MANAGEMENT ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MaterialsManagementSection(db: FirebaseFirestore, context: android.content.Context) {
    var selectedCategory by remember { mutableStateOf("Notes") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var materialsList by remember { mutableStateOf(listOf<Map<String, String>>()) }

    val categories = listOf("Notes", "Timetable", "Assignment", "Daily Activity")

    LaunchedEffect(Unit) {
        db.collection("materials").addSnapshotListener { value, _ ->
            if (value != null) {
                val list = mutableListOf<Map<String, String>>()
                for (doc in value) {
                    list.add(
                        mapOf(
                            "id" to doc.id,
                            "category" to (doc.getString("category") ?: ""),
                            "title" to (doc.getString("title") ?: ""),
                            "description" to (doc.getString("description") ?: "")
                        )
                    )
                }
                materialsList = list
            }
        }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Publish College Update / Notes / Timetable", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                label = { Text("Details / Google Drive PDF Link / Timings") },
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
                    val data = hashMapOf(
                        "category" to selectedCategory,
                        "title" to title.trim(),
                        "description" to description.trim(),
                        "timestamp" to System.currentTimeMillis()
                    )
                    db.collection("materials").add(data)
                        .addOnSuccessListener {
                            Toast.makeText(context, "$selectedCategory Published!", Toast.LENGTH_SHORT).show()
                            title = ""
                            description = ""
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

    Text("Manage Published Content", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(modifier = Modifier.height(4.dp))

    if (materialsList.isEmpty()) {
        Text("Abhi koi material published nahi hai.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        materialsList.forEach { item ->
            val docId = item["id"] ?: ""
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
                                text = (item["category"] ?: "").uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item["title"] ?: "",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(text = item["description"] ?: "", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = {
                        db.collection("materials").document(docId).delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show()
                            }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// Data class for Registered User
data class RegisteredUserData(
    val uid: String,
    val name: String,
    val email: String,
    val role: String,
    val rollNo: String
)

// --- TEACHER TAB 2: REGISTER NEW STUDENT / TEACHER ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserRegistrationSection(db: FirebaseFirestore, context: android.content.Context) {
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regRole by remember { mutableStateOf("student") }
    var regRollNo by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }

    var selectedFilter by remember { mutableStateOf("All") }
    var userList by remember { mutableStateOf(listOf<RegisteredUserData>()) }

    var userToEdit by remember { mutableStateOf<RegisteredUserData?>(null) }
    var userToDelete by remember { mutableStateOf<RegisteredUserData?>(null) }

    LaunchedEffect(Unit) {
        db.collection("users").addSnapshotListener { value, _ ->
            if (value != null) {
                val list = mutableListOf<RegisteredUserData>()
                for (doc in value) {
                    list.add(
                        RegisteredUserData(
                            uid = doc.id,
                            name = doc.getString("name") ?: "",
                            email = doc.getString("email") ?: "",
                            role = doc.getString("role") ?: "",
                            rollNo = doc.getString("rollNo") ?: ""
                        )
                    )
                }
                userList = list
            }
        }
    }

    // EDIT USER DIALOG
    if (userToEdit != null) {
        val user = userToEdit!!
        var editName by remember { mutableStateOf(user.name) }
        var editRollNo by remember { mutableStateOf(user.rollNo) }
        var editEmail by remember { mutableStateOf(user.email) }
        var isUpdating by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { userToEdit = null },
            title = { Text("Edit Registered User") },
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
                        val updateMap = hashMapOf<String, Any>(
                            "name" to editName.trim(),
                            "email" to editEmail.trim().lowercase(),
                            "rollNo" to if (user.role == "student") editRollNo.trim().uppercase() else "TEACHER"
                        )
                        db.collection("users").document(user.uid).update(updateMap)
                            .addOnSuccessListener {
                                isUpdating = false
                                userToEdit = null
                                Toast.makeText(context, "User profile updated in Firestore!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                isUpdating = false
                                Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
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
            text = { Text("Are you sure you want to delete ${user.name} (${user.email}) from Firestore?") },
            confirmButton = {
                Button(
                    onClick = {
                        db.collection("users").document(user.uid).delete()
                            .addOnSuccessListener {
                                userToDelete = null
                                Toast.makeText(context, "User deleted from Firestore!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Delete failed: ${e.message}", Toast.LENGTH_LONG).show()
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
            Text("Register New Student / Teacher", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                            onSuccess = {
                                isRegistering = false
                                Toast.makeText(context, "New $regRole Registered Successfully!", Toast.LENGTH_SHORT).show()
                                regName = ""
                                regEmail = ""
                                regPassword = ""
                                regRollNo = ""
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
                selected = selectedFilter == roleFilter,
                onClick = { selectedFilter = roleFilter },
                label = { Text(if (roleFilter == "All") "All Users" else roleFilter.replaceFirstChar { it.uppercase() }, fontSize = 12.sp) }
            )
        }
    }

    val filteredUsers = if (selectedFilter == "All") {
        userList
    } else {
        userList.filter { it.role == selectedFilter }
    }

    if (filteredUsers.isEmpty()) {
        Text("No users found.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            Spacer(modifier = Modifier.width(8.dp))
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
                            Text("Roll No: ${user.rollNo}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
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
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    try {
        val appName = "RegistrationApp_${System.currentTimeMillis()}"
        val secondaryApp = FirebaseApp.initializeApp(context, FirebaseApp.getInstance().options, appName)
        val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)

        val cleanEmail = email.trim().lowercase()

        secondaryAuth.createUserWithEmailAndPassword(cleanEmail, pass.trim())
            .addOnSuccessListener { result ->
                val newUid = result.user!!.uid
                val userMap = hashMapOf(
                    "uid" to newUid,
                    "name" to name.trim(),
                    "email" to cleanEmail,
                    "rollNo" to if (role == "student") rollNo.trim().uppercase() else "TEACHER",
                    "role" to role
                )
                db.collection("users").document(newUid).set(userMap)
                    .addOnSuccessListener {
                        secondaryAuth.signOut()
                        secondaryApp.delete()
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        secondaryAuth.signOut()
                        secondaryApp.delete()
                        onError(e.message ?: "Failed to save user in database")
                    }
            }
            .addOnFailureListener { e ->
                secondaryApp.delete()
                onError(e.message ?: "Failed to create user account")
            }
    } catch (e: Exception) {
        onError(e.message ?: "Error initializing user creation")
    }
}

// ==========================================
// 3. STUDENT DASHBOARD
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentDashboard(
    studentName: String,
    studentRollNo: String,
    onLogout: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    var attendanceList by remember { mutableStateOf(listOf<Map<String, String>>()) }
    var materialsList by remember { mutableStateOf(listOf<Map<String, String>>()) }
    var selectedFilter by remember { mutableStateOf("All") }
    var isLoadingAttendance by remember { mutableStateOf(true) }

    val filters = listOf("All", "Notes", "Timetable", "Assignment", "Daily Activity")

    LaunchedEffect(studentRollNo) {
        if (studentRollNo.isNotBlank()) {
            db.collection("attendance")
                .whereEqualTo("rollNo", studentRollNo.trim())
                .addSnapshotListener { value, _ ->
                    isLoadingAttendance = false
                    if (value != null) {
                        val list = mutableListOf<Map<String, String>>()
                        for (doc in value) {
                            list.add(
                                mapOf(
                                    "date" to (doc.getString("date") ?: ""),
                                    "subject" to (doc.getString("subject") ?: ""),
                                    "status" to (doc.getString("status") ?: "")
                                )
                            )
                        }
                        attendanceList = list
                    }
                }
        } else {
            isLoadingAttendance = false
        }

        db.collection("materials").addSnapshotListener { value, _ ->
            if (value != null) {
                val list = mutableListOf<Map<String, String>>()
                for (doc in value) {
                    list.add(
                        mapOf(
                            "category" to (doc.getString("category") ?: ""),
                            "title" to (doc.getString("title") ?: ""),
                            "description" to (doc.getString("description") ?: "")
                        )
                    )
                }
                materialsList = list
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
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
                val presentCount = attendanceList.count { it["status"] == "Present" }
                val absentCount = attendanceList.count { it["status"] == "Absent" }
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
                        val status = record["status"] ?: ""
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
                                Text(record["date"] ?: "", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Subject: ${record["subject"]}", fontSize = 13.sp)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (status == "Present") Color(0xFF2E7D32) else Color(0xFFC62828)
                            ) {
                                Text(
                                    text = status.uppercase(),
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

        // --- SECTION 2: VIEW NOTES, TIMETABLE, ASSIGNMENTS & ACTIVITIES ---
        Spacer(modifier = Modifier.height(16.dp))
        Text("College Notes, Timetable & Updates", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
            materialsList.filter { it["category"] == selectedFilter }
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
                                text = (item["category"] ?: "").uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item["title"] ?: "",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item["description"] ?: "",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
