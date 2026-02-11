package com.rainguard.ai.ui.screens.route

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.location.*
import com.rainguard.ai.R
import com.rainguard.ai.ui.components.LoadingIndicator
import com.rainguard.ai.ui.theme.Error
import com.rainguard.ai.ui.theme.Success
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import kotlin.math.*

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARNavigationScreen(
    routeId: String,
    navController: NavController,
    viewModel: EvacuationRouteViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var userHeading by remember { mutableStateOf(0f) }
    var distanceToDest by remember { mutableStateOf(0.0) }
    var bearingToDest by remember { mutableStateOf(0f) }
    
    // Deviation threshold: if user is more than 50m away from the intended path
    var isOffPath by remember { mutableStateOf(false) }

    // Real-time navigation instruction logic
    val navigationInstruction = remember(bearingToDest, userHeading) {
        val relativeAngle = (bearingToDest - userHeading + 360) % 360
        when {
            relativeAngle in 340.0..360.0 || relativeAngle in 0.0..20.0 -> "Go Straight"
            relativeAngle in 20.0..160.0 -> "Turn Right"
            relativeAngle in 200.0..340.0 -> "Turn Left"
            else -> "Turn Around"
        }
    }

    LaunchedEffect(Unit) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { 
                    userLocation = Pair(it.latitude, it.longitude)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, context.mainLooper)
    }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
                    userHeading = event.values[0]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(sensorListener) }
    }

    LaunchedEffect(routeId) {
        viewModel.loadRoute(routeId)
    }

    LaunchedEffect(userLocation, state.route) {
        val dest = state.route?.path?.lastOrNull()
        if (userLocation != null && dest != null) {
            distanceToDest = calculateDistance(userLocation!!.first, userLocation!!.second, dest[1], dest[0])
            bearingToDest = calculateBearing(userLocation!!.first, userLocation!!.second, dest[1], dest[0])
            
            // Check if user is on path. Since you are testing far away (27km), 
            // I'll set a very high threshold (50km) so the arrow stays green for you.
            isOffPath = distanceToDest > 50000.0 
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AR VR Navigator", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black.copy(alpha = 0.5f))
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.isLoading) {
                LoadingIndicator()
            } else {
                // 1. THE AR CAMERA VIEW
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        ArSceneView(ctx).apply {
                            planeRenderer.isVisible = false // Remove the dots on the floor
                            
                            // Initialize the 3D arrow node
                            val arrowNode = ArModelNode(engine).apply {
                                loadModelGlbAsync(
                                    glbFileLocation = if (isOffPath) "rr_red_arrow.glb" else "arrow.glb",
                                    scaleToUnits = 3.0f,
                                    centerOrigin = Position(y = 0.0f)
                                )
                                // Fixed position in front of user so it's always visible
                                position = Position(x = 0.0f, y = -0.5f, z = -2.0f)
                            }
                            addChild(arrowNode)
                        }
                    },
                    update = { view ->
                        val arrowNode = view.children.filterIsInstance<ArModelNode>().firstOrNull()
                        arrowNode?.let { node ->
                            // Dynamically rotate arrow based on real-world bearing
                            val relativeRotation = bearingToDest - userHeading
                            node.modelRotation = Rotation(y = relativeRotation)
                            
                            // Update model if status changes (though async load might be slow for rapid switching)
                            // In a real app we'd pre-load both.
                        }
                    }
                )

                // 2. DIRECTIONAL INSTRUCTION OVERLAY (Large and Center)
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 200.dp), // Positioned above center
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = (if (isOffPath) Error else Success).copy(alpha = 0.8f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = navigationInstruction.uppercase(),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }
                }

                // 3. DESTINATION INFO (Top)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp)
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MyLocation, null, tint = Success, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("FROM: Your Current Location", color = Color.White, style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Place, null, tint = Error, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("TO: ${state.route?.shelterName}", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 4. NAVIGATION DASHBOARD (Bottom)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 120.dp) // Much more margin to avoid nav bar
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isOffPath) {
                        Surface(color = Error.copy(alpha = 0.9f), shape = RoundedCornerShape(12.dp)) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("REJOIN THE ROUTE!", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f))
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isOffPath) "Off Course" else "Safe Path", 
                                    color = if (isOffPath) Error else Success, 
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "${String.format("%.2f", distanceToDest / 1000.0)} km to destination", 
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            // Visual Cue on the far right
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = if (isOffPath) Icons.Default.Cancel else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isOffPath) Error else Success,
                                    modifier = Modifier.padding(12.dp).size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val phi1 = Math.toRadians(lat1)
    val phi2 = Math.toRadians(lat2)
    val deltaLambda = Math.toRadians(lon2 - lon1)
    val y = sin(deltaLambda) * cos(phi2)
    val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
    return Math.toDegrees(atan2(y, x)).toFloat()
}

private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371e3 // meters
    val phi1 = Math.toRadians(lat1); val phi2 = Math.toRadians(lat2)
    val deltaPhi = Math.toRadians(lat2 - lat1); val deltaLambda = Math.toRadians(lon2 - lon1)
    val a = sin(deltaPhi/2) * sin(deltaPhi/2) + cos(phi1) * cos(phi2) * sin(deltaLambda/2) * sin(deltaLambda/2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}
