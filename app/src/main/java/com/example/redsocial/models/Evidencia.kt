package com.example.redsocial.models

data class Evidencia(
    val id: String = "",
    val challengeId: String = "",
    val userId: String = "",
    val userName: String = "",
    val tipo: String = "", // video, imagen, texto, audio
    val url: String? = null, // para imagen/video/audio
    val texto: String? = null, // para texto
    val descripcion: String? = null, // descripción de la evidencia
    val timestamp: Long = System.currentTimeMillis(),
    val views: Int = 0, // NUEVO campo para vistas
    val downloadsAllowed: Boolean = true // NUEVO campo para controlar descargas
) 