package com.eventzee.ui.organizer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eventzee.R
import com.eventzee.data.model.Registration
import com.eventzee.databinding.ItemRegistrationBinding

class RegistrationsAdapter : ListAdapter<Registration, RegistrationsAdapter.VH>(DIFF) {

    inner class VH(private val binding: ItemRegistrationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reg: Registration) {
            binding.tvName.text = reg.studentName
            binding.tvUsn.text = reg.usn
            if (reg.paymentStatus == "PAID") {
                binding.tvStatus.text = "PAID"
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_paid)
                binding.tvStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.status_paid_text))
            } else {
                binding.tvStatus.text = "UNPAID"
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_unpaid)
                binding.tvStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.status_unpaid_text))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemRegistrationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Registration>() {
            override fun areItemsTheSame(a: Registration, b: Registration) = a.id == b.id
            override fun areContentsTheSame(a: Registration, b: Registration) = a == b
        }
    }
}
