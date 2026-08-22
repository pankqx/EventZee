package com.eventzee.ui.student

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.eventzee.R
import com.eventzee.databinding.ActivityStudentBinding

class StudentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            loadFragment(EventsFragment())
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_events -> { loadFragment(EventsFragment()); true }
                R.id.nav_tickets -> { loadFragment(MyTicketsFragment()); true }
                R.id.nav_profile -> { loadFragment(ProfileFragment()); true }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.student_container, fragment)
            .commit()
    }
}
