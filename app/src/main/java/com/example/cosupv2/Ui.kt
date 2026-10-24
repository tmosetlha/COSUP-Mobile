package com.example.cosupv2

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.View
import android.widget.ImageView

/**
 * Sets a picture from res/drawable by name, shrunk so big website pictures cannot crash the app.
 * If the file is missing, the view is hidden instead of crashing.
 */
fun ImageView.setDrawableByName(name: String, maxPx: Int = 900) {
    val id = resources.getIdentifier(name, "drawable", context.packageName)
    if (id == 0) {
        visibility = View.GONE
        return
    }
    try {
        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            inScaled = false
        }
        BitmapFactory.decodeResource(resources, id, bounds)

        var sample = 1
        while (bounds.outWidth / (sample * 2) >= maxPx || bounds.outHeight / (sample * 2) >= maxPx) {
            sample *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inScaled = false
        }
        val bitmap = BitmapFactory.decodeResource(resources, id, options)
        if (bitmap != null) {
            bitmap.density = Bitmap.DENSITY_NONE
            setImageBitmap(bitmap)
        } else {
            setImageResource(id)
        }
        visibility = View.VISIBLE
    } catch (e: Throwable) {
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