package com.example.data.remote

import com.example.data.model.AlQuranMultiEditionResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface AlQuranApiService {
    @GET("v1/surah/{number}/editions/quran-uthmani,bn.bengali")
    suspend fun getSurahWithBengali(
        @Path("number") surahNumber: Int
    ): AlQuranMultiEditionResponse

    companion object {
        private const val BASE_URL = "https://api.alquran.cloud/"

        fun create(): AlQuranApiService {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(AlQuranApiService::class.java)
        }
    }
}
