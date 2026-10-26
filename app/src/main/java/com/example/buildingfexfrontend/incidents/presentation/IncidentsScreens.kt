package com.example.buildingfexfrontend.incidents.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.buildingfexfrontend.core.di.AppContainer
import com.example.buildingfexfrontend.core.i18n.string
import com.example.buildingfexfrontend.core.ui.appViewModel
import com.example.buildingfexfrontend.core.ui.components.AppTextField
import com.example.buildingfexfrontend.core.ui.components.ConfirmDialog
import com.example.buildingfexfrontend.core.ui.components.EmptyState
import com.example.buildingfexfrontend.core.ui.components.ErrorState
import com.example.buildingfexfrontend.core.ui.components.FullScreenLoading
import com.example.buildingfexfrontend.core.ui.components.SectionCard
import com.example.buildingfexfrontend.core.ui.components.StatusChip
import com.example.buildingfexfrontend.core.ui.components.VerticalGap
import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.incidents.domain.model.Incident
import com.example.buildingfexfrontend.incidents.domain.model.IncidentStatus
import com.example.buildingfexfrontend.ui.theme.BfError
import com.example.buildingfexfrontend.ui.theme.BfErrorContainer
import com.example.buildingfexfrontend.ui.theme.BfSuccess
import com.example.buildingfexfrontend.ui.theme.BfSuccessContainer
import com.example.buildingfexfrontend.ui.theme.BfWarning
import com.example.buildingfexfrontend.ui.theme.BfWarningContainer

@Composable
private fun statusColors(status: String): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> =
    when (status) {
        IncidentStatus.IN_PROGRESS -> BfWarningContainer to BfWarning
        IncidentStatus.RESOLVED -> BfSuccessContainer to BfSuccess
        else -> BfErrorContainer to BfError
    }

@Composable
private fun IncidentRow(incident: Incident, trailing: (@Composable () -> Unit)? = null) {
    val (container, content) = statusColors(incident.status)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusChip(text = string("inc.status." + incident.status), container = container, content = content)
                Spacer(modifier = Modifier.width(4.dp))
                trailing?.invoke()
            }
            VerticalGap(8)
            Text(incident.description, style = MaterialTheme.typography.bodyMedium)
            VerticalGap(6)
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = incident.residentName.ifBlank { string("inc.noResident") },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = incident.provider.ifBlank { string("inc.noProvider") },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            incident.createdAt?.let {
                Text(
                    text = Dates.displayDateTime(it),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminIncidentsScreen(container: AppContainer) {
    val viewModel: AdminIncidentsViewModel = appViewModel { AdminIncidentsViewModel(container.incidents) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::openCreate) {
                Icon(Icons.Filled.Add, contentDescription = string("inc.fab.add"))
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            VerticalGap(12)
            Text(
                text = string("inc.admin.title"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = string("inc.admin.subtitle"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VerticalGap(12)
            when {
                state.loading -> FullScreenLoading()
                state.error != null -> ErrorState(state.error!!, onRetry = viewModel::load)
                state.incidents.isEmpty() -> EmptyState(string("inc.admin.empty"))
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                ) {
                    items(state.incidents, key = { it.id }) { incident ->
                        IncidentRow(incident) {
                            IconButton(onClick = { viewModel.openEdit(incident) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Flag,
                                    contentDescription = string("inc.action.edit"),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                            IconButton(onClick = { viewModel.openDelete(incident) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = string("inc.action.delete"),
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.editorVisible) {
        IncidentEditorDialog(state, viewModel)
    }

    state.deleteTarget?.let { target ->
        ConfirmDialog(
            title = string("inc.delete.title"),
            message = string("inc.delete.message"),
            confirmText = string("inc.action.delete"),
            destructive = true,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::closeDelete,
        )
    }

    state.message?.let { message ->
        ConfirmDialog(
            title = string("inc.message.title"),
            message = message,
            confirmText = string("inc.message.ack"),
            onConfirm = viewModel::dismissMessage,
            onDismiss = viewModel::dismissMessage,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IncidentEditorDialog(state: AdminIncidentsUiState, viewModel: AdminIncidentsViewModel) {
    val statusOptions = listOf(
        IncidentStatus.OPEN to string("inc.status.open"),
        IncidentStatus.IN_PROGRESS to string("inc.status.in-progress"),
        IncidentStatus.RESOLVED to string("inc.status.resolved"),
    )

    AlertDialog(
        onDismissRequest = viewModel::closeEditor,
        title = {
            Text(
                if (state.editingId == null) string("inc.editor.createTitle")
                else string("inc.editor.editTitle"),
            )
        },
        text = {
            Column {
                AppTextField(
                    value = state.formDescription,
                    onValueChange = viewModel::onDescriptionChange,
                    label = string("inc.field.description"),
                    singleLine = false,
                )
                VerticalGap(12)
                Text(
                    text = string("inc.field.status"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    statusOptions.forEach { (value, label) ->
                        androidx.compose.material3.FilterChip(
                            selected = state.formStatus == value,
                            onClick = { viewModel.onStatusChange(value) },
                            label = { Text(label) },
                        )
                    }
                }
                VerticalGap(12)
                AppTextField(
                    value = state.formProvider,
                    onValueChange = viewModel::onProviderChange,
                    label = string("inc.field.provider"),
                )
                state.formError?.let {
                    VerticalGap(8)
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = viewModel::save, enabled = !state.saving) {
                Text(if (state.saving) string("inc.action.saving") else string("inc.action.save"))
            }
        },
        dismissButton = {
            TextButton(onClick = viewModel::closeEditor) { Text(string("inc.action.cancel")) }
        },
    )
}
