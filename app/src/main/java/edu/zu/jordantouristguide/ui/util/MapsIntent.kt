package edu.zu.jordantouristguide.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Opens the coordinates in Google Maps. If the Maps app is not installed,
 * falls back to the Google Maps website in the browser.
 * @return false if nothing on the device can show the location.
 */
fun openInGoogleMaps(context: Context, latitude: Double, longitude: Double, label: String): Boolean {
    val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(label)})")
    val mapsAppIntent = Intent(Intent.ACTION_VIEW, geoUri).setPackage("com.google.android.apps.maps")
    val webIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"),
    )
    return try {
        context.startActivity(mapsAppIntent)
        true
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(webIntent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }
}
