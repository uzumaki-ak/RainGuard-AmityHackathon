package com.rainguard.ai.ui.screens.authority

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rainguard.ai.R
import com.rainguard.ai.data.model.Report
import com.rainguard.ai.data.model.ReportType
import com.rainguard.ai.data.model.Route
import com.rainguard.ai.data.model.Shelter
import com.rainguard.ai.ui.navigation.NavRoutes
import com.rainguard.ai.ui.theme.*
import com.rainguard.ai.ui.utils.DateTimeUtils
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorityDashboardScreen(
    navController: NavController,
    viewModel: AuthorityDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.overview),
        stringResource(R.string.incidents),
        stringResource(R.string.shelters),
        stringResource(R.string.reports)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.command_center), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate(NavRoutes.SETTINGS) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(title) },
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.Dashboard
                                    1 -> Icons.Default.Warning
                                    2 -> Icons.Default.HomeWork
                                    else -> Icons.Default.Assessment
                                },
                                contentDescription = title
                            )
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> OverviewTab(state, viewModel, navController)
                1 -> IncidentsTab(state, viewModel)
                2 -> SheltersTab(state, viewModel)
                3 -> ReportsTab(state, viewModel)
            }
        }
    }
}

@Composable
fun OverviewTab(state: AuthorityDashboardState, viewModel: AuthorityDashboardViewModel, navController: NavController) {
    val context = LocalContext.current
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Live Map Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().height(250.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AuthorityMiniMap(context, state.allReports, onDelete = { viewModel.deleteReport(it) })
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                    ) {
                        Text("Interactive View", color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }

        // Stats
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(stringResource(R.string.active_reports), state.pendingReports.toString(), Icons.Default.Report, Warning, Modifier.weight(1f))
                StatCard("Safe Count", state.safeSignals.size.toString(), Icons.Default.CheckCircle, Success, Modifier.weight(1f))
            }
        }

        // Life Safety Live Feed
        item {
            Text("Life Safety Feed (Live)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Success.copy(alpha = 0.2f))
            ) {
                if (state.safeSignals.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No signals received yet", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.padding(8.dp)) {
                        items(state.safeSignals) { signal ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, null, tint = Success, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(signal.userName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Text(DateTimeUtils.getRelativeTime(signal.timestamp), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                            Divider(color = Success.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        }

        state.suggestedRoute?.let { route ->
            item {
                Text(stringResource(R.string.action_required), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                SuggestedRouteCard(route, onApprove = { viewModel.approveRoute(route.id) }, onReject = { viewModel.rejectRoute(route.id) })
            }
        }

        item {
            Text(stringResource(R.string.regional_risk_index), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            RiskTrendChart(trends = state.riskTrends)
        }
        
        item {
            SystemHealthCard()
        }
    }
}

@Composable
fun AuthorityMiniMap(context: Context, reports: List<Report>, onDelete: (String) -> Unit) {
    val mapView = remember {
        MapView(context).apply {
            Configuration.getInstance().load(context, context.getSharedPreferences("osm_auth", Context.MODE_PRIVATE))
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(12.0)
            controller.setCenter(GeoPoint(28.7041, 77.1025))
        }
    }

    LaunchedEffect(reports) {
        mapView.overlays.clear()
        reports.forEach { report ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(report.lat, report.lng)
                title = report.type.toDisplayString()
                snippet = "TAP TO DELETE"
                icon = context.getDrawable(
                    when(report.type) {
                        ReportType.FLOOD -> R.drawable.ic_alert
                        ReportType.REQUEST_HELP -> R.drawable.ic_citizen
                        else -> R.drawable.ic_report
                    }
                )
                setOnMarkerClickListener { m, _ ->
                    onDelete(report.id)
                    m.closeInfoWindow()
                    true
                }
            }
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }

    AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
}

@Composable
fun IncidentsTab(state: AuthorityDashboardState, viewModel: AuthorityDashboardViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(stringResource(R.string.active_incidents), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(state.reports.filter { !it.verified }) { report ->
            ApprovalRequestCard(
                report = report,
                onApprove = { viewModel.approveReport(report.id) },
                onDelete = { viewModel.deleteReport(report.id) },
                onLocate = { }
            )
        }
    }
}

@Composable
fun SheltersTab(state: AuthorityDashboardState, viewModel: AuthorityDashboardViewModel) {
    var editingShelter by remember { mutableStateOf<Shelter?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(stringResource(R.string.shelter_management), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(state.shelters) { shelter ->
            ShelterManagementCard(shelter, onEdit = { editingShelter = shelter })
        }
    }

    editingShelter?.let { shelter ->
        var capacity by remember { mutableStateOf(shelter.capacity.toString()) }
        var available by remember { mutableStateOf(shelter.available.toString()) }

        AlertDialog(
            onDismissRequest = { editingShelter = null },
            title = { Text("${stringResource(R.string.update_shelter)}: ${shelter.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = capacity, onValueChange = { capacity = it }, label = { Text(stringResource(R.string.capacity)) })
                    OutlinedTextField(value = available, onValueChange = { available = it }, label = { Text(stringResource(R.string.available)) })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateShelter(shelter.copy(capacity = capacity.toIntOrNull() ?: shelter.capacity, available = available.toIntOrNull() ?: shelter.available))
                    editingShelter = null
                }) { Text(stringResource(R.string.ok)) }
            }
        )
    }
}

@Composable
fun ReportsTab(state: AuthorityDashboardState, viewModel: AuthorityDashboardViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(stringResource(R.string.audit_log), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(state.reports) { report ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (report.verified) Icons.Default.CheckCircle else Icons.Default.History, contentDescription = null, tint = if (report.verified) Success else Color.Gray)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(report.type.toDisplayString(), fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.verified_by_system, DateTimeUtils.getRelativeTime(report.timestamp)), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestedRouteCard(route: Route, onApprove: () -> Unit, onReject: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.suggested_evac_route), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("For Ward 12 -> ${route.shelterName}", style = MaterialTheme.typography.bodyMedium)
            Text("${stringResource(R.string.why_this_route)}: ${route.rationale.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onApprove, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.approve)) }
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.reject)) }
            }
        }
    }
}

@Composable
fun ShelterManagementCard(shelter: Shelter, onEdit: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(shelter.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = (shelter.capacity - shelter.available).toFloat() / shelter.capacity,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = if (shelter.available < 20) Error else Success
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${stringResource(R.string.capacity)}: ${shelter.capacity - shelter.available}/${shelter.capacity}", style = MaterialTheme.typography.bodySmall)
                Text("${shelter.available} ${stringResource(R.string.available)}", style = MaterialTheme.typography.labelSmall, color = if (shelter.available < 20) Error else Success)
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Text(title, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun RiskTrendChart(trends: List<Float>) {
    Card(modifier = Modifier.fillMaxWidth().height(150.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (trends.size < 2) return@Canvas
                val path = Path()
                val stepX = size.width / (trends.size - 1)
                trends.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = size.height - (value * size.height)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path = path, color = Success, style = Stroke(width = 3.dp.toPx()))
            }
        }
    }
}

@Composable
fun ApprovalRequestCard(report: Report, onApprove: () -> Unit, onDelete: () -> Unit, onLocate: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().animateContentSize(), shape = RoundedCornerShape(20.dp), onClick = { isExpanded = !isExpanded }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.WaterDamage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(report.type.toDisplayString(), fontWeight = FontWeight.Bold)
                    Text(DateTimeUtils.getRelativeTime(report.timestamp), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Row {
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Close, contentDescription = null, tint = Error) }
                    IconButton(onClick = onApprove) { Icon(Icons.Default.Check, contentDescription = null, tint = Success) }
                }
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(report.description, style = MaterialTheme.typography.bodySmall)
                if (report.photoUrl != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(model = report.photoUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                }
            }
        }
    }
}

@Composable
fun SystemHealthCard() {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.system_status), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            HealthItem("AI Prediction Engine", true)
            HealthItem("IoT Sensor Grid", true)
            HealthItem("Evacuation Router", true)
        }
    }
}

@Composable
fun HealthItem(name: String, isHealthy: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(name, style = MaterialTheme.typography.bodySmall)
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isHealthy) Success else Error))
    }
}
