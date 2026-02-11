package com.rainguard.ai.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rainguard.ai.R
import com.rainguard.ai.data.model.UserRole
import com.rainguard.ai.ui.navigation.NavRoutes

@Composable
fun RoleSelectionScreen(
    navController: NavController,
    viewModel: RoleSelectionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.who_are_you),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Citizen Card
        RoleCard(
            icon = Icons.Default.Person,
            title = stringResource(R.string.citizen),
            description = stringResource(R.string.citizen_desc),
            selected = state.selectedRole == UserRole.CITIZEN,
            onClick = { viewModel.selectRole(UserRole.CITIZEN) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Authority Card
        RoleCard(
            icon = Icons.Default.Shield,
            title = stringResource(R.string.authority),
            description = stringResource(R.string.authority_desc),
            selected = state.selectedRole == UserRole.AUTHORITY,
            onClick = { viewModel.selectRole(UserRole.AUTHORITY) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Remember checkbox
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = state.rememberRole,
                onCheckedChange = { viewModel.setRememberRole(it) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.remember_role),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Continue button
        Button(
            onClick = {
                viewModel.confirmRole()
                val route = when (state.selectedRole) {
                    UserRole.CITIZEN -> NavRoutes.HOME_MAP
                    UserRole.AUTHORITY -> NavRoutes.AUTHORITY_DASHBOARD
                    null -> return@Button
                }
                navController.navigate(route) {
                    popUpTo(NavRoutes.LOGIN_ROLE) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.canContinue
        ) {
            Text(stringResource(R.string.continue_btn))
        }
    }
}

@Composable
fun RoleCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (selected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            if (selected) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}