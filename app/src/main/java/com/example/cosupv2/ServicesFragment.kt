package com.example.cosupv2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment

class ServicesFragment : Fragment(R.layout.fragment_services) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = view.findViewById<LinearLayout>(R.id.servicesList)
        val progress = view.findViewById<View>(R.id.servicesProgress)

        view.findViewById<View>(R.id.btnFindSites).setOnClickListener {
            (activity as? MainActivity)?.openTab(R.id.nav_find)
        }

        Repo.loadServices { services ->
            if (!isAdded) return@loadServices
            progress.visibility = View.GONE
            val inflater = LayoutInflater.from(requireContext())
            services.forEach { bindServiceCard(inflater, list, it) }
        }
    }
}
