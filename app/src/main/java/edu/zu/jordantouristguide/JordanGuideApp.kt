package edu.zu.jordantouristguide

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Application entry point. [HiltAndroidApp] generates the dependency-injection container. */
@HiltAndroidApp
class JordanGuideApp : Application()
