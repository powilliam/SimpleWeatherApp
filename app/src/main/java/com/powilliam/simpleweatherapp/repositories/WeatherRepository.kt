package com.powilliam.simpleweatherapp.repositories

import com.powilliam.simpleweatherapp.models.Coordinates
import com.powilliam.simpleweatherapp.models.Weather
import com.powilliam.simpleweatherapp.services.WeatherService
import retrofit2.*

class WeatherRepository(private val weatherService: WeatherService) {
    suspend fun getCurrentWeatherDetails(coordinates: Coordinates): Weather? {
        val response: Response<Weather>  = weatherService
                .getCurrentWeatherDetails(coordinates.latitude, coordinates.longitude)
                .awaitResponse()
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }
}