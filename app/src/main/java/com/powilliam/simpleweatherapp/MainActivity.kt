package com.powilliam.simpleweatherapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.powilliam.simpleweatherapp.databinding.ActivityMainBinding
import com.powilliam.simpleweatherapp.models.Weather
import com.powilliam.simpleweatherapp.services.OpenWeatherService
import com.powilliam.simpleweatherapp.usecases.GetWeatherDetailsFromLocationUseCase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val weatherService = OpenWeatherService.create()
        val getWeatherDetailsFromLocationUseCase =
                GetWeatherDetailsFromLocationUseCase(weatherService)
        val viewModelFactory = MainViewModelFactory(
                getWeatherDetailsFromLocationUseCase,
        )
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(MainViewModel::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStart() {
        super.onStart()
        observeWeather()
        observeIsGettingWeatherDetails()
        setRefreshButtonClickListener()
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

    private fun hasInternetConnection(): Boolean {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return manager.activeNetwork != null
    }

    private fun hasLocationProvidersEnabled(): Boolean {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun getWeatherDetailsFromCurrentLocation() {
        when {
            !hasInternetConnection() -> {
                Snackbar.make(binding.coordinatorLayout,
                        R.string.internet_unavailable, Snackbar.LENGTH_LONG)
                        .show()
            }
            !hasLocationProvidersEnabled() -> {
                Snackbar.make(binding.coordinatorLayout,
                        R.string.location_service_unavailable, Snackbar.LENGTH_LONG)
                        .show()
            }
            ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                val location = fusedLocationClient.getCurrentLocation(
                        LocationRequest.PRIORITY_HIGH_ACCURACY,
                        object : CancellationToken() {
                            override fun isCancellationRequested(): Boolean {
                                return false
                            }
                            override fun onCanceledRequested(
                                    p0: OnTokenCanceledListener
                            ): CancellationToken {
                                return this
                            }
                        }
                )
                location.addOnSuccessListener {
                    viewModel.getWeatherDetailsFromCurrentLocation(it)
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
                    binding.loadingAnimation.playAnimation()
                }
                else -> {
                    binding.temperature.visibility = View.VISIBLE
                    binding.thermalSensation.visibility = View.VISIBLE
                    binding.loadingAnimation.visibility = View.GONE
                    binding.loadingAnimation.pauseAnimation()
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