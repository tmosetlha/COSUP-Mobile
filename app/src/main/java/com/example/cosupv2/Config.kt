package com.example.cosupv2

/** Settings you fill in once. Everything else in the app reads from here. */
object Config {
    // 1. Your live website address, from Azure: App Services > your app > Overview > Default domain
    const val BASE_URL = "https://cosupweb20261019212549-ddfghmftbsb8b4at.southafricanorth-01.azurewebsites.net"

    // 2. Your Firebase Android App ID, from Firebase: Project settings > Your apps > COSUP Android > App ID
    const val FIREBASE_APP_ID = "1:1031564678835:android:69147cba0ecffa0045c2c6"

    // Already filled in for the cosup-semester2 project.
    const val FIREBASE_API_KEY = "AIzaSyDflGsVLdC3GSU1Tx69265NzItezcfjucg"
    const val FIREBASE_PROJECT_ID = "cosup-semester2"
    const val FIREBASE_SENDER_ID = "1031564678835"

    val firebaseReady: Boolean get() = !FIREBASE_APP_ID.startsWith("PASTE")
    val apiReady: Boolean get() = !BASE_URL.contains("PASTE")
}
