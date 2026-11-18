package edu.zu.jordantouristguide.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Reads a text file from the app's `assets` folder.
 * It is an interface so unit tests can pass JSON directly without Android.
 */
fun interface AssetReader {
    fun readText(fileName: String): String
}

class AndroidAssetReader @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AssetReader {
    override fun readText(fileName: String): String =
        context.assets.open(fileName).bufferedReader().use { it.readText() }
}
