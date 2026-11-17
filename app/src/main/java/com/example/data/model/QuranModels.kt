package com.example.data.model

import com.squareup.moshi.JsonClass

data class SurahInfo(
    val number: Int,
    val nameAr: String,
    val nameBn: String,
    val meaningBn: String,
    val ayahCount: Int,
    val revelationTypeBn: String
)

data class AyahItem(
    val numberInSurah: Int,
    val textAr: String,
    val textBn: String,
    val sajda: Boolean = false
)

@JsonClass(generateAdapter = true)
data class AlQuranMultiEditionResponse(
    val code: Int,
    val status: String,
    val data: List<AlQuranEditionSurahDto>
)

@JsonClass(generateAdapter = true)
data class AlQuranEditionSurahDto(
    val number: Int,
    val name: String,
    val englishName: String,
    val numberOfAyahs: Int,
    val ayahs: List<AlQuranAyahDto>
)

@JsonClass(generateAdapter = true)
data class AlQuranAyahDto(
    val numberInSurah: Int,
    val text: String
)
