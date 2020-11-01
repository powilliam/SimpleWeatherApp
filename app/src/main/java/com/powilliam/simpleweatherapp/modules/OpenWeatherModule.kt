package com.powilliam.simpleweatherapp.modules

import com.powilliam.simpleweatherapp.services.OpenWeatherService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Singleton

@Module
@InstallIn(ActivityComponent::class)
object OpenWeatherModule {
    @Provides
    fun provideOpenWeatherService(): OpenWeatherService {
        return OpenWeatherService
            .create()
    }
}