package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.SensorMeasurement
import com.example.myapplication.data.SensorRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    repository: SensorRepository,
    onNavigateBack: () -> Unit
) {
    val viewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModelFactory(repository)
    )
    
    val measurements by viewModel.measurements.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historia Pomiarów") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz")
                    }
                },
                actions = {
                    if (measurements.isNotEmpty()) {
                        IconButton(onClick = {
                            scope.launch {
                                viewModel.deleteAllMeasurements()
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Usuń wszystko")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { viewModel.setFilter("ALL") },
                    label = { Text("Wszystkie") }
                )
                FilterChip(
                    selected = selectedFilter == "GPS",
                    onClick = { viewModel.setFilter("GPS") },
                    label = { Text("GPS") }
                )
                FilterChip(
                    selected = selectedFilter == "ACCELEROMETER",
                    onClick = { viewModel.setFilter("ACCELEROMETER") },
                    label = { Text("Akcelerometr") }
                )
            }
            
            Divider()
            
            if (measurements.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Brak pomiarów",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(measurements, key = { it.id }) { measurement ->
                        MeasurementDetailCard(
                            measurement = measurement,
                            onDelete = {
                                scope.launch {
                                    viewModel.deleteMeasurement(measurement)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MeasurementDetailCard(
    measurement: SensorMeasurement,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        when (measurement.sensorType) {
                            "GPS" -> Icons.Default.LocationOn
                            "ACCELEROMETER" -> Icons.Default.Speed
                            else -> Icons.Default.Sensors
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = measurement.sensorType,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale("pl", "PL"))
                                .format(Date(measurement.timestamp)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Usuń",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Divider()
            
            when (measurement.sensorType) {
                "GPS" -> {
                    measurement.latitude?.let { lat ->
                        measurement.longitude?.let { lon ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                DetailRow("Szerokość geograficzna", "${"%.6f".format(lat)}°")
                                DetailRow("Długość geograficzna", "${"%.6f".format(lon)}°")
                            }
                        }
                    }
                }
                "ACCELEROMETER" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        DetailRow("Oś X", "${"%.3f".format(measurement.accelerationX)} m/s²")
                        DetailRow("Oś Y", "${"%.3f".format(measurement.accelerationY)} m/s²")
                        DetailRow("Oś Z", "${"%.3f".format(measurement.accelerationZ)} m/s²")
                        
                        val magnitude = kotlin.math.sqrt(
                            (measurement.accelerationX ?: 0f).let { it * it } +
                            (measurement.accelerationY ?: 0f).let { it * it } +
                            (measurement.accelerationZ ?: 0f).let { it * it }
                        )
                        DetailRow("Magnituda", "${"%.3f".format(magnitude)} m/s²")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
