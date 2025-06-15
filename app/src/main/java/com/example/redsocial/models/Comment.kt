package com.example.redsocial.models

data class Comment(
    val id: String = "",
    val challengeId: String = "",
    val userId: String = "",
    val userName: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
) 