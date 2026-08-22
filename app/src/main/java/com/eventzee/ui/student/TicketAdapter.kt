package com.eventzee.ui.student

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eventzee.R
import com.eventzee.data.model.Registration
import com.eventzee.data.repository.AppRepository
import com.eventzee.databinding.ItemTicketBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TicketAdapter(
    private val onViewTicket: (Registration) -> Unit,
    private val onCompletePayment: (Registration) -> Unit,
    private val getEvent: (String) -> AppRepository
) : ListAdapter<Registration, TicketAdapter.VH>(DIFF) {

    inner class VH(private val binding: ItemTicketBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reg: Registration) {
            val repo = getEvent(reg.eventId)
            CoroutineScope(Dispatchers.Main).launch {
                val event = withContext(Dispatchers.IO) { repo.getEventById(reg.eventId) }
                binding.tvEventTitle.text = event?.title ?: "Event"
                binding.tvDateTime.text = "${event?.date ?: ""} • ${event?.time ?: ""}"
                binding.tvVenue.text = event?.venue ?: ""
            }

            if (reg.paymentStatus == "PAID") {
                binding.tvStatus.text = "CONFIRMED"
                binding.tvStatus.setBackgroundResource(R.drawable.badge_confirmed)
                binding.tvStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.status_paid_text))
                binding.tvSeatInfo.text = reg.seatInfo
                binding.btnViewTicket.visibility = View.VISIBLE
                binding.btnCompletePayment.visibility = View.GONE
                binding.tvActionRequired.visibility = View.GONE
                binding.btnViewTicket.setOnClickListener { onViewTicket(reg) }
            } else {
                binding.tvStatus.text = "PENDING PAYMENT"
                binding.tvStatus.setBackgroundResource(R.drawable.badge_pending)
                binding.tvStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.status_pending_text))
                binding.tvSeatInfo.text = ""
                binding.tvActionRequired.visibility = View.VISIBLE
                binding.btnCompletePayment.visibility = View.VISIBLE
                binding.btnViewTicket.visibility = View.GONE
                binding.btnCompletePayment.setOnClickListener { onCompletePayment(reg) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemTicketBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Registration>() {
            override fun areItemsTheSame(a: Registration, b: Registration) = a.id == b.id
            override fun areContentsTheSame(a: Registration, b: Registration) = a == b
        }
    }
}
