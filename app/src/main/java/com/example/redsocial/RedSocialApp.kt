package com.example.redsocial

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.onesignal.OneSignal

class RedSocialApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Inicializar OneSignal
            OneSignal.initWithContext(this, "816af768-2713-4c05-96ce-74c700c09862")
            
            // Firebase
            FirebaseApp.initializeApp(this)
            

            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            
            val db = FirebaseFirestore.getInstance()
            db.firestoreSettings = settings
            
            // almacenamiento en caché para la colección de usuarios
            db.collection(COLLECTION_USUARIOS)
                .get()
                .addOnSuccessListener { /* Cache inicializado */ }
                .addOnFailureListener { /* Manejo silencioso */ }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    companion object {
        const val COLLECTION_USUARIOS = "usuarios"
        const val COLLECTION_POSTS = "posts"
        const val COLLECTION_COMMENTS = "comments"
    }
} 