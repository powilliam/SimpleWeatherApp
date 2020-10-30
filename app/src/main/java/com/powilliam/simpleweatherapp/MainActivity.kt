package com.powilliam.simpleweatherapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.powilliam.simpleweatherapp.databinding.ActivityMainBinding
import com.powilliam.simpleweatherapp.models.Weather
import com.powilliam.simpleweatherapp.repositories.WeatherRepository
import com.powilliam.simpleweatherapp.services.OpenWeatherService

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val weatherService = OpenWeatherService.create()
        val weatherRepository = WeatherRepository(weatherService)
        viewModel = ViewModelProvider(this, MainViewModelFactory(weatherRepository))
            .get(MainViewModel::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        observeWeather()
        observeIsGettingWeatherDetails()
        setRefreshButtonClickListener()
        getWeatherDetailsFromCurrentLocation()
    }

    override fun onResume() {
        super.onResume()
        getWeatherDetailsFromCurrentLocation()
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
                    getWeatherDetailsFromCurrentLocation()
                }
                return
            }
        }
    }

    // TODO Implements a fastest way to get the current location
    //  it could be done using a cached location
    //  and letting the user choose when they wants to refresh it
    private fun getWeatherDetailsFromCurrentLocation() {
        when {
            !hasInternetConnection() -> {
                Snackbar.make(binding.coordinatorLayout,
                        R.string.weather_service_unavailable, Snackbar.LENGTH_LONG)
                        .show()
            }
            !hasLocationProvidersEnabled() -> {
                Snackbar.make(binding.coordinatorLayout,
                        R.string.location_service_unavailable, Snackbar.LENGTH_LONG)
                        .show()
            }
            ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            -> {
                val location = fusedLocationClient.getCurrentLocation(
                        LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
                        object : CancellationToken() {
                            override fun isCancellationRequested(): Boolean {
                                return false
                            }
                            override fun onCanceledRequested(
                                    p0: OnTokenCanceledListener): CancellationToken {
                                return this
                            }
                        })
                location.addOnSuccessListener {
                    viewModel.getWeatherDetailsFromLocation(it)
                }
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                    || shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.permission_alert_dialog_title))
                        .setMessage(getString(R.string.permission_alert_dialog_message))
                        .setPositiveButton(R.string.permission_alert_positive_button) { _, _ ->
                            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION)
                        }
                        .show()
            }
            else -> {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION)
            }
        }
    }

    private fun hasInternetConnection(): Boolean {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return manager.activeNetworkInfo != null
                && manager.activeNetworkInfo!!.isConnected
    }

    private fun hasLocationProvidersEnabled(): Boolean {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun observeWeather() {
        viewModel.weather.observe(this) { weather: Weather? ->
            weather?.let {
                binding.temperature.text = getString(R.string.temperature).format(it.main.temp)
                binding.thermalSensation.text = getString(R.string.thermal_sensation)
                        .format(it.main.feels_like)
            }
        }
    }

    private fun observeIsGettingWeatherDetails() {
        viewModel.isGettingWeatherDetails.observe(this) {
            isGettingWeatherDetails: Boolean? ->
            when (isGettingWeatherDetails) {
                true -> {
                    binding.temperature.visibility = View.GONE
                    binding.thermalSensation.visibility = View.GONE
                    binding.loadingAnimation.visibility = View.VISIBLE
                }
                else -> {
                    binding.temperature.visibility = View.VISIBLE
                    binding.thermalSensation.visibility = View.VISIBLE
                    binding.loadingAnimation.visibility = View.GONE
                }
            }
        }
    }

    private fun setRefreshButtonClickListener() {
        binding.refreshButton.setOnClickListener {
            getWeatherDetailsFromCurrentLocation()
        }
    }

    companion object {
        const val LOCATION_PERMISSION = 1
    }
}