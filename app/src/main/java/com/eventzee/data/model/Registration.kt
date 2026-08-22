package com.eventzee.data.model

import com.google.firebase.firestore.DocumentId

data class Registration(
    @DocumentId
    val id: String = "",
    val eventId: String = "",
    val userId: String = "",
    val studentName: String = "",
    val studentEmail: String = "",
    val studentPhone: String = "",
    val usn: String = "",
    val department: String = "",
    val teamName: String = "",
    val paymentStatus: String = "PENDING", // PENDING, PAID
    val ticketCode: String = "",
    val seatInfo: String = "General Admission",
    val createdAt: Long = System.currentTimeMillis()
)
