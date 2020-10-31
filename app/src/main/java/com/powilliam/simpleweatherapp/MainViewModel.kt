package com.powilliam.simpleweatherapp

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powilliam.simpleweatherapp.models.Weather
import com.powilliam.simpleweatherapp.usecases.GetWeatherDetailsFromLocationUseCase
import kotlinx.coroutines.launch

class MainViewModel(
        private val getWeatherDetailsFromLocationUseCase: GetWeatherDetailsFromLocationUseCase,
) : ViewModel() {
    private val _weather: MutableLiveData<Weather> = MutableLiveData()
    val weather: LiveData<Weather>
        get() = _weather
    private val _isGettingWeatherDetails: MutableLiveData<Boolean> = MutableLiveData()
    val isGettingWeatherDetails: LiveData<Boolean>
        get() = _isGettingWeatherDetails

    init {
        _isGettingWeatherDetails.value = true
    }

    fun getWeatherDetailsFromCurrentLocation(location: Location) = viewModelScope.launch {
        _isGettingWeatherDetails.value = true
        getWeatherDetailsFromLocationUseCase
                .execute(location)
                .let { _weather.value = it }
        _isGettingWeatherDetails.value = false
    }
}