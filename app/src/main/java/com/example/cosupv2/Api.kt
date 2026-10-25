package com.example.cosupv2

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Talks to the COSUP website so app users and guests are saved in the same Azure SQL database. */
object Api {

    /** onDone(role, error): role is set on success, error on failure. If the website address is not set, it succeeds without contacting it. */
    fun exchange(
        idToken: String,
        firstName: String?,
        lastName: String?,
        phone: String?,
        onDone: (role: String?, error: String?) -> Unit
    ) {
        if (!Config.apiReady) {
            onDone(null, null)
            return
        }
        Thread {
            var role: String? = null
            var error: String? = null
            try {
                val conn = URL(Config.BASE_URL.trimEnd('/') + "/Auth/FirebaseLogin").openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 15000
                conn.readTimeout = 25000
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                val body = JSONObject()
                body.put("idToken", idToken)
                if (!firstName.isNullOrBlank()) body.put("firstName", firstName)
                if (!lastName.isNullOrBlank()) body.put("lastName", lastName)
                if (!phone.isNullOrBlank()) body.put("phone", phone)
                conn.outputStream.use { it.write(body.toString().toByteArray()) }

                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
                val json = try { JSONObject(text) } catch (e: Exception) { JSONObject() }
                if (code in 200..299) {
                    role = json.optString("role", "user")
                } else {
                    error = json.optString("error", "The COSUP server could not sign you in.")
                }
            } catch (e: Exception) {
                error = "Could not reach the COSUP server. Check your internet connection."
            }
            Handler(Looper.getMainLooper()).post { onDone(role, error) }
        }.start()
    }
}

/** Remembers who is signed in on this phone. */
object SessionStore {
    private fun prefs(c: Context) = c.getSharedPreferences("cosup_session", Context.MODE_PRIVATE)

    fun save(c: Context, name: String, email: String?, role: String) {
        prefs(c).edit().putString("name", name).putString("email", email).putString("role", role).apply()
    }

    fun clear(c: Context) {
        prefs(c).edit().clear().apply()
    }

    fun name(c: Context): String? = prefs(c).getString("name", null)
    fun email(c: Context): String? = prefs(c).getString("email", null)
    fun role(c: Context): String = prefs(c).getString("role", "user") ?: "user"
}
