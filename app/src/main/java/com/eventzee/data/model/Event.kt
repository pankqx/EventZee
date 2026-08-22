package com.eventzee.data.model

import com.google.firebase.firestore.DocumentId

data class Event(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val institution: String = "",
    val organizerName: String = "",
    val date: String = "",
    val time: String = "",
    val venue: String = "",
    val category: String = "Academic",
    val description: String = "",
    val maxTeamPlayers: Int = 1,
    val website: String = "",
    val coordinators: String = "",
    val upiId: String = "eventzee@upi",
    val registrationFee: Double = 299.0,
    val status: String = "Live", // "Live" or "Draft"
    val organizerUserId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
