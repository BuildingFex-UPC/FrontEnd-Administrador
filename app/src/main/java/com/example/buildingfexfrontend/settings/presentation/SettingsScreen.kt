package com.example.buildingfexfrontend.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.buildingfexfrontend.core.di.AppContainer
import com.example.buildingfexfrontend.core.i18n.string
import com.example.buildingfexfrontend.core.ui.appViewModel
import com.example.buildingfexfrontend.core.ui.components.AppButton
import com.example.buildingfexfrontend.core.ui.components.AppOutlinedButton
import com.example.buildingfexfrontend.core.ui.components.AppTextField
import com.example.buildingfexfrontend.core.ui.components.ConfirmDialog
import com.example.buildingfexfrontend.core.ui.components.ErrorState
import com.example.buildingfexfrontend.core.ui.components.FullScreenLoading
import com.example.buildingfexfrontend.core.ui.components.KeyValue
import com.example.buildingfexfrontend.core.ui.components.OutlinedCardBox
import com.example.buildingfexfrontend.core.ui.components.SectionCard
import com.example.buildingfexfrontend.core.ui.components.StatusChip
import com.example.buildingfexfrontend.core.ui.components.VerticalGap
import com.example.buildingfexfrontend.subscription.domain.model.SubscriptionPlan
import com.example.buildingfexfrontend.ui.theme.BfSuccess
import com.example.buildingfexfrontend.ui.theme.BfSuccessContainer

@Composable
fun SettingsScreen(container: AppContainer) {
    val viewModel: SettingsViewModel = appViewModel {
        SettingsViewModel(container.auth, container.subscription)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    state.openCheckoutUrl?.let { checkoutUrl ->
        com.example.buildingfexfrontend.core.ui.components.CheckoutWebView(
            url = checkoutUrl,
            onRedirect = viewModel::onCheckoutRedirect,
            onDismiss = viewModel::consumeCheckoutUrl,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = string("set.title"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = string("set.subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VerticalGap(16)

        when {
            state.loading -> FullScreenLoading()
            state.loadError != null -> ErrorState(state.loadError!!, onRetry = viewModel::load)
            else -> {
                val profile = state.profile
                SectionCard(title = string("set.profile.adminTitle")) {
                    if (profile == null) {
                        Text(
                            text = string("set.profile.loadFailed"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        KeyValue(label = string("set.profile.name"), value = profile.name.ifBlank { "—" })
                        profile.email?.takeIf { it.isNotBlank() }?.let {
                            KeyValue(label = string("set.profile.email"), value = it)
                        }
                        profile.dni?.takeIf { it.isNotBlank() }?.let { KeyValue(label = string("set.profile.dni"), value = it) }
                        profile.address?.takeIf { it.isNotBlank() }?.let { KeyValue(label = string("set.profile.address"), value = it) }
                        profile.company?.takeIf { it.isNotBlank() }?.let { KeyValue(label = string("set.profile.company"), value = it) }
                        profile.ruc?.takeIf { it.isNotBlank() }?.let { KeyValue(label = string("set.profile.ruc"), value = it) }
                        VerticalGap(12)
                        AppOutlinedButton(
                            text = string("set.profile.edit"),
                            onClick = viewModel::openEditProfile,
                        )
                        VerticalGap(8)
                        AppOutlinedButton(
                            text = string("set.pw.change"),
                            onClick = viewModel::openChangePassword,
                        )
                    }
                }

                VerticalGap(16)
                val subscription = state.subscription
                SectionCard(title = string("set.plans.title")) {
                    Text(
                        text = string("set.plans.residents")
                            .replace("{current}", (subscription?.residentsCount ?: 0).toString())
                            .replace(
                                "{limit}",
                                (subscription?.residentLimit ?: SubscriptionPlan.FREE.residentLimit).toString(),
                            ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    state.message?.let {
                        VerticalGap(8)
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = BfSuccess,
                        )
                    }
                    state.error?.let {
                        VerticalGap(8)
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    VerticalGap(12)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SubscriptionPlan.entries.forEach { plan ->
                            PlanRow(
                                plan = plan,
                                selected = subscription?.planId == plan.id,
                                busy = state.busyPlanId == plan.id,
                                enabled = state.busyPlanId == null,
                                onSelect = { viewModel.requestPlanChange(plan.id) },
                            )
                        }
                    }
                }

                VerticalGap(16)
                SectionCard(title = string("set.session.title")) {
                    AppButton(
                        text = string("set.session.logout"),
                        destructive = true,
                        onClick = viewModel::logout,
                    )
                }
            }
        }
        VerticalGap(24)
    }

    state.message?.let { message ->
        ConfirmDialog(
            title = string("set.title"),
            message = message,
            confirmText = string("set.message.ack"),
            onConfirm = viewModel::dismissMessage,
            onDismiss = viewModel::dismissMessage,
        )
    }

    // Changing plans has billing consequences: always confirm first.
    state.confirmPlanId?.let { planId ->
        val plan = SubscriptionPlan.entries.firstOrNull { it.id == planId }
        if (plan != null) {
            ConfirmDialog(
                title = string("set.plans.confirm.title"),
                message = string("set.plans.confirm.message")
                    .replace("{plan}", plan.label)
                    .replace("{price}", plan.monthlyPricePen.toString())
                    .replace("{limit}", plan.residentLimit.toString()),
                confirmText = string("set.plans.confirm.confirm"),
                onConfirm = viewModel::confirmPlanChange,
                onDismiss = viewModel::cancelPlanChange,
            )
        }
    }

    if (state.showEditProfile) {
        EditProfileDialog(state = state, viewModel = viewModel)
    }

    if (state.showChangePassword) {
        ChangePasswordDialog(state = state, viewModel = viewModel)
    }
}

@Composable
private fun EditProfileDialog(state: SettingsUiState, viewModel: SettingsViewModel) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = { if (!state.editLoading) viewModel.closeEditProfile() },
        title = { Text(string("set.edit.title")) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppTextField(
                    value = state.editName,
                    onValueChange = viewModel::onEditNameChange,
                    label = string("set.profile.name"),
                    enabled = !state.editLoading,
                )
                AppTextField(
                    value = state.editEmail,
                    onValueChange = viewModel::onEditEmailChange,
                    label = string("set.profile.email"),
                    enabled = !state.editLoading,
                )
                AppTextField(
                    value = state.editDni,
                    onValueChange = viewModel::onEditDniChange,
                    label = string("set.profile.dni"),
                    enabled = !state.editLoading,
                )
                AppTextField(
                    value = state.editAddress,
                    onValueChange = viewModel::onEditAddressChange,
                    label = string("set.profile.address"),
                    enabled = !state.editLoading,
                )
                AppTextField(
                    value = state.editCompany,
                    onValueChange = viewModel::onEditCompanyChange,
                    label = string("set.profile.company"),
                    enabled = !state.editLoading,
                )
                AppTextField(
                    value = state.editRuc,
                    onValueChange = viewModel::onEditRucChange,
                    label = string("set.profile.ruc"),
                    enabled = !state.editLoading,
                )
                state.editError?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = viewModel::saveProfile,
                enabled = !state.editLoading,
            ) {
                Text(
                    if (state.editLoading) string("set.edit.saving")
                    else string("set.edit.save")
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = viewModel::closeEditProfile,
                enabled = !state.editLoading,
            ) {
                Text(string("set.edit.cancel"))
            }
        },
    )
}

@Composable
private fun ChangePasswordDialog(state: SettingsUiState, viewModel: SettingsViewModel) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = { if (!state.passwordLoading) viewModel.closeChangePassword() },
        title = { Text(string("set.pw.title")) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = string("set.pw.new"),
                    password = true,
                    enabled = !state.passwordLoading,
                    supportingText = string("set.pw.hint"),
                )
                AppTextField(
                    value = state.passwordConfirm,
                    onValueChange = viewModel::onPasswordConfirmChange,
                    label = string("set.pw.confirm"),
                    password = true,
                    enabled = !state.passwordLoading,
                )
                state.passwordError?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = viewModel::changePassword,
                enabled = !state.passwordLoading,
            ) {
                Text(
                    if (state.passwordLoading) string("set.pw.saving")
                    else string("set.pw.save")
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = viewModel::closeChangePassword,
                enabled = !state.passwordLoading,
            ) {
                Text(string("set.edit.cancel"))
            }
        },
    )
}

@Composable
private fun PlanRow(
    plan: SubscriptionPlan,
    selected: Boolean,
    busy: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
) {
    OutlinedCardBox {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(plan.label, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                if (selected) {
                    StatusChip(text = string("set.plans.current"), container = BfSuccessContainer, content = BfSuccess)
                }
            }
            Text(
                text = string("set.plans.priceLine")
                    .replace("{price}", plan.monthlyPricePen.toString())
                    .replace("{limit}", plan.residentLimit.toString()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VerticalGap(8)
            AppOutlinedButton(
                text = when {
                    busy -> string("set.plans.processing")
                    selected && plan == SubscriptionPlan.FREE -> string("set.plans.currentPlan")
                    plan == SubscriptionPlan.FREE -> string("set.plans.switchToFree")
                    selected -> string("set.plans.currentPlan")
                    else -> string("set.plans.choose").replace("{plan}", plan.label)
                },
                enabled = enabled && !(selected),
                onClick = onSelect,
            )
        }
    }
}
