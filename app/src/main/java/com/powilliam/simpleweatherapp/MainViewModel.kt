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

class MainViewModel(private val weatherRepository: WeatherRepository) : ViewModel() {
    private var coordinates: MutableLiveData<Coordinates> = MutableLiveData()
    private var _weather: MutableLiveData<Weather> = MutableLiveData()
    val weather: LiveData<Weather>
        get() = _weather
    private var _isLoadingWeatherDetails: MutableLiveData<Boolean> = MutableLiveData()
    val isLoadingWeatherDetails: LiveData<Boolean>
        get() = _isLoadingWeatherDetails

    init {
        _isLoadingWeatherDetails.value = true
    }

    fun updateCoordinatesWith(latitude: Double, longitude: Double) {
        coordinates.value = Coordinates(latitude, longitude)
    }

    fun getCurrentWeatherDetails() = viewModelScope.launch {
        _isLoadingWeatherDetails.value = true
        weatherRepository
            .getCurrentWeatherDetails(coordinates.value!!)
            .let { _weather.value = it }
        _isLoadingWeatherDetails.value = false
    }

    fun getWeatherDetailsFromLastKnownLocation(location: Location) = viewModelScope.launch {
        _isLoadingWeatherDetails.value = true
        val coordinates = Coordinates(location.latitude, location.longitude)
        weatherRepository
            .getCurrentWeatherDetails(coordinates)
            .let { _weather.value = it }
        _isLoadingWeatherDetails.value = false
    }
}