package com.powilliam.simpleweatherapp

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.powilliam.simpleweatherapp.databinding.ActivityMainBinding
import com.powilliam.simpleweatherapp.models.Weather
import com.powilliam.simpleweatherapp.repositories.WeatherRepository
import com.powilliam.simpleweatherapp.services.WeatherService

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val weatherService = WeatherService.create()
        val weatherRepository = WeatherRepository(weatherService)
        viewModel = ViewModelProvider(this, MainViewModelFactory(weatherRepository))
            .get(MainViewModel::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        observeWeather()
        observeHasGetCurrentWeatherDetailsFailed()
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

    // TODO Verify if GPS Service is active before request current location
    private fun getWeatherDetailsFromCurrentLocation() {
        when {
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
                    Toast.makeText(this,
                            getString(R.string.toast_text), Toast.LENGTH_SHORT)
                            .show()
                    viewModel.getWeatherDetailsFromLocation(it)
                }
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                    || shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                AlertDialog
                        .Builder(this)
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

    private fun observeWeather() {
        viewModel.weather.observe(this, { weather: Weather? ->
            weather?.let {
                binding.temperature.apply {
                    text = getString(R.string.temperature).format(it.main.temp)
                }
            }
        })
    }

    private fun observeHasGetCurrentWeatherDetailsFailed() {
        viewModel.hasGetCurrentWeatherDetailsFailed.observe(this, {
            when (it) {
                true -> Toast.makeText(this,
                        getString(R.string.weather_service_unavailable), Toast.LENGTH_SHORT)
                        .show()
                else -> {}
            }
        })
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