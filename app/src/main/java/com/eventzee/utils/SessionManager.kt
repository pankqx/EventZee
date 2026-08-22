package com.eventzee.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("eventzee_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_ROLE = "user_role"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_LOGGED_IN = "logged_in"
    }

    fun saveSession(userId: String, role: String, name: String, email: String) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_ROLE, role)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putBoolean(KEY_LOGGED_IN, true)
            apply()
        }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)
    fun getUserId(): String = prefs.getString(KEY_USER_ID, "") ?: ""
    fun getUserRole(): String = prefs.getString(KEY_USER_ROLE, "") ?: ""
    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""
    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
