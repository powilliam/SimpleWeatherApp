package com.powilliam.simpleweatherapp

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powilliam.simpleweatherapp.models.Coordinates
import com.powilliam.simpleweatherapp.models.Weather
import com.powilliam.simpleweatherapp.repositories.WeatherRepository
import kotlinx.coroutines.launch

// TODO Implements data persistence with ROOM database to store weather details
class MainViewModel(private val weatherRepository: WeatherRepository) : ViewModel() {
    private val _weather: MutableLiveData<Weather> = MutableLiveData()
    val weather: LiveData<Weather>
        get() = _weather
    private val _isGettingWeatherDetails: MutableLiveData<Boolean> = MutableLiveData()
    val isGettingWeatherDetails: LiveData<Boolean>
        get() = _isGettingWeatherDetails

    fun getWeatherDetailsFromLocation(location: Location) = viewModelScope.launch {
        val coordinates = Coordinates(location.latitude, location.longitude)
        _isGettingWeatherDetails.value = true
        weatherRepository
                .getWeatherDetailsFromCoordinates(coordinates)
                .let { _weather.value = it }
        _isGettingWeatherDetails.value = false
    }
}