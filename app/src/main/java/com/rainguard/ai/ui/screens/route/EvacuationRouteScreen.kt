package com.rainguard.ai.ui.screens.route

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rainguard.ai.R
import com.rainguard.ai.data.model.RouteSegment
import com.rainguard.ai.data.model.RiskSeverity
import com.rainguard.ai.ui.components.ConfidenceBar
import com.rainguard.ai.ui.components.LoadingIndicator
import com.rainguard.ai.ui.theme.Success
import com.rainguard.ai.ui.theme.Warning
import com.rainguard.ai.ui.theme.Error
import com.rainguard.ai.ui.utils.LocationUtils
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvacuationRouteScreen(
    routeId: String,
    navController: NavController,
    viewModel: EvacuationRouteViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(routeId) {
        viewModel.loadRoute(routeId)
    }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showNavigationStarted by remember { mutableStateOf(false) }
    var showModelTrace by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Route Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingIndicator()
        } else if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${state.error}")
            }
        } else {
            state.route?.let { route ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Risk Ribbon
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Success.copy(alpha = 0.9f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Safe Route — Move to high ground",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Map Section
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        ) {
                            RouteMiniMap(context = context, path = route.path)
                            
                            Card(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .align(Alignment.BottomStart),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text("ETA: ${route.etaMinutes} mins", fontWeight = FontWeight.Bold)
                                        Text("${route.distanceMeters / 1000.0} km", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Route to ${route.shelterName}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            ConfidenceBar(confidence = route.confidence)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Why this route?
                            Text("Why this route?", fontWeight = FontWeight.Bold)
                            route.rationale.forEach { reason ->
                                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Success, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(reason, style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Model Trace (Collapsed)
                            OutlinedCard(
                                onClick = { showModelTrace = !showModelTrace },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Model Trace Data", fontWeight = FontWeight.Bold)
                                        Icon(
                                            if (showModelTrace) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null
                                        )
                                    }
                                    AnimatedVisibility(visible = showModelTrace) {
                                        Column {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Sources used for this computation:", style = MaterialTheme.typography.bodySmall)
                                            route.sources.forEach { source ->
                                                Text("• $source", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Instructions",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(route.segments) { segment ->
                        SegmentCard(segment = segment)
                    }

                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Button(
                                onClick = {
                                    val gmmIntentUri = Uri.parse("google.navigation:q=${route.path.last()[1]},${route.path.last()[0]}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                    mapIntent.setPackage("com.google.android.apps.maps")
                                    context.startActivity(mapIntent)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start Navigation")
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { /* Share Logic */ },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share")
                                }
                                OutlinedButton(
                                    onClick = { /* Contact Logic */ },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.ContactPhone, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Send Alert")
                                }
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RouteMiniMap(context: Context, path: List<List<Double>>) {
    val mapView = remember {
        MapView(context).apply {
            Configuration.getInstance().load(context, context.getSharedPreferences("osm", Context.MODE_PRIVATE))
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            val line = Polyline(this)
            line.setPoints(path.map { GeoPoint(it[1], it[0]) })
            line.outlinePaint.color = android.graphics.Color.GREEN
            line.outlinePaint.strokeWidth = 10f
            overlays.add(line)
            if (path.isNotEmpty()) {
                controller.setCenter(GeoPoint(path[0][1], path[0][0]))
                controller.setZoom(15.0)
            }
        }
    }
    AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
}

@Composable
fun SegmentCard(segment: RouteSegment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (segment.hazards.isEmpty()) Icons.Default.ArrowForward else Icons.Default.Warning,
                contentDescription = null,
                tint = if (segment.hazards.isEmpty()) Success else Warning
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("${segment.from} to ${segment.to}", fontWeight = FontWeight.Bold)
                Text("${segment.distanceMeters}m • ${segment.etaSeconds / 60} mins", style = MaterialTheme.typography.bodySmall)
                if (segment.hazards.isNotEmpty()) {
                    Text("Hazards avoided: ${segment.hazards.joinToString()}", color = Error, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
