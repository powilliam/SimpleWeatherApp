package com.powilliam.simpleweatherapp.models

data class Weather(
    val main: Details
) {
    data class Details(
        val temp: Double,
        val feels_like: Double,
        val temp_min: Double,
        val temp_max: Double,
        val pressure: Double,
        val humidity: Double
    )
}