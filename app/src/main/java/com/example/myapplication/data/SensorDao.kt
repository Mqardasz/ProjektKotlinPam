package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorDao {
    @Query("SELECT * FROM sensor_measurements ORDER BY timestamp DESC")
    fun getAllMeasurements(): Flow<List<SensorMeasurement>>
    
    @Query("SELECT * FROM sensor_measurements WHERE sensorType = :type ORDER BY timestamp DESC")
    fun getMeasurementsByType(type: String): Flow<List<SensorMeasurement>>
    
    @Query("SELECT * FROM sensor_measurements WHERE id = :id")
    suspend fun getMeasurementById(id: Long): SensorMeasurement?
    
    @Insert
    suspend fun insertMeasurement(measurement: SensorMeasurement): Long
    
    @Delete
    suspend fun deleteMeasurement(measurement: SensorMeasurement)
    
    @Query("DELETE FROM sensor_measurements")
    suspend fun deleteAllMeasurements()
    
    @Query("SELECT COUNT(*) FROM sensor_measurements")
    fun getMeasurementCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM sensor_measurements WHERE sensorType = :type")
    fun getMeasurementCountByType(type: String): Flow<Int>
}
