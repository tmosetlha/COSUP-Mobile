package com.example.cosupv2

import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

/** Account, self-referral, about, Hopeline, WhatsApp and Facebook. */
class MoreFragment : Fragment(R.layout.fragment_more) {

    override fun onResume() {
        super.onResume()
        build()
    }

    private fun build() {
        val v = view ?: return
        val list = v.findViewById<LinearLayout>(R.id.moreList)
        list.removeAllViews()
        val ctx = requireContext()
        val main = requireActivity() as MainActivity

        val name = SessionStore.name(ctx)
        if (name == null) {
            row(list, null, "Sign in / Register", "Google, Yahoo, Microsoft, email or guest") { main.openAccount() }
        } else {
            val sub = if (SessionStore.role(ctx) == "guest") "Guest session. Tap to end it."
            else (SessionStore.email(ctx) ?: "Signed in") + ". Tap to sign out."
            row(list, null, name, sub) { main.openAccount() }
        }
        row(list, null, "Self-referral form", "Ask a COSUP caseworker to contact you") { main.openScreen(ReferralFragment()) }
        row(list, null, "About COSUP", "Our story, values and partners") { main.openScreen(AboutFragment()) }
        row(list, null, "Call the Hopeline", "0800 611 197, free and confidential") { ctx.dial("0800611197") }
        row(list, R.drawable.ic_whatsapp, "WhatsApp", "012 945 2000") { ctx.openUrl("https://wa.me/27129452000") }
        row(list, R.drawable.ic_facebook, "Facebook", "UPCOPCCOSUP") { ctx.openUrl("https://www.facebook.com/UPCOPCCOSUP") }
    }

    private fun row(list: LinearLayout, icon: Int?, title: String, sub: String, onClick: () -> Unit) {
        val row = LayoutInflater.from(requireContext()).inflate(R.layout.item_menu_row, list, false)
        val iconView = row.findViewById<ImageView>(R.id.rowIcon)
        if (icon == null) iconView.visibility = android.view.View.GONE else iconView.setImageResource(icon)
        row.findViewById<TextView>(R.id.rowTitle).text = title
        row.findViewById<TextView>(R.id.rowSub).text = sub
        row.setOnClickListener { onClick() }
        list.addView(row)
    }
}
