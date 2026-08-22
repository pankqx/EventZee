package com.eventzee.ui.student

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.eventzee.data.model.Event
import com.eventzee.data.model.Registration
import com.eventzee.data.model.User
import com.eventzee.data.repository.AppRepository
import kotlinx.coroutines.launch

class StudentViewModel(application: Application) : AndroidViewModel(application) {
    val repo = AppRepository()

    val allEvents: LiveData<List<Event>> = repo.getAllEvents()
    val registerResult = MutableLiveData<String>()
    val errorMessage = MutableLiveData<String>()

    fun getTicketsByUser(uid: String): LiveData<List<Registration>> = repo.getRegistrationsByUser(uid)
    fun getUserById(id: String): LiveData<User> = repo.getUserById(id)

    fun submitRegistration(reg: Registration) {
        viewModelScope.launch {
            val id = repo.register(reg)
            registerResult.postValue(id)
        }
    }

    fun updatePaymentStatus(regId: String, status: String) {
        viewModelScope.launch {
            val reg = repo.getRegistrationById(regId) ?: return@launch
            repo.updateRegistration(reg.copy(paymentStatus = status))
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch { repo.updateUser(user) }
    }
}
