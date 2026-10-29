package com.example.buildingfexfrontend.residents.presentation

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.buildingfexfrontend.core.di.AppContainer
import com.example.buildingfexfrontend.core.i18n.string
import com.example.buildingfexfrontend.core.i18n.stringOf
import com.example.buildingfexfrontend.core.ui.appViewModel
import com.example.buildingfexfrontend.core.ui.components.AppButton
import com.example.buildingfexfrontend.core.ui.components.AppTextField
import com.example.buildingfexfrontend.core.ui.components.ConfirmDialog
import com.example.buildingfexfrontend.core.ui.components.EmptyState
import com.example.buildingfexfrontend.core.ui.components.ErrorState
import com.example.buildingfexfrontend.core.ui.components.FullScreenLoading
import com.example.buildingfexfrontend.core.ui.components.SectionCard
import com.example.buildingfexfrontend.core.ui.components.VerticalGap
import com.example.buildingfexfrontend.residents.domain.DepartmentNumber
import com.example.buildingfexfrontend.residents.domain.model.Resident

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentsScreen(container: AppContainer) {
    val viewModel: ResidentsViewModel = appViewModel { ResidentsViewModel(container.residents) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::openAddDialog) {
                Icon(Icons.Filled.Add, contentDescription = string("res.fab.add"))
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
            state.planUsage?.let { (current, max) ->
                Text(
                    text = string("res.planUsage")
                        .replace("{current}", "$current")
                        .replace("{max}", "$max"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                VerticalGap(8)
            }
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(string("res.search.placeholder")) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
            )
            VerticalGap(12)

            when {
                state.loading -> FullScreenLoading(string("res.loading"))
                state.error != null -> ErrorState(state.error!!, onRetry = viewModel::load)
                state.filtered.isEmpty() -> EmptyState(
                    if (state.residents.isEmpty()) string("res.empty.none")
                    else string("res.empty.noMatch"),
                )

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp),
                ) {
                    items(state.filtered, key = { it.id }) { resident ->
                        ResidentRow(
                            resident = resident,
                            viewModel = viewModel,
                            onDelete = { viewModel.openDeleteDialog(resident) },
                        )
                    }
                }
            }
        }
    }

    if (state.showAddDialog) {
        AddResidentDialog(state, viewModel)
    }

    state.deleteTarget?.let { target ->
        ConfirmDialog(
            title = string("res.delete.title"),
            message = buildString {
                append(string("res.delete.message").replace("{name}", target.name))
                if (state.deleteLoading) {
                    append("\n")
                    append(string("res.delete.calculating"))
                } else {
                    val preview = state.deletePreview
                    if (preview != null && preview.reservations > 0) {
                        append(
                            string("res.delete.preview")
                                .replace("{reservations}", preview.reservations.toString()),
                        )
                    } else if (preview != null) {
                        append(string("res.delete.previewNoAccount"))
                    }
                }
            },
            confirmText = string("res.delete.confirm"),
            destructive = true,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::closeDeleteDialog,
        )
    }

    state.message?.let { message ->
        ConfirmDialog(
            title = string("res.message.title"),
            message = message,
            confirmText = string("res.message.confirm"),
            onConfirm = viewModel::dismissMessage,
            onDismiss = viewModel::dismissMessage,
        )
    }
}

@Composable
private fun ResidentRow(
    resident: Resident,
    viewModel: ResidentsViewModel,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val shareChooserTitle = string("res.share.chooser")
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(resident.name, fontWeight = FontWeight.SemiBold)
                Text(
                    text = string("res.row.floorDept")
                        .replace("{floor}", resident.floor)
                        .replace("{dept}", DepartmentNumber.departmentOf(resident.code)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (resident.hasCredentials) {
                    Text(
                        text = string("res.row.activeAccount"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Text(
                        text = string("res.row.noAccount"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (!resident.hasCredentials) {
                IconButton(
                    onClick = {
                        // Copy ONLY the code (no surrounding text) so it can be
                        // pasted verbatim into the resident app to create the
                        // account without any chance of a copy artifact.
                        clipboard.setText(AnnotatedString(resident.code))
                        viewModel.notifyCodeCopied()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = string("res.copy.cd")
                            .replace("{name}", resident.name),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                IconButton(
                    onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, inviteText(resident))
                        }
                        runCatching {
                            context.startActivity(
                                Intent.createChooser(send, shareChooserTitle),
                            )
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = string("res.share.cd")
                            .replace("{name}", resident.name),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = string("res.delete.cd"),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

/** Plain-text invitation shared via the system sheet (code is unique: department + random suffix). */
private fun inviteText(resident: Resident): String = buildString {
    val department = DepartmentNumber.departmentOf(resident.code)
    append(
        stringOf("res.invite.header")
            .replace("{name}", resident.name)
            .replace("{floor}", resident.floor)
            .replace("{dept}", department),
    )
    append(stringOf("res.invite.code").replace("{code}", resident.code))
    append(stringOf("res.invite.instructions"))
}

@Composable
private fun AddResidentDialog(state: ResidentsUiState, viewModel: ResidentsViewModel) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = viewModel::closeAddDialog,
        title = { Text(string("res.add.title")) },
        text = {
            Column {
                AppTextField(
                    value = state.addName,
                    onValueChange = viewModel::onAddNameChange,
                    label = string("res.field.name"),
                )
                VerticalGap(12)
                AppTextField(
                    value = state.addDepartment,
                    onValueChange = viewModel::onAddDepartmentChange,
                    label = string("res.field.department"),
                    placeholder = string("res.field.departmentPlaceholder"),
                    supportingText = state.addFloorHint?.let {
                        string("res.field.floorDetected").replace("{floor}", it)
                    } ?: string("res.field.departmentHelp"),
                )
                state.addError?.let {
                    VerticalGap(8)
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = viewModel::confirmAdd,
                enabled = !state.adding,
            ) {
                Text(if (state.adding) string("res.add.saving") else string("res.add.confirm"))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = viewModel::closeAddDialog) {
                Text(string("res.add.cancel"))
            }
        },
    )
}
