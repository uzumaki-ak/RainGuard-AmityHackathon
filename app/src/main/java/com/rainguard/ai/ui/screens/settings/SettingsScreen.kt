package com.rainguard.ai.ui.screens.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rainguard.ai.R
import com.rainguard.ai.data.model.EmergencyContact
import com.rainguard.ai.data.model.UserRole
import com.rainguard.ai.ui.navigation.NavRoutes
import com.rainguard.ai.ui.theme.Success

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddContactDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Language Selection
            item {
                SettingsSection(title = stringResource(R.string.language))
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val languages = listOf("en" to "English", "hi" to "हिंदी")
                    languages.forEachIndexed { index, (code, label) ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = languages.size),
                            onClick = {
                                viewModel.setLanguage(code)
                                val appLocale = LocaleListCompat.forLanguageTags(code)
                                AppCompatDelegate.setApplicationLocales(appLocale)
                            },
                            selected = state.language == code
                        ) {
                            Text(label)
                        }
                    }
                }
            }

            item { Divider() }

            // Saved Shelter Section
            item {
                SettingsSection(title = "Saved Primary Shelter")
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HomeWork, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Shelter B (Primary)", fontWeight = FontWeight.Bold)
                            Text("77.1, 28.7", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            item { Divider() }

            // Emergency Contacts
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsSection(title = stringResource(R.string.emergency_contacts))
                    IconButton(onClick = { showAddContactDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_contact))
                    }
                }
                Text("Your location will be auto-shared with these contacts during evacuation.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }

            items(state.contacts) { contact ->
                ContactItem(
                    contact = contact,
                    onDelete = { viewModel.deleteContact(it) }
                )
            }

            item { Divider() }

            // Switch Role
            item {
                SettingsSection(title = "Switch Role")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = if (state.userRole == UserRole.CITIZEN) "Currently: Citizen" else "Currently: Authority", fontWeight = FontWeight.Bold)
                            Text(text = "Toggle to switch interface", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = state.userRole == UserRole.AUTHORITY,
                            onCheckedChange = { isAuthority ->
                                val newRole = if (isAuthority) UserRole.AUTHORITY else UserRole.CITIZEN
                                viewModel.setUserRole(newRole)
                                if (newRole == UserRole.AUTHORITY) {
                                    navController.navigate(NavRoutes.AUTHORITY_DASHBOARD) { popUpTo(NavRoutes.HOME_MAP) { inclusive = true } }
                                } else {
                                    navController.navigate(NavRoutes.HOME_MAP) { popUpTo(NavRoutes.AUTHORITY_DASHBOARD) { inclusive = true } }
                                }
                            }
                        )
                    }
                }
            }

            item { Divider() }

            item {
                SettingsSection(title = stringResource(R.string.danger_zone))
                OutlinedButton(
                    onClick = { showClearCacheDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.clear_cache))
                }
            }
        }
    }

    if (showAddContactDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = { Text(stringResource(R.string.add_contact)) },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        viewModel.addContact(name, phone)
                        showAddContactDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddContactDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text(stringResource(R.string.clear_cache)) },
            text = { Text(stringResource(R.string.clear_cache_confirm)) },
            confirmButton = { TextButton(onClick = { viewModel.clearCache(); showClearCacheDialog = false }) { Text(stringResource(R.string.yes)) } },
            dismissButton = { TextButton(onClick = { showClearCacheDialog = false }) { Text(stringResource(R.string.no)) } }
        )
    }
}

@Composable
fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun ContactItem(
    contact: EmergencyContact,
    onDelete: (EmergencyContact) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(contact.name, fontWeight = FontWeight.Bold)
                    Text(contact.phone, style = MaterialTheme.typography.bodySmall)
                }
            }
            Row {
                IconButton(onClick = { /* Call Logic */ }) { Icon(Icons.Default.Phone, contentDescription = "Call", tint = Success) }
                IconButton(onClick = { onDelete(contact) }) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error) }
            }
        }
    }
}
