package com.eventzee.ui.student

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.eventzee.data.model.Event
import com.eventzee.databinding.FragmentEventsBinding
import com.eventzee.utils.SessionManager

class EventsFragment : Fragment() {
    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: StudentViewModel
    private lateinit var adapter: EventListAdapter
    private var allEvents = listOf<Event>()
    private var selectedCategory = "All Events"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[StudentViewModel::class.java]

        adapter = EventListAdapter { event ->
            val intent = Intent(requireContext(), EventDetailActivity::class.java)
            intent.putExtra("event_id", event.id)
            startActivity(intent)
        }
        binding.rvEvents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEvents.adapter = adapter

        val session = SessionManager(requireContext())
        binding.tvUserName.text = session.getUserName().ifEmpty { "User" }

        viewModel.allEvents.observe(viewLifecycleOwner) { events ->
            allEvents = events
            filterEvents()
        }

        setupCategoryChips()

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterEvents() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupCategoryChips() {
        val chips = listOf(binding.chipAll, binding.chipAcademic, binding.chipSocial, binding.chipSports)
        val cats = listOf("All Events", "Academic", "Social", "Sports")
        chips.forEachIndexed { i, chip ->
            chip.setOnClickListener {
                selectedCategory = cats[i]
                chips.forEach { c -> c.isSelected = false }
                chip.isSelected = true
                filterEvents()
            }
        }
        binding.chipAll.isSelected = true
    }

    private fun filterEvents() {
        val query = binding.etSearch.text.toString().lowercase()
        var filtered = allEvents
        if (selectedCategory != "All Events") {
            filtered = filtered.filter { it.category == selectedCategory }
        }
        if (query.isNotEmpty()) {
            filtered = filtered.filter {
                it.title.lowercase().contains(query) || it.venue.lowercase().contains(query) || it.date.lowercase().contains(query)
            }
        }
        adapter.submitList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
