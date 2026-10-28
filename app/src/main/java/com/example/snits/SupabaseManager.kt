package com.example.snits

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseUser(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "",
    @SerialName("rollNo") val rollNo: String = "",
    val year: String = "1st Year"
)

@Serializable
data class SupabaseAttendance(
    @SerialName("docId") val docId: String = "",
    @SerialName("rollNo") val rollNo: String = "",
    val date: String = "",
    val subject: String = "",
    val status: String = "",
    val year: String = "1st Year",
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class SupabaseMaterial(
    val id: String = java.util.UUID.randomUUID().toString(),
    val category: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class SupabaseTimetable(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    @SerialName("targetYear") val targetYear: String = "All",
    @SerialName("targetRole") val targetRole: String = "all",
    @SerialName("rollNo") val rollNo: String = "ALL",
    val timestamp: Long = System.currentTimeMillis()
)

object SupabaseManager {
    const val URL = "https://uikexanjyiaarccbdjtq.supabase.co"
    const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVpa2V4YW5qeWlhYXJjY2JkanRxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTI5ODcxNjQsImV4cCI6MjEwODU2MzE2NH0.rfDIuazL1xGaq6nt-EBynqAgjWY-tka1-tQNwWS4FvU"

    val client = createSupabaseClient(
        supabaseUrl = URL,
        supabaseKey = ANON_KEY
    ) {
        install(Postgrest)
    }
}
