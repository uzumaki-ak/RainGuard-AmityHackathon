package com.rainguard.ai.ui.screens.onboarding

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.rainguard.ai.R
import com.rainguard.ai.ui.navigation.NavRoutes
import com.rainguard.ai.ui.utils.PermissionUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val locationPermissions = rememberMultiplePermissionsState(
        permissions = PermissionUtils.LOCATION_PERMISSIONS.toList()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        viewModel.setLocationPermission(allGranted)
    }

    val notificationPermissions = if (PermissionUtils.NOTIFICATION_PERMISSION.isNotEmpty()) {
        rememberMultiplePermissionsState(
            permissions = PermissionUtils.NOTIFICATION_PERMISSION.toList()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            viewModel.setNotificationPermission(allGranted)
        }
    } else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPage(
                page = page,
                onPageChange = { viewModel.setPage(it) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Page indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(if (pagerState.currentPage == index) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == index)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Language selector
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = state.selectedLanguage,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.language)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                listOf("English", "हिन्दी", "বাংলা").forEach { lang ->
                    DropdownMenuItem(
                        text = { Text(lang) },
                        onClick = {
                            viewModel.setLanguage(lang)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Permission buttons
        Button(
            onClick = { locationPermissions.launchMultiplePermissionRequest() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.locationPermissionGranted
        ) {
            Icon(
                imageVector = if (state.locationPermissionGranted) Icons.Default.CheckCircle else Icons.Default.LocationOn,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (state.locationPermissionGranted)
                    "Location Allowed ✓"
                else
                    stringResource(R.string.allow_location)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { notificationPermissions?.launchMultiplePermissionRequest() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (state.notificationPermissionGranted) Icons.Default.CheckCircle else Icons.Default.Notifications,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (state.notificationPermissionGranted)
                    "Notifications Enabled ✓"
                else
                    stringResource(R.string.enable_notifications)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Demo mode toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = state.demoMode,
                onCheckedChange = { viewModel.setDemoMode(it) }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.demo_mode),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(R.string.demo_mode_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Continue button
        Button(
            onClick = {
                viewModel.completeOnboarding()
                navController.navigate(NavRoutes.LOGIN_ROLE) {
                    popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.canContinue
        ) {
            Text(stringResource(R.string.continue_btn))
        }

        AnimatedVisibility(visible = !state.locationPermissionGranted && state.canContinue) {
            Text(
                text = "⚠️ Location permission enhances safety features",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun OnboardingPage(
    page: Int,
    onPageChange: (Int) -> Unit
) {
    val (icon, title, description) = when (page) {
        0 -> Triple(
            Icons.Default.Shield,
            stringResource(R.string.onboarding_title_1),
            stringResource(R.string.onboarding_desc_1)
        )
        1 -> Triple(
            Icons.Default.Map,
            stringResource(R.string.onboarding_title_2),
            stringResource(R.string.onboarding_desc_2)
        )
        else -> Triple(
            Icons.Default.Lock,
            stringResource(R.string.onboarding_title_3),
            stringResource(R.string.onboarding_desc_3)
        )
    }

    LaunchedEffect(page) {
        onPageChange(page)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}