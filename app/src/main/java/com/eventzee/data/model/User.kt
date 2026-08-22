package com.eventzee.data.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val role: String = "", // "student" or "organizer"
    val usn: String = "",
    val department: String = "",
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
