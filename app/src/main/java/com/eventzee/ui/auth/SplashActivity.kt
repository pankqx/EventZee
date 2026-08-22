package com.eventzee.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.eventzee.databinding.ActivitySplashBinding
import com.eventzee.utils.SessionManager
import com.eventzee.ui.organizer.OrganizerActivity
import com.eventzee.ui.student.StudentActivity

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            val session = SessionManager(this)
            if (session.isLoggedIn()) {
                if (session.getUserRole() == "organizer") {
                    startActivity(Intent(this, OrganizerActivity::class.java))
                } else {
                    startActivity(Intent(this, StudentActivity::class.java))
                }
            } else {
                startActivity(Intent(this, AuthActivity::class.java))
            }
            finish()
        }, 2000)
    }
}
