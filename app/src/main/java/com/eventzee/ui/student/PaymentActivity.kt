package com.eventzee.ui.student

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.eventzee.databinding.ActivityPaymentBinding
import kotlinx.coroutines.launch

class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private lateinit var viewModel: StudentViewModel
    private var registrationId: String = ""
    private var eventId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[StudentViewModel::class.java]
        registrationId = intent.getStringExtra("registration_id") ?: ""
        eventId = intent.getStringExtra("event_id") ?: ""

        binding.ivBack.setOnClickListener { finish() }

        lifecycleScope.launch {
            val event = viewModel.repo.getEventById(eventId)
            event?.let {
                binding.tvEventTitle.text = it.title
                binding.tvVenue.text = it.venue
                binding.tvAmount.text = "₹${it.registrationFee.toInt()}.00"
                binding.tvUpiId.text = it.upiId
            }
        }

        binding.tvCopyUpi.setOnClickListener {
            val clipboard = getSystemService(ClipboardManager::class.java)
            clipboard.setPrimaryClip(ClipData.newPlainText("UPI ID", binding.tvUpiId.text))
            Toast.makeText(this, "UPI ID copied!", Toast.LENGTH_SHORT).show()
        }

        binding.btnSubmitPayment.setOnClickListener {
            viewModel.updatePaymentStatus(registrationId, "PAID")
            Toast.makeText(this, "Payment submitted successfully!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, StudentActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            // Navigate to My Tickets
            intent.putExtra("open_tab", "tickets")
            startActivity(intent)
            finish()
        }
    }
}
