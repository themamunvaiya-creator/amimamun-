package com.example.data.model

data class DailyDua(
    val id: String,
    val category: String,
    val titleBn: String,
    val arabic: String,
    val pronunciationBn: String,
    val meaningBn: String,
    val referenceBn: String,
    val virtueBn: String = ""
)
