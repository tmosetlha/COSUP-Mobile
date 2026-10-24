package com.example.cosupv2

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.ImageView

/** Sets an image from res/drawable by name. If the file is missing, the view is hidden instead of crashing. */
fun ImageView.setDrawableByName(name: String) {
    val id = resources.getIdentifier(name, "drawable", context.packageName)
    if (id != 0) {
        setImageResource(id)
        visibility = View.VISIBLE
    } else {
        visibility = View.GONE
    }
}

fun Context.openUrl(url: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: ActivityNotFoundException) {
        // no browser installed
    }
}

fun Context.dial(number: String) {
    try {
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: ActivityNotFoundException) {
        // no dialer available
    }
}
