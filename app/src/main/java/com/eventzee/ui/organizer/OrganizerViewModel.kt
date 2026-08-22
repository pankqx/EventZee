package com.eventzee.ui.organizer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.eventzee.data.model.Event
import com.eventzee.data.model.Registration
import com.eventzee.data.repository.AppRepository
import kotlinx.coroutines.launch

class OrganizerViewModel(application: Application) : AndroidViewModel(application) {
    val repo = AppRepository()

    val createEventResult = MutableLiveData<String>()
    val errorMessage = MutableLiveData<String>()

    fun getOrganizerEvents(uid: String): LiveData<List<Event>> = repo.getEventsByOrganizer(uid)
    fun getRegistrationsByEvent(eid: String): LiveData<List<Registration>> = repo.getRegistrationsByEvent(eid)

    fun createEvent(event: Event) {
        viewModelScope.launch {
            val id = repo.createEvent(event)
            createEventResult.postValue(id)
        }
    }
}
