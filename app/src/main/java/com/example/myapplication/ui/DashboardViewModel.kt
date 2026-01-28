package com.example.myapplication.ui

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.SensorMeasurement
import com.example.myapplication.data.SensorRepository
import com.example.myapplication.sensors.AccelerometerData
import com.example.myapplication.sensors.AccelerometerManager
import com.example.myapplication.sensors.LocationManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: SensorRepository,
    private val context: Context
) : ViewModel() {
    
    private val locationManager = LocationManager(context)
    private val accelerometerManager = AccelerometerManager(context)
    
    val measurements: StateFlow<List<SensorMeasurement>> = repository.allMeasurements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val gpsCount: StateFlow<Int> = repository.getMeasurementCountByType("GPS")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    val accelerometerCount: StateFlow<Int> = repository.getMeasurementCountByType("ACCELEROMETER")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    val totalCount: StateFlow<Int> = repository.getMeasurementCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    fun startLocationTracking(): Flow<Location> {
        return locationManager.getLocationUpdates()
    }
    
    fun startAccelerometerTracking(): Flow<AccelerometerData> {
        return accelerometerManager.getAccelerometerUpdates()
    }
    
    suspend fun saveGPSMeasurement(latitude: Double, longitude: Double) {
        val measurement = SensorMeasurement(
            timestamp = System.currentTimeMillis(),
            sensorType = "GPS",
            latitude = latitude,
            longitude = longitude
        )
        repository.insertMeasurement(measurement)
    }
    
    suspend fun saveAccelerometerMeasurement(x: Float, y: Float, z: Float) {
        val measurement = SensorMeasurement(
            timestamp = System.currentTimeMillis(),
            sensorType = "ACCELEROMETER",
            accelerationX = x,
            accelerationY = y,
            accelerationZ = z
        )
        repository.insertMeasurement(measurement)
    }
    
    suspend fun deleteMeasurement(measurement: SensorMeasurement) {
        repository.deleteMeasurement(measurement)
    }
    
    suspend fun deleteAllMeasurements() {
        repository.deleteAllMeasurements()
    }
}

class DashboardViewModelFactory(
    private val repository: SensorRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
