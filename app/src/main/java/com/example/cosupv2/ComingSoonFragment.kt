package com.example.cosupv2

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment

class ComingSoonFragment : Fragment(R.layout.fragment_coming_soon) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.csTitle).text = requireArguments().getString("title")
    }

    companion object {
        fun newInstance(title: String) = ComingSoonFragment().apply {
            arguments = Bundle().apply { putString("title", title) }
        }
    }
}
