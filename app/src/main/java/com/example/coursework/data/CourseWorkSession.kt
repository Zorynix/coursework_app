package com.example.coursework.data

import android.content.Context
import android.content.SharedPreferences

class CourseWorkSession(val context: Context) {
    val sharedPres: SharedPreferences =
        context.getSharedPreferences("coursework", Context.MODE_PRIVATE)

    fun storeToken(token: String) {
        sharedPres.edit().putString("token", token).apply()
    }

    fun getToken(): String? {
        sharedPres.getString("token", null)?.let {
            return it
        }
        return null
    }
}
