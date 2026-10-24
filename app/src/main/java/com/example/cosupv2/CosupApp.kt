package com.example.cosupv2

import android.app.Application
import android.content.Context
import java.io.PrintWriter
import java.io.StringWriter

/** Saves the error if the app crashes, so the next start can show it. */
class CosupApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            try {
                val writer = StringWriter()
                error.printStackTrace(PrintWriter(writer))
                getSharedPreferences("cosup_crash", Context.MODE_PRIVATE)
                    .edit().putString("last", writer.toString()).commit()
            } catch (e: Throwable) {
                // ignore
            }
            previous?.uncaughtException(thread, error)
        }
    }
}