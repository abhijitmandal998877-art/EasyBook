package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseManager {
    private var isInitialized = false
    private var auth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            // We initialize Firebase programmatically using the provided Firebase credentials.
            // This is robust, secure, and avoids dependencies on a physical google-services.json file,
            // which prevents various compile/sync errors in sandboxed environments.
            val options = FirebaseOptions.Builder()
                .setApiKey("AIzaSyBHWy_-ML2Hga98qKoNmxRN5U9EeU5o1lk")
                .setApplicationId("1:1095330034926:android:5e02069b76cad3c0e76fff")
                .setProjectId("easybook-1")
                .build()

            FirebaseApp.initializeApp(context.applicationContext, options)
            auth = FirebaseAuth.getInstance()
            db = FirebaseFirestore.getInstance()
            isInitialized = true
            Log.d("FirebaseManager", "Firebase programmatic initialization succeeded!")
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Firebase programmatic initialization failed: ${e.message}", e)
        }
    }

    fun getAuth(): FirebaseAuth? {
        return auth
    }

    fun getFirestore(): FirebaseFirestore? {
        return db
    }
}
