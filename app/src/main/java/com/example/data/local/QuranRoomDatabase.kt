package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "cached_ayahs")
data class CachedAyahEntity(
    @PrimaryKey val id: String, // "${surahNumber}_${numberInSurah}"
    val surahNumber: Int,
    val numberInSurah: Int,
    val textAr: String,
    val textBn: String
)

@Dao
interface QuranCacheDao {
    @Query("SELECT * FROM cached_ayahs WHERE surahNumber = :surahNumber ORDER BY numberInSurah ASC")
    suspend fun getSurahAyahs(surahNumber: Int): List<CachedAyahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<CachedAyahEntity>)

    @Query("SELECT DISTINCT surahNumber FROM cached_ayahs")
    suspend fun getCachedSurahNumbers(): List<Int>
}

@Database(
    entities = [CachedAyahEntity::class],
    version = 1,
    exportSchema = false
)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun quranCacheDao(): QuranCacheDao

    companion object {
        @Volatile
        private var INSTANCE: QuranDatabase? = null

        fun getInstance(context: Context): QuranDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    QuranDatabase::class.java,
                    "quran_offline_cache.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
