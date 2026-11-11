package com.example.buildingfexfrontend.socialspaces.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.buildingfexfrontend.core.di.AppContainer
import com.example.buildingfexfrontend.core.i18n.string
import com.example.buildingfexfrontend.core.ui.appViewModel
import com.example.buildingfexfrontend.core.ui.components.AppDialog
import com.example.buildingfexfrontend.core.ui.components.AppImage
import com.example.buildingfexfrontend.core.ui.components.AppOutlinedButton
import com.example.buildingfexfrontend.core.ui.components.AppTextField
import com.example.buildingfexfrontend.core.ui.components.ConfirmDialog
import com.example.buildingfexfrontend.core.ui.components.EmptyState
import com.example.buildingfexfrontend.core.ui.components.ErrorState
import com.example.buildingfexfrontend.core.ui.components.FormError
import com.example.buildingfexfrontend.core.ui.components.FullScreenLoading
import com.example.buildingfexfrontend.core.ui.components.OutlinedCardBox
import com.example.buildingfexfrontend.core.ui.components.PhotoPickerField
import com.example.buildingfexfrontend.core.ui.components.SectionCard
import com.example.buildingfexfrontend.core.ui.components.VerticalGap
import com.example.buildingfexfrontend.socialspaces.domain.model.Space

@Composable
fun SpacesScreen(container: AppContainer) {
    val viewModel: SpacesViewModel = appViewModel { SpacesViewModel(container.spaces) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = string("spaces.title"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = string("spaces.subtitle"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        VerticalGap(16)

        when {
            state.loading -> FullScreenLoading()
            state.error != null -> ErrorState(state.error!!, onRetry = viewModel::load)
            else -> {
                SectionCard(
                    title = string("spaces.catalogTitle"),
                    actions = { TextButton(onClick = viewModel::openAdd) { Text(string("spaces.new")) } },
                ) {
                    if (state.spaces.isEmpty()) {
                        EmptyState(string("spaces.empty"))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.spaces.forEach { space ->
                                SpaceRow(
                                    space = space,
                                    onEdit = { viewModel.openEdit(space) },
                                    onDelete = { viewModel.requestDelete(space) },
                                )
                            }
                        }
                    }
                }
            }
        }
        VerticalGap(24)
    }

    if (state.dialog != SpaceDialog.NONE) {
        val isEdit = state.dialog == SpaceDialog.EDIT
        AppDialog(
            title = if (isEdit) string("spaces.editTitle") else string("spaces.addTitle"),
            onDismiss = viewModel::closeDialog,
            onConfirm = viewModel::submit,
            busy = state.busy || state.photoProcessing,
        ) {
            AppTextField(
                value = state.name,
                onValueChange = { viewModel.onFieldChange("name", it) },
                label = string("spaces.fieldName"),
            )

            VerticalGap(12)
            AppTextField(
                value = state.cost,
                onValueChange = { viewModel.onFieldChange("cost", it) },
                label = string("spaces.fieldCost"),
            )

            VerticalGap(12)
            AppTextField(
                value = state.description,
                onValueChange = { viewModel.onFieldChange("description", it) },
                label = string("spaces.fieldDescription"),
                singleLine = false,
            )
            VerticalGap(12)
            AppTextField(
                value = state.capacity,
                onValueChange = { viewModel.onFieldChange("capacity", it) },
                label = string("spaces.fieldCapacity"),
            )
            VerticalGap(12)
            PhotoPickerField(
                dataUrl = state.imageUrl,
                onPhotoChange = viewModel::setPhoto,
                onClear = viewModel::clearPhoto,
                onError = viewModel::photoProcessingFailed,
                onProcessingChange = viewModel::onPhotoProcessing,
                enabled = !state.busy,
                label = string("spaces.fieldImage"),
            )
            state.formError?.let {
                VerticalGap(8)
                FormError(it)
            }
        }
    }

    state.deleteTarget?.let { target ->
        ConfirmDialog(
            title = string("spaces.deleteTitle"),
            message = string("spaces.deleteMessage")
                .replace("{name}", target.name),
            confirmText = string("spaces.deleteAction"),
            destructive = true,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete,
        )
    }

    state.message?.let { message ->
        ConfirmDialog(
            title = string("spaces.title"),
            message = message,
            confirmText = string("spaces.understood"),
            onConfirm = viewModel::dismissMessage,
            onDismiss = viewModel::dismissMessage,
        )
    }
}


@Composable
private fun SpaceRow(space: Space, onEdit: () -> Unit, onDelete: () -> Unit) {
    OutlinedCardBox {
        Column {
            if (space.imageUrl.isNotBlank()) {
                AppImage(
                    source = space.imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(10.dp)),
                )
                VerticalGap(10)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(space.name, fontWeight = FontWeight.SemiBold)
                    if (space.description.isNotBlank()) {
                        Text(
                            text = space.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (space.capacity != null) {
                        Text(
                            text = string("spaces.capacity")
                                .replace("{n}", "${space.capacity}"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = string("spaces.cost").replace(
                            "{n}",
                            String.format(java.util.Locale.US, "%.2f", space.cost),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            VerticalGap(8)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppOutlinedButton(text = string("spaces.edit"), onClick = onEdit)
                AppOutlinedButton(text = string("spaces.deleteAction"), onClick = onDelete)
            }
        }
    }
}

