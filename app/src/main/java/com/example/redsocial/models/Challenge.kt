package com.example.redsocial.models

data class Challenge(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val creatorId: String = "",
    val category: String = "",
    val imageUrl: String? = null
) 