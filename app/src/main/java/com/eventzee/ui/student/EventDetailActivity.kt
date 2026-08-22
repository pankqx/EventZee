package com.eventzee.ui.student

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.eventzee.databinding.ActivityEventDetailBinding
import kotlinx.coroutines.launch

class EventDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEventDetailBinding
    private lateinit var viewModel: StudentViewModel
    private var eventId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[StudentViewModel::class.java]
        eventId = intent.getStringExtra("event_id") ?: ""

        binding.ivBack.setOnClickListener { finish() }

        lifecycleScope.launch {
            val event = viewModel.repo.getEventById(eventId) ?: return@launch
            binding.tvEventTitle.text = event.title
            binding.tvInstitution.text = event.institution
            binding.tvDate.text = event.date
            binding.tvTime.text = event.time
            binding.tvType.text = if (event.maxTeamPlayers > 1) "Team Event" else "Individual Event"
            binding.tvCategory.text = event.category
            binding.tvDescription.text = event.description.ifEmpty { "No description available." }
            binding.tvVenue.text = event.venue
            binding.tvVenueSub.text = "Main Campus"
            binding.tvCoordinators.text = event.coordinators.ifEmpty { "Contact organizer for details" }
        }

        binding.btnRegister.setOnClickListener {
            val intent = Intent(this, RegistrationFormActivity::class.java)
            intent.putExtra("event_id", eventId)
            startActivity(intent)
        }
    }
}
