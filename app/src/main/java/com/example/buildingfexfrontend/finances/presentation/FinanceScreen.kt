package com.example.buildingfexfrontend.finances.presentation

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
import com.example.buildingfexfrontend.core.ui.components.EmptyState
import com.example.buildingfexfrontend.core.ui.components.ErrorState
import com.example.buildingfexfrontend.core.ui.components.FullScreenLoading
import com.example.buildingfexfrontend.core.ui.components.SectionCard
import com.example.buildingfexfrontend.core.ui.components.StatusChip
import com.example.buildingfexfrontend.core.ui.components.VerticalGap
import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.finances.domain.ResidentFinancialStatus
import com.example.buildingfexfrontend.residents.domain.DepartmentNumber
import com.example.buildingfexfrontend.ui.theme.BfError
import com.example.buildingfexfrontend.ui.theme.BfErrorContainer
import com.example.buildingfexfrontend.ui.theme.BfSuccess
import com.example.buildingfexfrontend.ui.theme.BfSuccessContainer
import com.example.buildingfexfrontend.ui.theme.BfWarning
import com.example.buildingfexfrontend.ui.theme.BfWarningContainer

@Composable
fun FinanceScreen(container: AppContainer) {
    val viewModel: FinanceViewModel = appViewModel { FinanceViewModel(container.finances) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = string("fin.title"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = string("fin.subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VerticalGap(16)

        when {
            state.loading -> FullScreenLoading()
            state.error != null -> ErrorState(state.error!!, onRetry = viewModel::load)
            else -> {
                SectionCard(title = string("fin.settings.title")) {
                    AppTextField(
                        value = state.baseInput,
                        onValueChange = viewModel::onBaseChange,
                        label = string("fin.settings.base"),
                    )
                    VerticalGap(12)
                    AppTextField(
                        value = state.lateInput,
                        onValueChange = viewModel::onLateChange,
                        label = string("fin.settings.late"),
                    )
                    VerticalGap(12)
                    AppButton(
                        text = if (state.savingSettings) string("fin.settings.saving") else string("fin.settings.save"),
                        onClick = viewModel::saveSettings,
                        loading = state.savingSettings,
                    )
                }

                VerticalGap(16)
                SectionCard(title = string("fin.automation.title")) {
                    AppOutlinedButton(
                        text = if (state.busy) string("fin.processing") else string("fin.automation.generate"),
                        enabled = !state.busy,
                        onClick = viewModel::generateMonthlyReceipts,
                    )
                    VerticalGap(8)
                    AppOutlinedButton(
                        text = if (state.busy) string("fin.processing") else string("fin.automation.overdue"),
                        enabled = !state.busy,
                        onClick = viewModel::createOverdueReceipts,
                    )
                }

                VerticalGap(16)
                SectionCard(title = string("fin.status.title")) {
                    if (state.statuses.isEmpty()) {
                        EmptyState(string("fin.status.empty"))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.statuses.forEach { status ->
                                ResidentStatusRow(
                                    status = status,
                                    busy = state.busy,
                                    onReconcile = { viewModel.reconcile(status) },
                                    onGenerate = { viewModel.generateFor(status) },
                                    onWaive = { viewModel.waiveLateFees(status) },
                                )
                            }
                        }
                    }
                }
            }
        }
        VerticalGap(24)
    }

    state.message?.let { message ->
        ConfirmDialog(
            title = string("fin.title"),
            message = message,
            confirmText = string("fin.understood"),
            onConfirm = viewModel::dismissMessage,
            onDismiss = viewModel::dismissMessage,
        )
    }
}

@Composable
private fun ResidentStatusRow(
    status: ResidentFinancialStatus,
    busy: Boolean,
    onReconcile: () -> Unit,
    onGenerate: () -> Unit,
    onWaive: () -> Unit,
) {
    val (chipText, chipContainer, chipContent) = when (status.overallStatus) {
        "Overdue" -> Triple(string("fin.chip.overdue"), BfErrorContainer, BfError)
        "Paid" -> Triple(string("fin.chip.paid"), BfSuccessContainer, BfSuccess)
        "None" -> Triple(
            string("fin.chip.none"),
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
        )
        else -> Triple(string("fin.chip.pending"), BfWarningContainer, BfWarning)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(status.resident.name, fontWeight = FontWeight.SemiBold)
                Text(
                    text = string("fin.row.floorDept")
                        .replace("{floor}", status.resident.floor)
                        .replace("{dept}", DepartmentNumber.departmentOf(status.resident.code)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusChip(text = chipText, container = chipContainer, content = chipContent)
        }
        VerticalGap(6)
        Text(
            text = if (status.hasNoReceipts) {
                string("fin.row.noReceipts")
            } else {
                string("fin.row.nextPayment")
                    .replace("{date}", Dates.displayDate(status.nextPaymentDate))
                    .replace("{days}", status.daysUntilNextPayment.toString())
                    .replace("{amount}", Dates.currency(status.pendingAmount))
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VerticalGap(8)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (status.hasNoReceipts) {
                // No receipts yet: offer to issue the first one so the debt
                // shows up in the resident's app too.
                AppOutlinedButton(
                    text = string("fin.action.issue"),
                    modifier = Modifier.weight(1f),
                    enabled = !busy,
                    onClick = onGenerate,
                )
            } else if (status.overallStatus != "Paid") {
                AppOutlinedButton(
                    text = string("fin.action.reconcile"),
                    modifier = Modifier.weight(1f),
                    enabled = !busy,
                    onClick = onReconcile,
                )
            } else {
                AppOutlinedButton(
                    text = string("fin.action.issue"),
                    modifier = Modifier.weight(1f),
                    enabled = !busy,
                    onClick = onGenerate,
                )
            }
            if (!status.hasNoReceipts) {
                AppOutlinedButton(
                    text = string("fin.action.waiveFee"),
                    modifier = Modifier.weight(1f),
                    enabled = !busy,
                    onClick = onWaive,
                )
            }
        }
    }
}
