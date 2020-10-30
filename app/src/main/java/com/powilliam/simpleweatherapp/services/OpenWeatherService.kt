package com.powilliam.simpleweatherapp.services

import com.powilliam.simpleweatherapp.models.Weather
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherService {
    @GET("/data/2.5/weather")
    fun getCurrentWeatherDetails(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") appid: String = APP_ID,
        @Query("units") units: String = UNITS
    ): Call<Weather>

    companion object {
        private const val APP_ID = "a29a04266240a4d8fbbe72a5a9195034"
        private const val UNITS = "metric"
        private const val BASE_URL = "https://api.openweathermap.org/"

        fun create(): OpenWeatherService {
            return Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(OpenWeatherService::class.java)
        }
    }
}