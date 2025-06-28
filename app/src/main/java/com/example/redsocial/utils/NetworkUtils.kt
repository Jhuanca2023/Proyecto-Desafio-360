package com.example.redsocial.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import okhttp3.*
import org.json.JSONObject
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException
import com.google.firebase.firestore.FirebaseFirestore

object NetworkUtils {
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    fun sendPushNotificationOneSignal(
        appId: String,
        apiKey: String,
        userId: String, // OneSignal player/user ID
        title: String,
        message: String
    ) {
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("app_id", appId)
            put("include_external_user_ids", listOf(userId))
            put("headings", JSONObject().put("es", title))
            put("contents", JSONObject().put("es", message))
        }
        val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), json.toString())
        val request = Request.Builder()
            .url("https://onesignal.com/api/v1/notifications")
            .addHeader("Authorization", "Basic $apiKey")
            .post(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Manejo de error
            }
            override fun onResponse(call: Call, response: Response) {
                // Manejo de respuesta
            }
        })
    }

    fun notificarEvento(
        usuarioObjetivoId: String,
        tipo: String,
        mensaje: String,
        oneSignalId: String? = null,
        actorId: String? = null,
        actorPhotoUrl: String? = null
    ) {
        val db = FirebaseFirestore.getInstance()
        val noti = hashMapOf(
            "tipo" to tipo,
            "mensaje" to mensaje,
            "fecha" to System.currentTimeMillis(),
            "leido" to false
        )
        if (actorId != null) noti["actorId"] = actorId
        if (actorPhotoUrl != null) noti["actorPhotoUrl"] = actorPhotoUrl
        db.collection("usuarios")
            .document(usuarioObjetivoId)
            .collection("notificaciones")
            .add(noti)
        if (oneSignalId != null) {
            sendPushNotificationOneSignal(
                appId = "816af768-2713-4c05-96ce-74c700c09862",
                apiKey = "TU_ONESIGNAL_REST_API_KEY", // <-- Reemplaza por tu API KEY real
                userId = oneSignalId,
                title = "¡Nueva notificación!",
                message = mensaje
            )
        }
    }
} 