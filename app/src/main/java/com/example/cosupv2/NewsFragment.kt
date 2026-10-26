package com.example.cosupv2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class NewsFragment : Fragment(R.layout.fragment_news) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = view.findViewById<LinearLayout>(R.id.newsList)
        val progress = view.findViewById<View>(R.id.newsProgress)

        Repo.loadNews { stories ->
            if (!isAdded) return@loadNews
            progress.visibility = View.GONE
            val ctx = requireContext()
            val inflater = LayoutInflater.from(ctx)
            stories.forEach { n ->
                val card = inflater.inflate(R.layout.item_news_card, list, false)

                val img = card.findViewById<ImageView>(R.id.newsImg)
                if (n.cover.isNotEmpty()) img.setDrawableByName(n.cover) else img.visibility = View.GONE

                val cat = card.findViewById<TextView>(R.id.newsCategory)
                if (n.category.isEmpty()) cat.visibility = View.GONE else cat.text = n.category

                card.findViewById<TextView>(R.id.newsTitle).text = n.title
                card.findViewById<TextView>(R.id.newsMeta).text = "${n.source} \u00B7 ${n.date}"
                card.findViewById<TextView>(R.id.newsBody).text = n.body

                val read = card.findViewById<View>(R.id.newsRead)
                if (n.url.isEmpty()) {
                    read.visibility = View.GONE
                } else {
                    read.setOnClickListener { ctx.openUrl(n.url) }
                }
                list.addView(card)
            }
        }
    }
}
