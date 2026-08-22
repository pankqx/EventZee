package com.eventzee.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.eventzee.R
import com.eventzee.databinding.FragmentLoginBinding
import com.eventzee.ui.organizer.OrganizerActivity
import com.eventzee.ui.student.StudentActivity
import com.eventzee.utils.SessionManager

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        val roles = listOf("Select Role", "Student", "Organizer")
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRole.adapter = adapter

        binding.tvRegister.setOnClickListener {
            (activity as? AuthActivity)?.navigateToRegister()
        }

        var passwordVisible = false
        binding.ivTogglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            if (passwordVisible) {
                binding.etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT
                binding.ivTogglePassword.setImageResource(R.drawable.ic_eye_on)
            } else {
                binding.etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.ivTogglePassword.setImageResource(R.drawable.ic_eye_off)
            }
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val selectedRole = binding.spinnerRole.selectedItem.toString()
            if (selectedRole == "Select Role") {
                Toast.makeText(requireContext(), "Please select a role", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.login(email, password)
        }

        viewModel.loginResult.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                SessionManager(requireContext()).saveSession(user.id, user.role, user.name, user.email)
                val intent = if (user.role == "organizer") {
                    Intent(requireContext(), OrganizerActivity::class.java)
                } else {
                    Intent(requireContext(), StudentActivity::class.java)
                }
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
