package com.powilliam.simpleweatherapp

import android.location.Location
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.powilliam.simpleweatherapp.models.Weather
import com.powilliam.simpleweatherapp.usecases.GetWeatherDetailsFromLocationUseCase
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(
        private val getWeatherDetailsFromLocationUseCase: GetWeatherDetailsFromLocationUseCase,
) : ViewModel() {
    private val _weather: MutableLiveData<Weather> = MutableLiveData()
    val weather: LiveData<Weather>
        get() = _weather
    private val _isGettingWeatherDetails: MutableLiveData<Boolean> = MutableLiveData()
    val isGettingWeatherDetails: LiveData<Boolean>
        get() = _isGettingWeatherDetails

    fun getWeatherDetailsFromCurrentLocation(location: Location) = viewModelScope.launch {
        _isGettingWeatherDetails.value = true
        getWeatherDetailsFromLocationUseCase
                .execute(location)
                .let { _weather.value = it }
        _isGettingWeatherDetails.value = false
    }

    fun onHavingInternetOrProvidersUnavailable() {
        _isGettingWeatherDetails.value = false
    }
}