package com.example.cosupv2

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.json.JSONObject

/** The self-referral form. Saved in the same database as the website form. */
class ReferralFragment : Fragment(R.layout.fragment_referral) {

    private var siteNames = listOf("No preference")
    private var serviceNames = listOf("Not sure")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()

        val name = view.findViewById<EditText>(R.id.refName)
        val phone = view.findViewById<EditText>(R.id.refPhone)
        val email = view.findViewById<EditText>(R.id.refEmail)
        val message = view.findViewById<EditText>(R.id.refMessage)
        val siteSpinner = view.findViewById<Spinner>(R.id.refSite)
        val serviceSpinner = view.findViewById<Spinner>(R.id.refService)
        val consent = view.findViewById<CheckBox>(R.id.refConsent)
        val msg = view.findViewById<TextView>(R.id.refMsg)
        val submit = view.findViewById<View>(R.id.refSubmit)

        SessionStore.name(ctx)?.let { name.setText(it) }

        fun setAdapter(spinner: Spinner, items: List<String>) {
            spinner.adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, items)
        }
        setAdapter(siteSpinner, siteNames)
        setAdapter(serviceSpinner, serviceNames)

        Repo.loadSites { sites ->
            if (!isAdded) return@loadSites
            siteNames = listOf("No preference") + sites.map { it.name }
            setAdapter(siteSpinner, siteNames)
        }
        Repo.loadServices { services ->
            if (!isAdded) return@loadServices
            serviceNames = listOf("Not sure") + services.map { it.name }
            setAdapter(serviceSpinner, serviceNames)
        }

        fun show(text: String, ok: Boolean) {
            msg.text = text
            msg.setBackgroundResource(if (ok) R.drawable.bg_ok else R.drawable.bg_error)
            msg.setTextColor(android.graphics.Color.parseColor(if (ok) "#17642A" else "#9B2F17"))
            msg.visibility = View.VISIBLE
        }

        submit.setOnClickListener {
            val n = name.text.toString().trim()
            val p = phone.text.toString().trim()
            val e = email.text.toString().trim()

            if (n.isEmpty()) return@setOnClickListener show("Please enter your full name.", false)
            if (!Regex("^[0-9+ ]{9,15}$").matches(p)) return@setOnClickListener show("Enter a valid phone number (digits only).", false)
            if (e.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(e).matches()) return@setOnClickListener show("Enter a valid email address.", false)
            if (!consent.isChecked) return@setOnClickListener show("Please accept the POPIA consent to continue.", false)

            val body = JSONObject()
            body.put("fullName", n)
            body.put("contactNumber", p)
            if (e.isNotEmpty()) body.put("email", e)
            if (siteSpinner.selectedItemPosition > 0) body.put("preferredArea", siteNames[siteSpinner.selectedItemPosition])
            if (serviceSpinner.selectedItemPosition > 0) body.put("serviceNeeded", serviceNames[serviceSpinner.selectedItemPosition])
            val m = message.text.toString().trim()
            if (m.isNotEmpty()) body.put("message", m)
            body.put("consent", true)

            submit.isEnabled = false
            msg.visibility = View.GONE
            Api.post("/api/referrals", body) { ok, error ->
                if (!isAdded) return@post
                submit.isEnabled = true
                if (ok) {
                    show("Your referral has been submitted. A COSUP team member will contact you within 24 hours.", true)
                    phone.setText("")
                    email.setText("")
                    message.setText("")
                    consent.isChecked = false
                } else {
                    show(error ?: "Something went wrong. Please try again.", false)
                }
            }
        }
    }
}
