package com.example.redsocial.utils

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

fun uploadImageToImgur(
    imageBytes: ByteArray,
    clientId: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val client = OkHttpClient()
    
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "image",
            "image.jpg",
            imageBytes.toRequestBody("image/*".toMediaTypeOrNull())
        )
        .build()

    val request = Request.Builder()
        .url("https://api.imgur.com/3/image")
        .header("Authorization", "Client-ID $clientId")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onError("Error al subir la imagen: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.body?.string()
            if (response.isSuccessful && responseBody != null) {
                try {
                    val json = JSONObject(responseBody)
                    val data = json.getJSONObject("data")
                    val imageUrl = data.getString("link")
                    onSuccess(imageUrl)
                } catch (e: Exception) {
                    onError("Error al procesar la respuesta: ${e.message}")
                }
            } else {
                onError("Error en la respuesta del servidor: ${response.code}")
            }
        }
    })
} 