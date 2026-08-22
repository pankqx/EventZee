package com.eventzee.ui.student

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.eventzee.R
import com.eventzee.data.model.Registration
import com.eventzee.databinding.ActivityRegistrationFormBinding
import com.eventzee.utils.SessionManager

class RegistrationFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationFormBinding
    private lateinit var viewModel: StudentViewModel
    private var eventId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[StudentViewModel::class.java]
        eventId = intent.getStringExtra("event_id") ?: ""

        binding.ivBack.setOnClickListener { finish() }

        val session = SessionManager(this)
        // Pre-fill from session
        binding.etName.setText(session.getUserName())
        binding.etEmail.setText(session.getUserEmail())

        val depts = listOf("Select Department", "Computer Science", "Information Science", "Electronics & Communication", "Mechanical", "Civil", "Biotechnology")
        val adapter = ArrayAdapter(this, R.layout.spinner_item, depts)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDept.adapter = adapter

        binding.btnProceed.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val usn = binding.etUsn.text.toString().trim()
            val dept = binding.spinnerDept.selectedItem?.toString() ?: ""
            val teamName = binding.etTeamName.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || usn.isEmpty() || dept == "Select Department") {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ticketCode = "EZ-${(1000..9999).random()}-X${(100..999).random()}"
            val reg = Registration(
                eventId = eventId,
                userId = session.getUserId(),
                studentName = name,
                studentEmail = email,
                studentPhone = phone,
                usn = usn,
                department = dept,
                teamName = teamName,
                paymentStatus = "PENDING",
                ticketCode = ticketCode,
                seatInfo = "General Admission"
            )
            viewModel.submitRegistration(reg)
        }

        viewModel.registerResult.observe(this) { regId ->
            if (regId.isNotEmpty()) {
                val intent = Intent(this, PaymentActivity::class.java)
                intent.putExtra("registration_id", regId)
                intent.putExtra("event_id", eventId)
                startActivity(intent)
                finish()
            }
        }

        viewModel.errorMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
