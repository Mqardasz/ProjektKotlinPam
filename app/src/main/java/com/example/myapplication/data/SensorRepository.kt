package com.example.myapplication.data

import kotlinx.coroutines.flow.Flow

class SensorRepository(private val sensorDao: SensorDao) {
    
    val allMeasurements: Flow<List<SensorMeasurement>> = sensorDao.getAllMeasurements()
    
    fun getMeasurementsByType(type: String): Flow<List<SensorMeasurement>> {
        return sensorDao.getMeasurementsByType(type)
    }
    
    suspend fun getMeasurementById(id: Long): SensorMeasurement? {
        return sensorDao.getMeasurementById(id)
    }
    
    suspend fun insertMeasurement(measurement: SensorMeasurement): Long {
        return sensorDao.insertMeasurement(measurement)
    }
    
    suspend fun deleteMeasurement(measurement: SensorMeasurement) {
        sensorDao.deleteMeasurement(measurement)
    }
    
    suspend fun deleteAllMeasurements() {
        sensorDao.deleteAllMeasurements()
    }
    
    fun getMeasurementCount(): Flow<Int> {
        return sensorDao.getMeasurementCount()
    }
    
    fun getMeasurementCountByType(type: String): Flow<Int> {
        return sensorDao.getMeasurementCountByType(type)
    }
}
