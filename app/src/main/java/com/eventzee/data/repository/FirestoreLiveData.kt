package com.eventzee.data.repository

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

/**
 * LiveData that stays in sync with a Firestore collection query in real time.
 * The snapshot listener is attached while there are active observers and
 * detached automatically when there are none (mirrors what Room's LiveData did).
 */
class FirestoreCollectionLiveData<T : Any>(
    private val query: Query,
    private val clazz: Class<T>
) : LiveData<List<T>>() {
    private var registration: ListenerRegistration? = null

    override fun onActive() {
        registration = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            value = snapshot.toObjects(clazz)
        }
    }

    override fun onInactive() {
        registration?.remove()
        registration = null
    }
}

/** LiveData that stays in sync with a single Firestore document in real time. */
class FirestoreDocumentLiveData<T : Any>(
    private val docRef: DocumentReference,
    private val clazz: Class<T>
) : LiveData<T>() {
    private var registration: ListenerRegistration? = null

    override fun onActive() {
        registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            value = snapshot.toObject(clazz)
        }
    }

    override fun onInactive() {
        registration?.remove()
        registration = null
    }
}
