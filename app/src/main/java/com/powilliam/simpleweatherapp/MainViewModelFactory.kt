package com.powilliam.simpleweatherapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.powilliam.simpleweatherapp.repositories.WeatherRepository
import java.security.InvalidParameterException

class MainViewModelFactory(private val weatherRepository: WeatherRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(weatherRepository) as T
        }
        else {
            throw InvalidParameterException("MainViewModel not found")
        }
    }
}