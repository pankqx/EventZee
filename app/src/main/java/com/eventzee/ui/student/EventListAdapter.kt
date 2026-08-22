package com.eventzee.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eventzee.data.model.Event
import com.eventzee.databinding.ItemEventBinding

class EventListAdapter(private val onView: (Event) -> Unit) :
    ListAdapter<Event, EventListAdapter.VH>(DIFF) {

    inner class VH(private val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.tvEventTitle.text = event.title
            binding.tvEventDate.text = "${event.date}, ${event.time}"
            binding.tvEventVenue.text = event.venue
            binding.btnView.setOnClickListener { onView(event) }
            binding.root.setOnClickListener { onView(event) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Event>() {
            override fun areItemsTheSame(a: Event, b: Event) = a.id == b.id
            override fun areContentsTheSame(a: Event, b: Event) = a == b
        }
    }
}
