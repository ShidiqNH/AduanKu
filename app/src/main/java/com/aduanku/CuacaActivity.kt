package com.aduanku

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class CuacaActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cuaca)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check if the location permission is granted
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Fetch location if permission is granted
            fetchLocation()
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    // Fetch the device's current location (latitude and longitude)
    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                // Use the latitude and longitude for fetching weather data
                val lat = location.latitude
                val lon = location.longitude
                // Fetch weather data
                fetchCurrentWeather(lat, lon)
                fetchForecast(lat, lon)
            } else {
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fetch current weather
    private fun fetchCurrentWeather(lat: Double, lon: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(WeatherApiService::class.java)
        apiService.getCurrentWeather(lat, lon, "metric", "2b1f8eaa3c73f756bd524b22a3117429").enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherData = response.body()!!
                    updateCurrentWeatherUI(weatherData)
                } else {
                    Toast.makeText(this@CuacaActivity, "Failed to fetch weather data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(this@CuacaActivity, "Error connecting to the server", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Update UI for current weather
    private fun updateCurrentWeatherUI(weatherData: WeatherResponse) {
        val currentWeatherTextView: TextView = findViewById(R.id.currentWeather)
        val descriptionTextView: TextView = findViewById(R.id.todaysHighlights)
        val tempTextView: TextView = findViewById(R.id.dateLocation)
        val iconImageView: ImageView = findViewById(R.id.icon1)

        currentWeatherTextView.text = "${weatherData.main.temp}°C ${weatherData.weather[0].description}"
        descriptionTextView.text = "Wind Speed: ${weatherData.wind.speed} km/h"
        tempTextView.text = "Location: ${weatherData.name}"

        // Load weather icon
        val iconUrl = "https://openweathermap.org/img/wn/${weatherData.weather[0].icon}.png"
        Glide.with(this).load(iconUrl).into(iconImageView)
    }

    // Fetch 5-hour forecast
    private fun fetchForecast(lat: Double, lon: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(WeatherApiService::class.java)
        apiService.get5HourForecast(lat, lon, "metric", "2b1f8eaa3c73f756bd524b22a3117429").enqueue(object : Callback<ForecastResponse> {
            override fun onResponse(call: Call<ForecastResponse>, response: Response<ForecastResponse>) {
                Log.d("API_CALL", "onResponse: Response received")

                if (response.isSuccessful) {
                    Log.d("API_CALL", "onResponse: Success - ${response.body()}")
                    val forecastData = response.body()!!
                    updateForecastUI(forecastData)
                } else {
                    Log.e("API_ERROR", "onResponse: Failed - Error code: ${response.code()}, Message: ${response.message()}")
                    Toast.makeText(this@CuacaActivity, "Failed to fetch forecast data. Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                Log.e("API_ERROR", "onFailure: Request failed - ${t.localizedMessage}")
                Toast.makeText(this@CuacaActivity, "Error connecting to the server", Toast.LENGTH_SHORT).show()
            }
        })
    }


    // Update UI for forecast
    private fun updateForecastUI(forecastData: ForecastResponse) {
        val forecast = forecastData.list.take(5)

        val timeViews = listOf(
            findViewById<TextView>(R.id.time1),
            findViewById<TextView>(R.id.time2),
            findViewById<TextView>(R.id.time3),
            findViewById<TextView>(R.id.time4),
            findViewById<TextView>(R.id.time5)
        )
        val descriptionViews = listOf(
            findViewById<TextView>(R.id.description1),
            findViewById<TextView>(R.id.description2),
            findViewById<TextView>(R.id.description3),
            findViewById<TextView>(R.id.description4),
            findViewById<TextView>(R.id.description5)
        )
        val iconViews = listOf(
            findViewById<ImageView>(R.id.icon1),
            findViewById<ImageView>(R.id.icon2),
            findViewById<ImageView>(R.id.icon3),
            findViewById<ImageView>(R.id.icon4),
            findViewById<ImageView>(R.id.icon5)
        )

        for (i in forecast.indices) {
            val forecastItem = forecast[i]
            val time = forecastItem.dt_txt.substring(11, 16)
            timeViews[i].text = time
            descriptionViews[i].text = forecastItem.weather[0].description

            val iconUrl = "https://openweathermap.org/img/wn/${forecastItem.weather[0].icon}.png"
            Glide.with(this).load(iconUrl).into(iconViews[i])
        }
    }

    // Handle location permission result
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation()  // Fetch location if permission is granted
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Define constant for location permission request code
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}

// Retrofit API interface
interface WeatherApiService {
    @GET("weather")
    fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String,
        @Query("appid") appId: String
    ): Call<WeatherResponse>

    @GET("forecast")
    fun get5HourForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String,
        @Query("appid") appId: String
    ): Call<ForecastResponse>
}

// Data models for weather and forecast
data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val name: String
)

data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Double,
    val humidity: Int
)

data class Weather(
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double,
    val deg: Int,
    val gust: Double
)

data class ForecastResponse(
    val list: List<ForecastItem>
)

data class ForecastItem(
    val dt_txt: String,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind
)
