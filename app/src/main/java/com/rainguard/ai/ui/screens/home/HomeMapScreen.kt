package com.rainguard.ai.ui.screens.home

import android.Manifest
import android.content.Context
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.rainguard.ai.R
import com.rainguard.ai.data.model.Report
import com.rainguard.ai.data.model.ReportType
import com.rainguard.ai.data.model.RiskSeverity
import com.rainguard.ai.data.model.Route
import com.rainguard.ai.ui.components.ConfidenceBar
import com.rainguard.ai.ui.components.LoadingIndicator
import com.rainguard.ai.ui.components.RiskLevelBadge
import com.rainguard.ai.ui.navigation.NavRoutes
import com.rainguard.ai.ui.theme.*
import com.rainguard.ai.ui.utils.DateTimeUtils
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeMapScreen(
    navController: NavController,
    viewModel: HomeMapViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    var showRiskSheet by remember { mutableStateOf(false) }
    var selectedReport by remember { mutableStateOf<Report?>(null) }

    val riskSheetState = rememberModalBottomSheetState()
    val reportSheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(NavRoutes.SETTINGS) }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (state.reports.count { !it.verified } > 0) {
                                Badge { Text("${state.reports.count { !it.verified }}") }
                            }
                        }
                    ) {
                        IconButton(onClick = { navController.navigate(NavRoutes.ALERTS) }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { navController.navigate(NavRoutes.CHAT) },
                    containerColor = MaterialTheme.colorScheme.tertiary
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "Chat Assistant")
                }
                
                SmallFloatingActionButton(
                    onClick = { navController.navigate(NavRoutes.REPORT) },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.Camera, contentDescription = stringResource(R.string.report_incident))
                }
                
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )
                
                FloatingActionButton(
                    onClick = {
                        state.nearestShelter?.let {
                            viewModel.requestEvacuationRoute(it.id)
                        }
                    },
                    containerColor = Success,
                    modifier = Modifier.scale(if (state.currentRisk == RiskSeverity.HIGH) scale else 1f)
                ) {
                    Icon(
                        Icons.Default.DirectionsRun,
                        contentDescription = stringResource(R.string.evacuate),
                        tint = Color.White
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                LoadingIndicator()
            } else {
                OSMMapView(
                    context = context,
                    riskZones = state.riskZones,
                    shelters = state.shelters,
                    reports = state.reports,
                    activeRoute = null, // Can be updated when a route is computed
                    onShelterClick = { shelterId ->
                        navController.navigate(NavRoutes.shelter(shelterId))
                    },
                    onReportClick = { report ->
                        selectedReport = report
                    },
                    onMapLongPress = { lat, lng ->
                        navController.navigate(NavRoutes.REPORT + "?lat=$lat&lng=$lng")
                    },
                    onLocationUpdate = { lat, lng ->
                        viewModel.setUserLocation(lat, lng)
                    }
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .padding(bottom = 80.dp) // Space for status row
                ) {
                    Surface(
                        onClick = { showRiskSheet = true },
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RiskLevelBadge(severity = state.currentRisk)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.current_risk),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Confidence: ${(state.currentConfidence * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "• ${DateTimeUtils.getRelativeTime(state.lastUpdated)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showRiskSheet) {
            ModalBottomSheet(
                onDismissRequest = { showRiskSheet = false },
                sheetState = riskSheetState
            ) {
                RiskBottomSheetContent(
                    state = state,
                    onShelterClick = {
                        navController.navigate(NavRoutes.shelter(it))
                        showRiskSheet = false
                    },
                    onReportClick = {
                        navController.navigate(NavRoutes.REPORT)
                        showRiskSheet = false
                    }
                )
            }
        }

        selectedReport?.let { report ->
            ModalBottomSheet(
                onDismissRequest = { selectedReport = null },
                sheetState = reportSheetState
            ) {
                ReportDetailBottomSheet(report = report)
            }
        }
    }
}

@Composable
fun RiskBottomSheetContent(
    state: HomeMapState,
    onShelterClick: (String) -> Unit,
    onReportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live Status",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            RiskLevelBadge(severity = state.currentRisk)
        }

        Spacer(modifier = Modifier.height(16.dp))
        ConfidenceBar(confidence = state.currentConfidence)
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { state.nearestShelter?.let { onShelterClick(it.id) } },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Icon(Icons.Default.DirectionsRun, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Evacuate to Nearest Shelter")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onReportClick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Icon(Icons.Default.Report, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Report Flood / Damage")
        }

        Spacer(modifier = Modifier.height(24.dp))

        state.nearestShelter?.let { shelter ->
            Text(
                text = "Nearest Shelter Info",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(shelter.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(shelter.address, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Available: ${shelter.available}/${shelter.capacity}", color = Success, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ReportDetailBottomSheet(report: Report) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = report.type.toDisplayString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (report.verified) {
                Surface(
                    color = Success.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Success, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verified", style = MaterialTheme.typography.labelSmall, color = Success)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (report.photoUrl != null) {
            AsyncImage(
                model = report.photoUrl,
                contentDescription = "Incident Photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(text = "Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = report.description, style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = DateTimeUtils.getRelativeTime(report.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun OSMMapView(
    context: Context,
    riskZones: List<com.rainguard.ai.data.model.RiskZone>,
    shelters: List<com.rainguard.ai.data.model.Shelter>,
    reports: List<com.rainguard.ai.data.model.Report>,
    activeRoute: Route?,
    onShelterClick: (String) -> Unit,
    onReportClick: (com.rainguard.ai.data.model.Report) -> Unit,
    onMapLongPress: (Double, Double) -> Unit,
    onLocationUpdate: (Double, Double) -> Unit
) {
    val mapView = remember {
        MapView(context).apply {
            Configuration.getInstance().load(context, context.getSharedPreferences("osm", Context.MODE_PRIVATE))
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(14.0)
            controller.setCenter(GeoPoint(28.7041, 77.1025))
        }
    }

    val locationOverlay = remember { MyLocationNewOverlay(mapView) }

    LaunchedEffect(riskZones, shelters, reports, activeRoute) {
        mapView.overlays.clear()
        
        // My Location
        locationOverlay.enableMyLocation()
        mapView.overlays.add(locationOverlay)

        // Events Overlay for Long Press
        val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean = false
            override fun longPressHelper(p: GeoPoint?): Boolean {
                p?.let { onMapLongPress(it.latitude, it.longitude) }
                return true
            }
        })
        mapView.overlays.add(eventsOverlay)

        // Risk Zones (Heatmap/Polygons)
        riskZones.forEach { zone ->
            val polygon = Polygon(mapView)
            val points = zone.coordinates.map { GeoPoint(it[1], it[0]) }
            polygon.points = points
            val color = when(zone.severity) {
                RiskSeverity.HIGH -> android.graphics.Color.argb(100, 255, 0, 0)
                RiskSeverity.MEDIUM -> android.graphics.Color.argb(100, 255, 165, 0)
                RiskSeverity.LOW -> android.graphics.Color.argb(100, 0, 255, 0)
            }
            polygon.fillPaint.color = color
            polygon.outlinePaint.color = color
            polygon.outlinePaint.strokeWidth = 2f
            mapView.overlays.add(polygon)
        }

        // Active Route (Green Line)
        activeRoute?.let { route ->
            val line = Polyline(mapView)
            line.setPoints(route.path.map { GeoPoint(it[1], it[0]) })
            line.outlinePaint.color = android.graphics.Color.GREEN
            line.outlinePaint.strokeWidth = 10f
            mapView.overlays.add(line)
        }

        // Shelters (House Icons)
        shelters.forEach { shelter ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(shelter.lat, shelter.lng)
                title = shelter.name
                icon = context.getDrawable(R.drawable.ic_shelter)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { _, _ ->
                    onShelterClick(shelter.id)
                    true
                }
            }
            mapView.overlays.add(marker)
        }

        // Reports (Clustered Pins - simplified for now)
        reports.forEach { report ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(report.lat, report.lng)
                title = report.type.toDisplayString()
                icon = when(report.type) {
                    ReportType.FLOOD -> context.getDrawable(R.drawable.ic_alert)
                    ReportType.REQUEST_HELP -> context.getDrawable(R.drawable.ic_citizen)
                    else -> context.getDrawable(R.drawable.ic_report)
                }
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { _, _ ->
                    onReportClick(report)
                    true
                }
            }
            mapView.overlays.add(marker)
        }

        mapView.invalidate()
        onLocationUpdate(28.7041, 77.1025)
    }

    AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
}
