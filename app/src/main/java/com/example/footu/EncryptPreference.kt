package com.example.footu

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.footu.model.User
import com.example.footu.utils.PREF_ACCESS_TOKEN
import com.example.footu.utils.PREF_REFRESH_TOKEN
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class EncryptPreference @Inject constructor(
    @ApplicationContext context: Context,
) {
    private var pref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    init {
        pref = getSecureSharedPreferences(context)
        editor = pref?.edit()
    }

    private fun getSecureSharedPreferences(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            Companion.PREF_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun saveUser(user: User) {
        editor?.putString("id", user.id.toString())
        editor?.putString("username", user.username)
        editor?.putInt("admin", user.role ?: 0)
        editor?.putString("fullname", user.fullname)
        editor?.putString("phone", user.phone)
        editor?.apply()
    }

    fun saveToken(accessToken: String) {
        editor?.putString(PREF_ACCESS_TOKEN, accessToken)
        editor?.apply()
    }

    fun saveRefreshToken(token: String) {
        editor?.putString(PREF_REFRESH_TOKEN, token)
        editor?.apply()
    }

    fun getAccessToken(): String? = pref?.getString(PREF_ACCESS_TOKEN, "")

    fun getRefreshToken(): String? = pref?.getString(PREF_REFRESH_TOKEN, "")
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
        editor?.remove("fullname")
        editor?.remove("admin")
        editor?.remove("phone")
        editor?.remove(PREF_ACCESS_TOKEN)
        editor?.remove(PREF_REFRESH_TOKEN)
        editor?.apply()
    }

    companion object {
        private const val PREF_FILE_NAME = "secure_prefs"
    }
}
