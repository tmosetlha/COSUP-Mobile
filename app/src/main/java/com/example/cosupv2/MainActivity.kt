package com.example.cosupv2

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private class Tab(val id: Int, val icon: Int, val label: Int)

    private val tabs = listOf(
        Tab(R.id.nav_home, R.id.nav_home_icon, R.id.nav_home_label),
        Tab(R.id.nav_services, R.id.nav_services_icon, R.id.nav_services_label),
        Tab(R.id.nav_find, R.id.nav_find_icon, R.id.nav_find_label),
        Tab(R.id.nav_news, R.id.nav_news_icon, R.id.nav_news_label),
        Tab(R.id.nav_more, R.id.nav_more_icon, R.id.nav_more_label)
    )

    private var current = R.id.nav_home
    private var dark = false

    private val authLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { refreshAvatar() }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.apply(this)
        dark = ThemeManager.isDark(this)
        val barStyle = if (dark) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val root = findViewById<View>(R.id.main)
        val bottomBar = findViewById<View>(R.id.bottomBar)
        val basePad = (6 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, bars.top, 0, 0)
            bottomBar.setPadding(0, basePad, 0, basePad + bars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.logoView).setDrawableByName("logo")
        findViewById<ImageView>(R.id.hopelineBtn).apply {
            setDrawableByName("hopeline")
            setOnClickListener { dial("0800611197") }
        }

        val themeBtn = findViewById<ImageButton>(R.id.themeBtn)
        themeBtn.setImageResource(if (dark) R.drawable.ic_moon else R.drawable.ic_sun)
        themeBtn.setBackgroundResource(if (dark) R.drawable.bg_toggle_moon else R.drawable.bg_toggle_sun)
        themeBtn.setOnClickListener {
            ThemeManager.setDark(this, !dark)
            recreate()
        }

        findViewById<View>(R.id.avatarBtn).setOnClickListener { openAccount() }
        refreshAvatar()

        tabs.forEach { tab ->
            findViewById<View>(tab.id).setOnClickListener { select(tab.id) }
        }

        current = savedInstanceState?.getInt("tab") ?: R.id.nav_home
        paintTabs()
        if (savedInstanceState == null) {
            show(fragmentFor(current))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshAvatar()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("tab", current)
    }

    // ---------- account button ----------

    private fun refreshAvatar() {
        val name = SessionStore.name(this)
        val icon = findViewById<View>(R.id.avatarIcon)
        val initials = findViewById<TextView>(R.id.avatarInitials)
        if (name == null) {
            icon.visibility = View.VISIBLE
            initials.visibility = View.GONE
        } else {
            val letters = name.split(" ").filter { it.isNotBlank() }.take(2)
                .joinToString("") { it.first().uppercase() }
            initials.text = letters.ifEmpty { "U" }
            icon.visibility = View.GONE
            initials.visibility = View.VISIBLE
        }
    }

    /** Opens the sign-in screen, or the account box if someone is already signed in. */
    fun openAccount() {
        val name = SessionStore.name(this)
        if (name == null) {
            authLauncher.launch(Intent(this, AuthActivity::class.java))
            return
        }
        val role = SessionStore.role(this)
        val detail = if (role == "guest") "Browsing as a guest" else (SessionStore.email(this) ?: "Signed in")
        AlertDialog.Builder(this)
            .setTitle(name)
            .setMessage(detail)
            .setPositiveButton(if (role == "guest") "End guest session" else "Sign out") { _, _ ->
                if (Config.firebaseReady) {
                    try { FirebaseAuth.getInstance().signOut() } catch (e: Exception) { /* ignore */ }
                }
                SessionStore.clear(this)
                refreshAvatar()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    // ---------- tabs ----------

    private fun fragmentFor(id: Int): Fragment = when (id) {
        R.id.nav_home -> HomeFragment()
        R.id.nav_services -> ServicesFragment()
        R.id.nav_find -> FindSiteFragment()
        R.id.nav_news -> NewsFragment()
        else -> MoreFragment()
    }

    private fun select(id: Int) {
        if (id == current && supportFragmentManager.findFragmentById(R.id.fragmentContainer) != null) {
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            return
        }
        current = id
        paintTabs()
        show(fragmentFor(id))
    }

    private fun paintTabs() {
        val green = ContextCompat.getColor(this, R.color.cosup_green)
        val muted = Color.parseColor(if (dark) "#93A79B" else "#6B7C72")
        tabs.forEach { tab ->
            val color = if (tab.id == current) green else muted
            findViewById<ImageView>(tab.icon).setColorFilter(color)
            findViewById<TextView>(tab.label).apply {
                setTextColor(color)
                setTypeface(null, if (tab.id == current) Typeface.BOLD else Typeface.NORMAL)
            }
        }
    }

    private fun show(fragment: Fragment) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    /** Opens a screen on top of the current tab. The back button returns to the tab. */
    fun openScreen(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    /** Lets a screen jump to another tab, for example Home -> Find a Site. */
    fun openTab(itemId: Int) {
        select(itemId)
    }
}
