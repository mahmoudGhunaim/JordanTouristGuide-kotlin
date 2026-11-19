package edu.zu.jordantouristguide.config

import edu.zu.jordantouristguide.BuildConfig

/**
 * Central switch between the offline data source and Firebase.
 *
 * - `USE_FIREBASE = false` (default): places come from `assets/places.json`,
 *   reviews and demo accounts are stored locally in Room. No setup needed.
 * - `USE_FIREBASE = true`: places and reviews come from Cloud Firestore and
 *   login uses Firebase Authentication. Requires `app/google-services.json`
 *   (see README.md -> "Connecting Firebase").
 */
object AppConfig {

    /** Flip this to `true` after adding google-services.json. */
    const val USE_FIREBASE = true

    /**
     * When Firebase is on and the Firestore "places" collection is empty,
     * upload the bundled places.json once so the team doesn't have to
     * type the data in the console by hand.
     */
    const val SEED_FIRESTORE_IF_EMPTY = true

    /**
     * Firebase is only used when the flag is on AND the config file was found
     * at build time, so a missing google-services.json can never crash the app.
     */
    val isFirebaseEnabled: Boolean
        get() = USE_FIREBASE && BuildConfig.FIREBASE_CONFIGURED
}
