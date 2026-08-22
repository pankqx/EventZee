package com.eventzee.ui.organizer

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.eventzee.R
import com.eventzee.data.model.Event
import com.eventzee.databinding.ActivityCreateEventBinding
import com.eventzee.utils.SessionManager
import java.util.*

class CreateEventActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateEventBinding
    private lateinit var viewModel: OrganizerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[OrganizerViewModel::class.java]
        binding.ivBack.setOnClickListener { finish() }

        val categories = listOf("Academic", "Social", "Sports", "Cultural", "Technical")
        val adapter = ArrayAdapter(this, R.layout.spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        binding.etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                binding.etDate.setText("${month + 1}/$day/$year")
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.etTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                val amPm = if (hour >= 12) "PM" else "AM"
                val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
                binding.etTime.setText(String.format("%d:%02d %s", displayHour, minute, amPm))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
        }

        binding.btnCreateEvent.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val institution = binding.etInstitution.text.toString().trim()
            val organizerName = binding.etOrganizerName.text.toString().trim()
            val date = binding.etDate.text.toString().trim()
            val time = binding.etTime.text.toString().trim()
            val venue = binding.etVenue.text.toString().trim()
            val category = binding.spinnerCategory.selectedItem?.toString() ?: "Academic"
            val desc = binding.etDescription.text.toString().trim()
            val upiId = binding.etUpiId.text.toString().trim().ifEmpty { "eventzee@upi" }
            val session = SessionManager(this)

            if (title.isEmpty() || date.isEmpty() || time.isEmpty() || venue.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val event = Event(
                title = title,
                institution = institution,
                organizerName = organizerName,
                date = date,
                time = time,
                venue = venue,
                category = category,
                description = desc,
                upiId = upiId,
                status = "Live",
                organizerUserId = session.getUserId()
            )
            viewModel.createEvent(event)
        }

        viewModel.createEventResult.observe(this) { id ->
            if (id.isNotEmpty()) {
                Toast.makeText(this, "Event created successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
