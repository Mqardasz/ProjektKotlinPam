package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.SensorMeasurement
import com.example.myapplication.data.SensorRepository
import kotlinx.coroutines.flow.*

class HistoryViewModel(
    private val repository: SensorRepository
) : ViewModel() {
    
    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()
    
    val measurements: StateFlow<List<SensorMeasurement>> = _selectedFilter
        .flatMapLatest { filter ->
            when (filter) {
                "ALL" -> repository.allMeasurements
                else -> repository.getMeasurementsByType(filter)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }
    
    suspend fun deleteMeasurement(measurement: SensorMeasurement) {
        repository.deleteMeasurement(measurement)
    }
    
    suspend fun deleteAllMeasurements() {
        repository.deleteAllMeasurements()
    }
}

class HistoryViewModelFactory(
    private val repository: SensorRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
