package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor_measurements")
data class SensorMeasurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val sensorType: String, // "GPS", "ACCELEROMETER", "CAMERA"
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accelerationX: Float? = null,
    val accelerationY: Float? = null,
    val accelerationZ: Float? = null,
    val photoPath: String? = null,
    val notes: String? = null
)
