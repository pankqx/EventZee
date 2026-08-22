package com.eventzee.ui.student

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.eventzee.databinding.FragmentProfileBinding
import com.eventzee.ui.auth.AuthActivity
import com.eventzee.utils.SessionManager

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: StudentViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[StudentViewModel::class.java]
        val session = SessionManager(requireContext())

        viewModel.getUserById(session.getUserId()).observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvName.text = user.name
                binding.tvUsn.text = if (user.usn.isNotEmpty()) "USN: ${user.usn}" else "USN: N/A"
                binding.tvDept.text = user.department.ifEmpty { "N/A" }
                binding.tvEmail.text = user.email
                binding.tvPhone.text = user.phone.ifEmpty { "Not set" }
            }
        }

        binding.rowEditProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Edit Profile - coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.rowChangePassword.setOnClickListener {
            Toast.makeText(requireContext(), "Change Password - coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            session.clearSession()
            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
