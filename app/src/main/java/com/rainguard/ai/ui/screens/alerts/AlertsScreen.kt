package com.rainguard.ai.ui.screens.alerts

import android.content.Context
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
import com.rainguard.ai.data.model.Alert
import com.rainguard.ai.ui.components.EmptyState
import com.rainguard.ai.ui.components.LoadingIndicator
import com.rainguard.ai.ui.components.RiskLevelBadge
import com.rainguard.ai.ui.theme.Success
import com.rainguard.ai.ui.utils.DateTimeUtils
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    navController: NavController,
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.showSuccessMessage) {
        state.showSuccessMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications & Alerts") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.filter == AlertFilter.ACTIVE,
                    onClick = { viewModel.setFilter(AlertFilter.ACTIVE) },
                    label = { Text("Active") }
                )
                FilterChip(
                    selected = state.filter == AlertFilter.ALL,
                    onClick = { viewModel.setFilter(AlertFilter.ALL) },
                    label = { Text("All Alerts") }
                )
            }

            if (state.isLoading) {
                LoadingIndicator()
            } else {
                val filteredAlerts = when (state.filter) {
                    AlertFilter.ACTIVE -> state.alerts.filter { !it.acknowledged }
                    AlertFilter.ALL -> state.alerts
                }

                if (filteredAlerts.isEmpty()) {
                    EmptyState(
                        title = "All clear!",
                        message = "No active alerts for your area at the moment."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredAlerts) { alert ->
                            AlertCard(
                                alert = alert,
                                context = context,
                                onAcknowledge = { viewModel.acknowledgeAlert(it) },
                                onSafe = { viewModel.sendSafeStatus(it) },
                                onClick = { viewModel.selectAlert(alert) }
                            )
                        }
                    }
                }
            }
        }
    }

    state.selectedAlert?.let { alert ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissAlert() },
            title = { Text(alert.title) },
            text = {
                Column {
                    Text(alert.message)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Recommended Actions:", fontWeight = FontWeight.Bold)
                    alert.recommendedActions.forEach { action ->
                        Text("• $action", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Confidence: High (0.85)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(onClick = { 
                    viewModel.sendSafeStatus(alert.id)
                    viewModel.dismissAlert()
                }) {
                    Text("I'M SAFE")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissAlert() }) {
                    Text("CLOSE")
                }
            }
        )
    }
}

@Composable
fun AlertCard(
    alert: Alert,
    context: Context,
    onAcknowledge: (String) -> Unit,
    onSafe: (String) -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Map Snippet
            Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                AlertMiniMap(context = context)
                Box(modifier = Modifier.padding(8.dp).align(Alignment.TopEnd)) {
                    RiskLevelBadge(severity = alert.severity)
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = DateTimeUtils.getRelativeTime(alert.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onAcknowledge(alert.id) },
                        modifier = Modifier.weight(1f),
                        enabled = !alert.acknowledged
                    ) {
                        Text(if (alert.acknowledged) "Acknowledged ✓" else "Acknowledge")
                    }
                    Button(
                        onClick = { onSafe(alert.id) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Success)
                    ) {
                        Text("I'm Safe")
                    }
                }
                
                TextButton(
                    onClick = { /* Help flow */ },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Request help for this area", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun AlertMiniMap(context: Context) {
    val mapView = remember {
        MapView(context).apply {
            Configuration.getInstance().load(context, context.getSharedPreferences("osm", Context.MODE_PRIVATE))
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(false)
            controller.setZoom(13.0)
            controller.setCenter(GeoPoint(28.7041, 77.1025))
            
            val marker = Marker(this)
            marker.position = GeoPoint(28.7041, 77.1025)
            marker.icon = context.getDrawable(R.drawable.ic_alert)
            overlays.add(marker)
        }
    }
    AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
}
