package com.eventzee.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.eventzee.R
import com.eventzee.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.auth_container, LoginFragment())
                .commit()
        }
    }

    fun navigateToRegister() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.auth_container, RegisterFragment())
            .addToBackStack(null)
            .commit()
    }

    fun navigateToLogin() {
        supportFragmentManager.popBackStack()
    }
}
