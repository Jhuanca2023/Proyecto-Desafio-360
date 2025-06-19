package com.example.redsocial.utils

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import okhttp3.RequestBody.Companion.toRequestBody
import android.widget.Toast
import android.content.Context

fun uploadVideoToSupabase(
    videoBytes: ByteArray,
    fileName: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val supabaseUrl = "https://qvfsdtoqfojsimpbplol.supabase.co"
    val bucket = "evidencias"
    val apiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF2ZnNkdG9xZm9qc2ltcGJwbG9sIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg5ODA3NTQsImV4cCI6MjA2NDU1Njc1NH0.842LbnOG85_Qav-AeKcTlDT2dX1cXOWURJFUzj0SiF8"

    val client = OkHttpClient()
    val requestBody = videoBytes.toRequestBody("video/mp4".toMediaTypeOrNull())
    val url = "$supabaseUrl/storage/v1/object/$bucket/$fileName"
    val request = Request.Builder()
        .url(url)
        .addHeader("apikey", apiKey)
        .addHeader("Authorization", "Bearer $apiKey")
        .put(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onError("Error al subir el video: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                val publicUrl = "$supabaseUrl/storage/v1/object/public/$bucket/$fileName"
                onSuccess(publicUrl)
            } else {
                onError("Error en la respuesta del servidor: ${response.code} - ${response.body?.string()}")
            }
        }
    })
}

fun uploadVideoToSupabase(
    videoBytes: ByteArray,
    fileName: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
    context: Context
) {
    val supabaseUrl = "https://qvfsdtoqfojsimpbplol.supabase.co"
    val bucket = "evidencias"
    val apiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF2ZnNkdG9xZm9qc2ltcGJwbG9sIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg5ODA3NTQsImV4cCI6MjA2NDU1Njc1NH0.842LbnOG85_Qav-AeKcTlDT2dX1cXOWURJFUzj0SiF8"

    val client = OkHttpClient()
    val requestBody = videoBytes.toRequestBody("video/mp4".toMediaTypeOrNull())
    val url = "$supabaseUrl/storage/v1/object/$bucket/$fileName"
    val request = Request.Builder()
        .url(url)
        .addHeader("apikey", apiKey)
        .addHeader("Authorization", "Bearer $apiKey")
        .put(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onError("Error al subir el video: ${e.message}")
            Toast.makeText(context, "Error al subir el video: ${e.message}", Toast.LENGTH_LONG).show()
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                val publicUrl = "$supabaseUrl/storage/v1/object/public/$bucket/$fileName"
                onSuccess(publicUrl)
            } else {
                onError("Error en la respuesta del servidor: ${response.code} - ${response.body?.string()}")
                Toast.makeText(context, "Error en la respuesta del servidor: ${response.code} - ${response.body?.string()}", Toast.LENGTH_LONG).show()
            }
        }
    })
} 