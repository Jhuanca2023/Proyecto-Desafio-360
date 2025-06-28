package com.example.redsocial.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.FileInputStream

object AudioUtils {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false

    fun startRecording(context: Context, onError: (String) -> Unit): Boolean {
        if (isRecording) {
            onError("Ya está grabando")
            return false
        }

        try {
            // Crear archivo temporal para el audio
            audioFile = File(context.cacheDir, "audio_${System.currentTimeMillis()}.mp3")
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile?.absolutePath)
                
                prepare()
                start()
            }

            isRecording = true
            Log.d("AudioUtils", "Grabación iniciada: ${audioFile?.absolutePath}")
            return true

        } catch (e: Exception) {
            Log.e("AudioUtils", "Error iniciando grabación: ${e.message}")
            onError("Error iniciando grabación: ${e.message}")
            return false
        }
    }

    fun stopRecording(): File? {
        if (!isRecording) {
            return null
        }

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            
            Log.d("AudioUtils", "Grabación detenida")
            return audioFile

        } catch (e: Exception) {
            Log.e("AudioUtils", "Error deteniendo grabación: ${e.message}")
            return null
        }
    }

    fun isRecording(): Boolean = isRecording

    fun uploadAudioToSupabase(
        audioFile: File,
        fileName: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val supabaseUrl = "https://qvfsdtoqfojsimpbplol.supabase.co"
        val bucket = "evidencias"
        val apiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF2ZnNkdG9xZm9qc2ltcGJwbG9sIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg5ODA3NTQsImV4cCI6MjA2NDU1Njc1NH0.842LbnOG85_Qav-AeKcTlDT2dX1cXOWURJFUzj0SiF8"

        try {
            val audioBytes = audioFile.readBytes()
            val client = OkHttpClient()
            val requestBody = audioBytes.toRequestBody("audio/mpeg".toMediaTypeOrNull())
            val url = "$supabaseUrl/storage/v1/object/$bucket/$fileName"
            
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer $apiKey")
                .put(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onError("Error al subir el audio: ${e.message}")
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

        } catch (e: Exception) {
            onError("Error procesando el audio: ${e.message}")
        }
    }
} 