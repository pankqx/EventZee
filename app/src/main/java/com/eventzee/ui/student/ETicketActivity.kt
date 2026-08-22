package com.eventzee.ui.student

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.eventzee.databinding.ActivityEticketBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.launch

class ETicketActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEticketBinding
    private lateinit var viewModel: StudentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEticketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[StudentViewModel::class.java]
        val regId = intent.getStringExtra("registration_id") ?: ""

        binding.ivBack.setOnClickListener { finish() }

        lifecycleScope.launch {
            val reg = viewModel.repo.getRegistrationById(regId) ?: return@launch
            val event = viewModel.repo.getEventById(reg.eventId) ?: return@launch

            binding.tvTicketCode.text = reg.ticketCode
            binding.tvEventTitle.text = event.title
            binding.tvSubtitle.text = event.institution
            binding.tvDate.text = event.date
            binding.tvTime.text = event.time
            binding.tvVenue.text = event.venue

            // Generate QR code
            try {
                val qrBitmap = generateQR(reg.ticketCode, 400, 400)
                binding.ivQrCode.setImageBitmap(qrBitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun generateQR(text: String, width: Int, height: Int): Bitmap {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height)
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bmp
    }
}
