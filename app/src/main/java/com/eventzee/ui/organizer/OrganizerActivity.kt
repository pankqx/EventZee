package com.eventzee.ui.organizer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.eventzee.databinding.ActivityOrganizerBinding
import com.eventzee.utils.SessionManager

class OrganizerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrganizerBinding
    private lateinit var viewModel: OrganizerViewModel
    private lateinit var adapter: OrganizerEventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrganizerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[OrganizerViewModel::class.java]
        val session = SessionManager(this)

        adapter = OrganizerEventAdapter(
            onViewRegistrations = { event ->
                val intent = Intent(this, RegistrationsListActivity::class.java)
                intent.putExtra("event_id", event.id)
                startActivity(intent)
            },
            onEditEvent = { event ->
                val intent = Intent(this, CreateEventActivity::class.java)
                intent.putExtra("event_id", event.id)
                startActivity(intent)
            }
        )
        binding.rvMyEvents.layoutManager = LinearLayoutManager(this)
        binding.rvMyEvents.adapter = adapter

        viewModel.getOrganizerEvents(session.getUserId()).observe(this) { events ->
            adapter.submitList(events)
        }

        binding.btnCreateEvent.setOnClickListener {
            startActivity(Intent(this, CreateEventActivity::class.java))
        }
    }
}
