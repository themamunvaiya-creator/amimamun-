package com.example.data.repository

import com.example.data.local.CachedAyahEntity
import com.example.data.local.PrebundledQuranData
import com.example.data.local.QuranCacheDao
import com.example.data.local.SurahMetadata
import com.example.data.model.AyahItem
import com.example.data.model.SurahInfo
import com.example.data.remote.AlQuranApiService
import kotlinx.coroutines.CancellationException

class QuranRepository(
    private val apiService: AlQuranApiService,
    private val cacheDao: QuranCacheDao
) {
    fun getAllSurahs(): List<SurahInfo> = SurahMetadata.allSurahs

    suspend fun getCachedSurahNumbers(): Set<Int> {
        val dbCached = cacheDao.getCachedSurahNumbers().toSet()
        return dbCached + PrebundledQuranData.bundledSurahs.keys
    }

    suspend fun getSurahAyahs(surahNumber: Int): Result<List<AyahItem>> {
        return try {
            // 1. Check Room cache first
            val cached = cacheDao.getSurahAyahs(surahNumber)
            if (cached.isNotEmpty()) {
                return Result.success(
                    cached.map {
                        AyahItem(
                            numberInSurah = it.numberInSurah,
                            textAr = it.textAr,
                            textBn = it.textBn
                        )
                    }
                )
            }

            // 2. Check prebundled offline Surahs
            val bundled = PrebundledQuranData.bundledSurahs[surahNumber]
            if (bundled != null) {
                return Result.success(bundled)
            }

            // 3. Fetch full Arabic + Bengali Surah from AlQuran Cloud API and cache in Room
            val response = apiService.getSurahWithBengali(surahNumber)
            if (response.data.size < 2) {
                return Result.failure(IllegalStateException("অসম্পূর্ণ সূরা তথ্য পাওয়া গেছে।"))
            }
            val arabicAyahs = response.data[0].ayahs
            val bengaliAyahs = response.data[1].ayahs

            val merged = arabicAyahs.mapIndexed { index, arAyah ->
                val bnText = bengaliAyahs.getOrNull(index)?.text ?: ""
                AyahItem(
                    numberInSurah = arAyah.numberInSurah,
                    textAr = arAyah.text,
                    textBn = bnText
                )
            }

            cacheDao.insertAyahs(
                merged.map { ayah ->
                    CachedAyahEntity(
                        id = "${surahNumber}_${ayah.numberInSurah}",
                        surahNumber = surahNumber,
                        numberInSurah = ayah.numberInSurah,
                        textAr = ayah.textAr,
                        textBn = ayah.textBn
                    )
                }
            )

            Result.success(merged)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Fallback to bundled if somehow available, else return failure
            PrebundledQuranData.bundledSurahs[surahNumber]?.let {
                return Result.success(it)
            }
            Result.failure(e)
        }
    }
}
