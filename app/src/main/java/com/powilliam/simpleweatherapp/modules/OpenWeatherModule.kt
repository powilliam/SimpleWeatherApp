package com.powilliam.simpleweatherapp.modules

import com.powilliam.simpleweatherapp.services.OpenWeatherService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object OpenWeatherModule {
    @Singleton
    @Provides
    fun provideOpenWeatherService(): OpenWeatherService {
        return OpenWeatherService
            .create()
    }
}