package com.powilliam.simpleweatherapp

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powilliam.simpleweatherapp.models.Coordinates
import com.powilliam.simpleweatherapp.models.Weather
import com.powilliam.simpleweatherapp.repositories.WeatherRepository
import kotlinx.coroutines.launch
import java.lang.Exception

// TODO Implements data persistence with ROOM database to store weather details
class MainViewModel(private val weatherRepository: WeatherRepository) : ViewModel() {
    private val _hasGetCurrentWeatherDetailsFailed: MutableLiveData<Boolean> = MutableLiveData()
    val hasGetCurrentWeatherDetailsFailed: LiveData<Boolean>
        get() = _hasGetCurrentWeatherDetailsFailed
    private val _weather: MutableLiveData<Weather> = MutableLiveData()
    val weather: LiveData<Weather>
        get() = _weather

    fun getWeatherDetailsFromLocation(location: Location) = viewModelScope.launch {
        try {
            val coordinates = Coordinates(location.latitude, location.longitude)
            weatherRepository
                    .getWeatherDetailsFromCoordinates(coordinates)
                    .let {
                        _weather.value = it
                        _hasGetCurrentWeatherDetailsFailed.value = false
                    }
        } catch (e: Exception) {
            _hasGetCurrentWeatherDetailsFailed.value = true
        }
    }
}