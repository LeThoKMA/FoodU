package com.example.footu

import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.example.footu.dagger2.App
import com.example.footu.model.User
import javax.inject.Inject

class MyPreferencee @Inject constructor() {
    private var pref: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(App.app)
    private var editor: SharedPreferences.Editor? = null
    fun saveUser(user: User, password: String) {
        editor = pref?.edit()
        editor?.putString("id", user.id.toString())
        editor?.putString("username", user.username)
        editor?.putString("passwd", password)
        editor?.putInt("admin", user.role ?: 0)
        editor?.putString("fullname", user.fullname)
        editor?.apply()
    }

    fun getPasswd(): String {
        return pref?.getString("passwd", "").toString()
    }

    fun getUser(): User {
        var user = User()
        if (pref?.getString("id", "")?.isNotBlank() == true) {
            user = User(
                id = pref?.getString("id", "")!!.toInt(),
                username = pref?.getString("username", ""),
                role = pref?.getInt("admin", 0),
                fullname = pref?.getString("fullname", ""),
            )
        }

        return user
    }

    fun logout() {
        editor = pref?.edit()
        editor?.remove("id")
        editor?.remove("username")
        editor?.remove("passwd")
        editor?.remove("admin")
        editor?.apply()
    }
}
