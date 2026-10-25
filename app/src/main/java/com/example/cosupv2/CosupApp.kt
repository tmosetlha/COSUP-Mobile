package com.example.cosupv2

import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Process
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

/** Starts Firebase, and on a crash opens the error screen instead of just closing. */
class CosupApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val processName = if (Build.VERSION.SDK_INT >= 28) getProcessName() else packageName
        if (processName.endsWith(":crash")) return

        if (Config.firebaseReady && FirebaseApp.getApps(this).isEmpty()) {
            try {
                FirebaseApp.initializeApp(
                    this,
                    FirebaseOptions.Builder()
                        .setApiKey(Config.FIREBASE_API_KEY)
                        .setApplicationId(Config.FIREBASE_APP_ID)
                        .setProjectId(Config.FIREBASE_PROJECT_ID)
                        .setGcmSenderId(Config.FIREBASE_SENDER_ID)
                        .build()
                )
            } catch (e: Exception) {
                // Auth screen will explain that sign-in is not available
            }
        }

        Thread.setDefaultUncaughtExceptionHandler { _, error ->
            try {
                val writer = StringWriter()
                error.printStackTrace(PrintWriter(writer))
                val intent = Intent(this, CrashActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .putExtra("trace", writer.toString().take(8000))
                startActivity(intent)
            } catch (e: Throwable) {
                // ignore
            }
            Process.killProcess(Process.myPid())
            exitProcess(10)
        }
    }
}
