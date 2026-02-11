package com.rainguard.ai.ui.screens.report

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.rainguard.ai.R
import com.rainguard.ai.data.model.ReportType
import com.rainguard.ai.ui.theme.Error
import com.rainguard.ai.ui.theme.Success
import com.rainguard.ai.ui.utils.PermissionUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class, ExperimentalLayoutApi::class)
@Composable
fun ReportScreen(
    navController: NavController,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val cameraPermissions = rememberMultiplePermissionsState(
        permissions = PermissionUtils.CAMERA_PERMISSIONS.toList()
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.setPhoto(it) }
    }

    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.submitSuccess) {
        if (state.submitSuccess) {
            showSuccessDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Report") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Immediate Danger Button
            Button(
                onClick = { 
                    viewModel.submitReport(isImmediateDanger = true)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Error),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("REPORT IMMEDIATE DANGER", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Type Selection
            Text(text = "Report Type", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportType.values().forEach { type ->
                    FilterChip(
                        selected = state.selectedType == type,
                        onClick = { viewModel.setType(type) },
                        label = { Text(type.toDisplayString()) }
                    )
                }
            }

            // Location Section
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Auto-Geolocation", style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (state.location != null) "${state.location!!.first}, ${state.location!!.second}" else "Detecting...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            // Photo Upload
            OutlinedButton(
                onClick = {
                    if (cameraPermissions.allPermissionsGranted) {
                        galleryLauncher.launch("image/*")
                    } else {
                        cameraPermissions.launchMultiplePermissionRequest()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (state.photoUri != null) "Photo Added ✓" else "Upload Photo")
            }

            // Notes
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.setDescription(it) },
                label = { Text("Short Notes") },
                placeholder = { Text("E.g. Water rising rapidly near the bridge...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Optional Contact
            OutlinedTextField(
                value = state.contact,
                onValueChange = { viewModel.setContact(it) },
                label = { Text("Optional Contact / SMS") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.submitReport() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSubmitting && state.selectedType != null,
                contentPadding = PaddingValues(16.dp)
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Submit Report")
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                navController.popBackStack()
            },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(48.dp)) },
            title = { Text("Report Received") },
            text = { Text("Your report has been queued for verification. It will appear on the map shortly.") },
            confirmButton = {
                TextButton(onClick = { 
                    showSuccessDialog = false
                    navController.popBackStack()
                }) {
                    Text("OK")
                }
            }
        )
    }
}
