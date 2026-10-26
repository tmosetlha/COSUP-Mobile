package com.example.cosupv2

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment

class AboutFragment : Fragment(R.layout.fragment_about) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageView>(R.id.aboutPhoto).setDrawableByName("about")
        view.findViewById<ImageView>(R.id.aboutCity).setDrawableByName("city_of_tshwane")
        view.findViewById<ImageView>(R.id.aboutUp).setDrawableByName("university_of_pretoria")
    }
}
