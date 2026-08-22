package com.eventzee.data.repository

import androidx.lifecycle.LiveData
import com.eventzee.data.model.Event
import com.eventzee.data.model.Registration
import com.eventzee.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Firestore-backed data layer. Collections used:
 *   users          - one document per account (student or organizer)
 *   events         - one document per event
 *   registrations  - one document per student registration/ticket
 *
 * Note: this mirrors the college documentation's design - login is a direct
 * Firestore query against the users collection (no Firebase Authentication
 * yet), and passwords are stored as plain text. That's fine for a student
 * project, but should NOT go to production as-is (see README/roadmap).
 */
class AppRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("users")
    private val eventsRef = db.collection("events")
    private val registrationsRef = db.collection("registrations")

    // ---------- Users ----------

    suspend fun registerUser(user: User): String {
        val ref = usersRef.add(user).await()
        return ref.id
    }

    suspend fun login(email: String, password: String): User? {
        val snapshot = usersRef
            .whereEqualTo("email", email)
            .whereEqualTo("password", password)
            .limit(1)
            .get()
            .await()
        return snapshot.documents.firstOrNull()?.toObject(User::class.java)
    }

    fun getUserById(id: String): LiveData<User> =
        FirestoreDocumentLiveData(usersRef.document(id), User::class.java)

    suspend fun getUserByIdSync(id: String): User? =
        usersRef.document(id).get().await().toObject(User::class.java)

    suspend fun updateUser(user: User) {
        usersRef.document(user.id).set(user).await()
    }

    // ---------- Events ----------

    fun getAllEvents(): LiveData<List<Event>> =
        FirestoreCollectionLiveData(eventsRef.orderBy("createdAt", Query.Direction.DESCENDING), Event::class.java)

    fun getEventsByOrganizer(uid: String): LiveData<List<Event>> =
        FirestoreCollectionLiveData(eventsRef.whereEqualTo("organizerUserId", uid), Event::class.java)

    suspend fun getEventById(id: String): Event? =
        eventsRef.document(id).get().await().toObject(Event::class.java)

    suspend fun createEvent(event: Event): String {
        val ref = eventsRef.add(event).await()
        return ref.id
    }

    suspend fun updateEvent(event: Event) {
        eventsRef.document(event.id).set(event).await()
    }

    // ---------- Registrations ----------

    suspend fun register(reg: Registration): String {
        val ref = registrationsRef.add(reg).await()
        return ref.id
    }

    suspend fun updateRegistration(reg: Registration) {
        registrationsRef.document(reg.id).set(reg).await()
    }

    fun getRegistrationsByUser(uid: String): LiveData<List<Registration>> =
        FirestoreCollectionLiveData(registrationsRef.whereEqualTo("userId", uid), Registration::class.java)

    fun getRegistrationsByEvent(eid: String): LiveData<List<Registration>> =
        FirestoreCollectionLiveData(registrationsRef.whereEqualTo("eventId", eid), Registration::class.java)

    suspend fun getRegistrationById(id: String): Registration? =
        registrationsRef.document(id).get().await().toObject(Registration::class.java)

    suspend fun getPaidCount(eid: String): Int =
        registrationsRef
            .whereEqualTo("eventId", eid)
            .whereEqualTo("paymentStatus", "PAID")
            .get()
            .await()
            .size()
}
