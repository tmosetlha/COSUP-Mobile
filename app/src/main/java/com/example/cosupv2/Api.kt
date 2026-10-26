package com.example.cosupv2

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Talks to the COSUP website so the app and website share one Azure SQL database. */
object Api {

    private val main = Handler(Looper.getMainLooper())

    private fun call(path: String, body: JSONObject?): Pair<Int, String> {
        val conn = URL(Config.BASE_URL.trimEnd('/') + path).openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 25000
        if (body != null) {
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.outputStream.use { it.write(body.toString().toByteArray()) }
        } else {
            conn.requestMethod = "GET"
        }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
        return Pair(code, text)
    }

    private fun json(text: String): JSONObject = try { JSONObject(text) } catch (e: Exception) { JSONObject() }

    /** GET a path from the website. onDone gets the response text, or null if it could not be loaded. */
    fun get(path: String, onDone: (String?) -> Unit) {
        if (!Config.apiReady) {
            onDone(null)
            return
        }
        Thread {
            var result: String? = null
            try {
                val (code, text) = call(path, null)
                if (code in 200..299) result = text
            } catch (e: Exception) {
                // stays null
            }
            main.post { onDone(result) }
        }.start()
    }

    /** POST JSON to the website. */
    fun post(path: String, body: JSONObject, onDone: (ok: Boolean, message: String?) -> Unit) {
        if (!Config.apiReady) {
            onDone(false, "The COSUP server address is not set in this build.")
            return
        }
        Thread {
            var ok = false
            var message: String? = null
            try {
                val (code, text) = call(path, body)
                ok = code in 200..299
                if (!ok) message = json(text).optString("error", "The COSUP server could not save this.")
            } catch (e: Exception) {
                message = "Could not reach the COSUP server. Check your internet connection."
            }
            main.post { onDone(ok, message) }
        }.start()
    }

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
                val body = JSONObject()
                body.put("idToken", idToken)
                if (!firstName.isNullOrBlank()) body.put("firstName", firstName)
                if (!lastName.isNullOrBlank()) body.put("lastName", lastName)
                if (!phone.isNullOrBlank()) body.put("phone", phone)
                val (code, text) = call("/Auth/FirebaseLogin", body)
                val j = json(text)
                if (code in 200..299) role = j.optString("role", "user")
                else error = j.optString("error", "The COSUP server could not sign you in.")
            } catch (e: Exception) {
                error = "Could not reach the COSUP server. Check your internet connection."
            }
            main.post { onDone(role, error) }
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
