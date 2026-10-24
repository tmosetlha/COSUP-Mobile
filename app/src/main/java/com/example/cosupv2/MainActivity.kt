package com.example.cosupv2

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.apply(this)
        val dark = ThemeManager.isDark(this)
        val barStyle = if (dark) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, bars.top, 0, 0)
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

        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_services -> ComingSoonFragment.newInstance("Services")
                R.id.nav_find -> ComingSoonFragment.newInstance("Find a Site")
                R.id.nav_news -> ComingSoonFragment.newInstance("News")
                else -> ComingSoonFragment.newInstance("More")
            }
            show(fragment)
            true
        }
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_home
        }
    }

    private fun show(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    /** Lets a screen jump to another tab, for example Home -> Find a Site. */
    fun openTab(itemId: Int) {
        bottomNav.selectedItemId = itemId
    }
}
