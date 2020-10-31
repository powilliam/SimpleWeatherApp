package com.powilliam.simpleweatherapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.powilliam.simpleweatherapp.usecases.GetWeatherDetailsFromLocationUseCase
import java.security.InvalidParameterException

class MainViewModelFactory(
        private val getWeatherDetailsFromLocationUseCase: GetWeatherDetailsFromLocationUseCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                    getWeatherDetailsFromLocationUseCase,
            ) as T
        }
        else {
            throw InvalidParameterException("MainViewModel not found")
        }
    }
}