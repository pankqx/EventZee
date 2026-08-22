package com.eventzee.ui.organizer

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.eventzee.data.model.Registration
import com.eventzee.databinding.ActivityRegistrationsListBinding
import kotlinx.coroutines.launch

class RegistrationsListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationsListBinding
    private lateinit var viewModel: OrganizerViewModel
    private lateinit var adapter: RegistrationsAdapter
    private var allRegs = listOf<Registration>()
    private var eventId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[OrganizerViewModel::class.java]
        eventId = intent.getStringExtra("event_id") ?: ""

        binding.ivBack.setOnClickListener { finish() }

        adapter = RegistrationsAdapter()
        binding.rvRegistrations.layoutManager = LinearLayoutManager(this)
        binding.rvRegistrations.adapter = adapter

        lifecycleScope.launch {
            val event = viewModel.repo.getEventById(eventId)
            binding.tvEventTitle.text = event?.title ?: "Event"
            binding.tvEventSeries.text = event?.institution ?: ""
        }

        viewModel.getRegistrationsByEvent(eventId).observe(this) { regs ->
            allRegs = regs
            adapter.submitList(regs)
            binding.tvParticipants.text = regs.size.toString()
            val paid = regs.count { it.paymentStatus == "PAID" }
            binding.tvEarnings.text = "₹${paid * 200}" // demo calculation
            filterList()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterList() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filterList() {
        val query = binding.etSearch.text.toString().lowercase()
        val filtered = if (query.isEmpty()) allRegs
        else allRegs.filter { it.studentName.lowercase().contains(query) || it.usn.lowercase().contains(query) }
        adapter.submitList(filtered)
    }
}
