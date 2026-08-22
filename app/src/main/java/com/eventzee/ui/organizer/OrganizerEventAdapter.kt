package com.eventzee.ui.organizer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eventzee.R
import com.eventzee.data.model.Event
import com.eventzee.databinding.ItemOrganizerEventBinding

class OrganizerEventAdapter(
    private val onViewRegistrations: (Event) -> Unit,
    private val onEditEvent: (Event) -> Unit
) : ListAdapter<Event, OrganizerEventAdapter.VH>(DIFF) {

    inner class VH(private val binding: ItemOrganizerEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.tvEventTitle.text = event.title
            binding.tvDateTime.text = "${event.date} • ${event.time}"

            if (event.status == "Live") {
                binding.tvStatus.text = "Live"
                binding.tvStatus.setBackgroundResource(R.drawable.badge_live)
                binding.tvStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
                binding.tvDot.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.status_paid_text))
                binding.btnViewRegistrations.visibility = View.VISIBLE
                binding.btnEditEvent.visibility = View.GONE
                binding.tvNoRegs.visibility = View.GONE
                binding.btnViewRegistrations.setOnClickListener { onViewRegistrations(event) }
            } else {
                binding.tvStatus.text = "Draft"
                binding.tvStatus.setBackgroundResource(R.drawable.badge_draft)
                binding.tvStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.text_secondary))
                binding.tvDot.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.text_secondary))
                binding.btnViewRegistrations.visibility = View.GONE
                binding.btnEditEvent.visibility = View.VISIBLE
                binding.tvNoRegs.visibility = View.VISIBLE
                binding.btnEditEvent.setOnClickListener { onEditEvent(event) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemOrganizerEventBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Event>() {
            override fun areItemsTheSame(a: Event, b: Event) = a.id == b.id
            override fun areContentsTheSame(a: Event, b: Event) = a == b
        }
    }
}
