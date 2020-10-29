package com.powilliam.simpleweatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import com.powilliam.simpleweatherapp.databinding.ActivityMainBinding
import com.powilliam.simpleweatherapp.models.Weather
import com.powilliam.simpleweatherapp.repositories.WeatherRepository
import com.powilliam.simpleweatherapp.services.WeatherService

// TODO Handle network errors and denied permissions to avoid app crashing
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val weatherService = WeatherService.create()
        val weatherRepository = WeatherRepository(weatherService)
        viewModel = ViewModelProvider(this, MainViewModelFactory(weatherRepository))
            .get(MainViewModel::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest
            .create()
            .apply {
                interval = 1000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    viewModel.updateCoordinatesWith(location.latitude, location.longitude)
                }
            }
        }
        when {
            hasGrantedPermissions() -> {
                getWeatherDetailsFromLastKnownLocation()
                startRequestLocationUpdates()
            }
            else -> {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION)
            }
        }
        observeIsLoadingWeatherDetails()
        observeWeather()
        setRefreshButtonClickListener()
    }

    override fun onResume() {
        super.onResume()
        getWeatherDetailsFromLastKnownLocation()
        startRequestLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopRequestLocationUpdates()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    getWeatherDetailsFromLastKnownLocation()
                    startRequestLocationUpdates()
                }
                return
            }
        }
    }

    private fun hasGrantedPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun startRequestLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun stopRequestLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun getWeatherDetailsFromLastKnownLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                viewModel.getWeatherDetailsFromLastKnownLocation(it)
            }
        }
    }

    private fun observeIsLoadingWeatherDetails() {
        viewModel.isLoadingWeatherDetails.observe(this, {
                isLoadingWeatherDetails: Boolean? ->
            isLoadingWeatherDetails.let {
                when(it) {
                    true -> {
                        binding.temperature.visibility = View.GONE
                        binding.loadingAnimation.visibility = View.VISIBLE
                        binding.refreshButton.hide()
                    }
                    else -> {
                        binding.temperature.visibility = View.VISIBLE
                        binding.loadingAnimation.visibility = View.GONE
                        binding.refreshButton.show()
                    }
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun observeWeather() {
        viewModel.weather.observe(this, { weather: Weather? ->
            weather?.let {
                binding.temperature.apply {
                    text = "${weather.main.temp}°C"
                }
            }
        })
    }

    private fun setRefreshButtonClickListener() {
        binding.refreshButton.setOnClickListener {
            viewModel.getCurrentWeatherDetails()
        }
    }

    companion object {
        const val LOCATION_PERMISSION = 1
    }
}