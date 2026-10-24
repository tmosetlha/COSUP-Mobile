package com.example.cosupv2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var launched = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        findViewById<ImageView>(R.id.splashLogo).setDrawableByName("logo")

        val card = findViewById<View>(R.id.logoCard)
        val title = findViewById<View>(R.id.splashTitle)
        val sub = findViewById<View>(R.id.splashSub)
        val spinner = findViewById<View>(R.id.splashSpinner)
        val shift = resources.displayMetrics.density * 16f

        card.alpha = 0f
        card.scaleX = 0.8f
        card.scaleY = 0.8f
        title.alpha = 0f
        title.translationY = shift
        sub.alpha = 0f
        sub.translationY = shift
        spinner.alpha = 0f

        card.animate().alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration(900).setInterpolator(OvershootInterpolator(1.1f)).start()
        title.animate().alpha(1f).translationY(0f).setStartDelay(600).setDuration(700).start()
        sub.animate().alpha(1f).translationY(0f).setStartDelay(850).setDuration(700).start()
        spinner.animate().alpha(1f).setStartDelay(1000).setDuration(600).start()

        findViewById<View>(R.id.splashRoot).setOnClickListener { goNext() }

        val prefs = getSharedPreferences("cosup_crash", Context.MODE_PRIVATE)
        val crash = prefs.getString("last", null)
        if (crash != null) {
            prefs.edit().remove("last").apply()
            showCrash(crash)
        } else {
            handler.postDelayed({ goNext() }, 2800)
        }
    }

    /** Shows the error from the last crash so it can be fixed quickly. */
    private fun showCrash(trace: String) {
        val lines = trace.lines()
        val summary = (lines.take(4) +
                lines.filter { it.startsWith("Caused by") } +
                listOf("-- COSUP code --") +
                lines.filter { it.contains("com.example.cosupv2") }.take(8))
            .joinToString("\n")

        AlertDialog.Builder(this)
            .setTitle("COSUP crashed last time")
            .setMessage(summary)
            .setCancelable(false)
            .setPositiveButton("Continue") { _, _ -> handler.postDelayed({ goNext() }, 300) }
            .show()
    }

    @Suppress("DEPRECATION")
    private fun goNext() {
        if (launched) return
        launched = true
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}