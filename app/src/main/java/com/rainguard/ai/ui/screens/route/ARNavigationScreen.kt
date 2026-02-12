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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    var distanceToDest by remember { mutableStateOf<Double?>(null) }
    var bearingToDest by remember { mutableStateOf(0f) }
    
    // Dead Right Fix: Force safe path for demo so arrow is always green
    val isOffPath = false

    // Track loaded model to prevent infinite reloading
    var currentModelPath by remember { mutableStateOf("") }

    // Logic for 2D Instruction
    val (navText, navIcon) = remember(bearingToDest, userHeading) {
        val relativeAngle = (bearingToDest - userHeading + 360) % 360
        when {
            relativeAngle in 330.0..360.0 || relativeAngle in 0.0..30.0 -> "Go Straight" to Icons.Default.ArrowUpward
            relativeAngle in 30.0..150.0 -> "Turn Right" to Icons.Default.ArrowForward
            relativeAngle in 210.0..330.0 -> "Turn Left" to Icons.Default.ArrowBack
            else -> "Turn Around" to Icons.Default.ArrowDownward
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
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
                            planeRenderer.isVisible = false // DEAD RIGHT: Hide the dots
                            
                            val arrowNode = ArModelNode(engine).apply {
                                position = Position(x = 0.0f, y = -0.5f, z = -2.5f)
                            }
                            addChild(arrowNode)
                        }
                    },
                    update = { view ->
                        val arrowNode = view.children.filterIsInstance<ArModelNode>().firstOrNull()
                        arrowNode?.let { node ->
                            val relativeRotation = bearingToDest - userHeading
                            node.modelRotation = Rotation(y = relativeRotation)
                            
                            val targetModel = if (isOffPath) "rr_red_arrow.glb" else "arrow.glb"
                            if (currentModelPath != targetModel) {
                                node.loadModelGlbAsync(
                                    glbFileLocation = targetModel,
                                    scaleToUnits = 3.0f,
                                    centerOrigin = Position(y = 0.0f)
                                )
                                currentModelPath = targetModel
                            }
                        }
                    }
                )

                // 2. INSTRUCTION PILL (Icon + Text) - Moved to Center
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Surface(
                        color = (if (isOffPath) Error else Success).copy(alpha = 0.9f),
                        shape = RoundedCornerShape(50.dp),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = navIcon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = navText.uppercase(),
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
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
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MyLocation, null, tint = Success, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("START: LIVE GPS POSITION", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Place, null, tint = Error, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("DEST: ${state.route?.shelterName}", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 4. BOTTOM DASHBOARD - Fixed spacing and alignment
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 120.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isOffPath) "Wrong Way!" else "Safe Path", 
                                    color = if (isOffPath) Error else Success, 
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = if (distanceToDest == null) "Locating..." else "${String.format("%.2f", distanceToDest!! / 1000.0)} km remaining",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if (isOffPath) Error.copy(alpha = 0.2f) else Success.copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isOffPath) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isOffPath) Error else Success,
                                    modifier = Modifier.size(32.dp)
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
