package com.example.myapplication.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.SensorMeasurement
import com.example.myapplication.data.SensorRepository
import com.example.myapplication.sensors.AccelerometerData
import com.example.myapplication.sensors.AccelerometerManager
import com.example.myapplication.sensors.LocationManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    repository: SensorRepository,
    onNavigateToHistory: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(repository, context)
    )
    
    val measurements by viewModel.measurements.collectAsState()
    val gpsCount by viewModel.gpsCount.collectAsState()
    val accelerometerCount by viewModel.accelerometerCount.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    var isCollectingLocation by remember { mutableStateOf(false) }
    var isCollectingAccelerometer by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var currentAcceleration by remember { mutableStateOf<AccelerometerData?>(null) }
    
    LaunchedEffect(isCollectingLocation) {
        if (isCollectingLocation && permissionsState.allPermissionsGranted) {
            viewModel.startLocationTracking().collect { location ->
                currentLocation = location
            }
        }
    }
    
    LaunchedEffect(isCollectingAccelerometer) {
        if (isCollectingAccelerometer) {
            viewModel.startAccelerometerTracking().collect { data ->
                currentAcceleration = data
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Sensorów") },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "Historia")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Statistics cards
            item {
                Text(
                    text = "Statystyki",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "GPS",
                        count = gpsCount,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Akcelerometr",
                        count = accelerometerCount,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Razem",
                        count = totalCount,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // GPS Section
            item {
                Text(
                    text = "Lokalizacja GPS",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!permissionsState.allPermissionsGranted) {
                            Text("Wymagane uprawnienia do lokalizacji")
                            Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
                                Text("Przyznaj uprawnienia")
                            }
                        } else {
                            currentLocation?.let { location ->
                                Text("Szerokość: ${location.latitude}")
                                Text("Długość: ${location.longitude}")
                            } ?: Text("Brak danych lokalizacji")
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        isCollectingLocation = !isCollectingLocation
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        if (isCollectingLocation) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = null
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (isCollectingLocation) "Stop" else "Start")
                                }
                                
                                Button(
                                    onClick = {
                                        currentLocation?.let {
                                            scope.launch {
                                                viewModel.saveGPSMeasurement(it.latitude, it.longitude)
                                            }
                                        }
                                    },
                                    enabled = currentLocation != null,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Zapisz")
                                }
                            }
                        }
                    }
                }
            }
            
            // Accelerometer Section
            item {
                Text(
                    text = "Akcelerometr",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentAcceleration?.let { data ->
                            Text("X: ${"%.2f".format(data.x)} m/s²")
                            Text("Y: ${"%.2f".format(data.y)} m/s²")
                            Text("Z: ${"%.2f".format(data.z)} m/s²")
                        } ?: Text("Brak danych akcelerometru")
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    isCollectingAccelerometer = !isCollectingAccelerometer
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    if (isCollectingAccelerometer) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(if (isCollectingAccelerometer) "Stop" else "Start")
                            }
                            
                            Button(
                                onClick = {
                                    currentAcceleration?.let {
                                        scope.launch {
                                            viewModel.saveAccelerometerMeasurement(it.x, it.y, it.z)
                                        }
                                    }
                                },
                                enabled = currentAcceleration != null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Zapisz")
                            }
                        }
                    }
                }
            }
            
            // Recent measurements
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ostatnie pomiary",
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (measurements.isNotEmpty()) {
                        TextButton(onClick = {
                            scope.launch {
                                viewModel.deleteAllMeasurements()
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Usuń wszystko")
                        }
                    }
                }
            }
            
            if (measurements.isEmpty()) {
                item {
                    Text(
                        text = "Brak pomiarów",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(measurements.take(5)) { measurement ->
                    MeasurementCard(
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

@Composable
fun StatCard(
    title: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
fun MeasurementCard(
    measurement: SensorMeasurement,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = measurement.sensorType,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                        .format(Date(measurement.timestamp)),
                    style = MaterialTheme.typography.bodySmall
                )
                when (measurement.sensorType) {
                    "GPS" -> {
                        measurement.latitude?.let { lat ->
                            measurement.longitude?.let { lon ->
                                Text("Lat: ${"%.4f".format(lat)}, Lon: ${"%.4f".format(lon)}")
                            }
                        }
                    }
                    "ACCELEROMETER" -> {
                        Text("X: ${"%.2f".format(measurement.accelerationX)}, " +
                                "Y: ${"%.2f".format(measurement.accelerationY)}, " +
                                "Z: ${"%.2f".format(measurement.accelerationZ)}")
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Usuń")
            }
        }
    }
}
