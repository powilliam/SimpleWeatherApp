package com.powilliam.simpleweatherapp.repositories

import com.powilliam.simpleweatherapp.models.Coordinates
import com.powilliam.simpleweatherapp.models.Weather
import com.powilliam.simpleweatherapp.services.WeatherService
import retrofit2.*

class WeatherRepository(private val weatherService: WeatherService) {
    suspend fun getWeatherDetailsFromCoordinates(coordinates: Coordinates): Weather? {
        return weatherService
                .getCurrentWeatherDetails(coordinates.latitude, coordinates.longitude)
                .await()
    }
}