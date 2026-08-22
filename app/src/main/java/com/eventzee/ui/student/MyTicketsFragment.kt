package com.eventzee.ui.student

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.eventzee.databinding.FragmentMyTicketsBinding
import com.eventzee.utils.SessionManager

class MyTicketsFragment : Fragment() {
    private var _binding: FragmentMyTicketsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: StudentViewModel
    private lateinit var adapter: TicketAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyTicketsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[StudentViewModel::class.java]
        val session = SessionManager(requireContext())

        adapter = TicketAdapter(
            onViewTicket = { reg ->
                val intent = Intent(requireContext(), ETicketActivity::class.java)
                intent.putExtra("registration_id", reg.id)
                startActivity(intent)
            },
            onCompletePayment = { reg ->
                val intent = Intent(requireContext(), PaymentActivity::class.java)
                intent.putExtra("registration_id", reg.id)
                intent.putExtra("event_id", reg.eventId)
                startActivity(intent)
            },
            getEvent = { eventId ->
                // We'll load event name from DB - pass callback via ViewModel
                viewModel.repo
            }
        )

        binding.rvTickets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTickets.adapter = adapter

        viewModel.getTicketsByUser(session.getUserId()).observe(viewLifecycleOwner) { regs ->
            adapter.submitList(regs)
            binding.tvEmpty.visibility = if (regs.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
