package com.powilliam.simpleweatherapp.usecases

import android.location.Location
import com.powilliam.simpleweatherapp.models.Weather
import com.powilliam.simpleweatherapp.services.OpenWeatherService
import retrofit2.await
import javax.inject.Inject

class GetWeatherDetailsFromLocationUseCase @Inject constructor(
        private val weatherService: OpenWeatherService
) {
    suspend fun execute(location: Location): Weather {
        return weatherService
                .getCurrentWeatherDetails(location.latitude, location.longitude)
                .await()
    }
}