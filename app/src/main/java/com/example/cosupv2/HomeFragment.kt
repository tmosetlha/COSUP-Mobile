package com.example.cosupv2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.Calendar

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val main = requireActivity() as MainActivity
        val ctx = requireContext()
        val inflater = LayoutInflater.from(ctx)

        // Pictures (copied into res/drawable). Missing pictures are simply hidden.
        view.findViewById<ImageView>(R.id.bannerImg).setDrawableByName("banner")
        view.findViewById<ImageView>(R.id.hopeImg).setDrawableByName("hopeline")
        view.findViewById<ImageView>(R.id.footerLogo).setDrawableByName("logo")
        view.findViewById<ImageView>(R.id.partnerCity).setDrawableByName("city_of_tshwane")
        view.findViewById<ImageView>(R.id.partnerUp).setDrawableByName("university_of_pretoria")

        // Hero buttons
        view.findViewById<View>(R.id.heroSelfReferral).setOnClickListener { main.openTab(R.id.nav_more) }
        view.findViewById<View>(R.id.heroFindSite).setOnClickListener { main.openTab(R.id.nav_find) }

        // Services
        val servicesContainer = view.findViewById<LinearLayout>(R.id.servicesContainer)
        SampleData.services.forEach { s ->
            val card = inflater.inflate(R.layout.item_service, servicesContainer, false)
            card.findViewById<TextView>(R.id.svcName).text = s.name
            card.findViewById<TextView>(R.id.svcDesc).text = s.description
            val icon = card.findViewById<ImageView>(R.id.svcIcon)
            icon.setDrawableByName(s.icon)
            card.findViewById<View>(R.id.svcIconWrap).visibility = icon.visibility
            servicesContainer.addView(card)
        }
        view.findViewById<View>(R.id.btnAllServices).setOnClickListener { main.openTab(R.id.nav_services) }

        // Find a site + news highlights
        view.findViewById<View>(R.id.btnOpenMap).setOnClickListener { main.openTab(R.id.nav_find) }
        val newsContainer = view.findViewById<LinearLayout>(R.id.newsContainer)
        SampleData.news.forEach { n ->
            val row = inflater.inflate(R.layout.item_news_row, newsContainer, false)
            row.findViewById<TextView>(R.id.newsTitle).text = n.title
            row.findViewById<TextView>(R.id.newsMeta).text = "${n.source} \u00B7 ${n.date}"
            row.setOnClickListener { ctx.openUrl(n.url) }
            newsContainer.addView(row)
        }
        view.findViewById<View>(R.id.btnAllNews).setOnClickListener { main.openTab(R.id.nav_news) }

        // Hopeline
        view.findViewById<View>(R.id.btnCall).setOnClickListener { ctx.dial("0800611197") }
        view.findViewById<View>(R.id.footerCall).setOnClickListener { ctx.dial("0800611197") }

        val year = Calendar.getInstance().get(Calendar.YEAR)
        view.findViewById<TextView>(R.id.copyright).text = "\u00A9 $year COSUP \u00B7 The 5 Watermelons"
    }
}
