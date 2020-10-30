package com.powilliam.simpleweatherapp.repositories

import com.powilliam.simpleweatherapp.models.Coordinates
import com.powilliam.simpleweatherapp.models.Weather
import com.powilliam.simpleweatherapp.services.OpenWeatherService
import retrofit2.*

// TODO Implements getWeatherDetailsFromCoordinates as a UseCase
class WeatherRepository(private val weatherService: OpenWeatherService) {
    suspend fun getWeatherDetailsFromCoordinates(coordinates: Coordinates): Weather? {
        return weatherService
                .getCurrentWeatherDetails(coordinates.latitude, coordinates.longitude)
                .await()
    }
}