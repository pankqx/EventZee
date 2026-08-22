package com.eventzee.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.eventzee.data.model.User
import com.eventzee.data.repository.AppRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = AppRepository()

    val loginResult = MutableLiveData<User?>()
    val registerResult = MutableLiveData<String>()
    val errorMessage = MutableLiveData<String>()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val user = repo.login(email, password)
            if (user != null) loginResult.postValue(user)
            else errorMessage.postValue("Invalid email or password")
        }
    }

    fun register(name: String, email: String, password: String, role: String) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty()) {
            errorMessage.postValue("Please fill all fields")
            return
        }
        viewModelScope.launch {
            val id = repo.registerUser(User(name = name, email = email, password = password, role = role))
            registerResult.postValue(id)
        }
    }
}
