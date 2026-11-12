package com.example.buildingfexfrontend.dashboard.presentation

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
import com.example.buildingfexfrontend.core.i18n.stringOf
import com.example.buildingfexfrontend.core.ui.appViewModel
import com.example.buildingfexfrontend.core.ui.components.EmptyState
import com.example.buildingfexfrontend.core.ui.components.ErrorState
import com.example.buildingfexfrontend.core.ui.components.FullScreenLoading
import com.example.buildingfexfrontend.core.ui.components.OutlinedCardBox
import com.example.buildingfexfrontend.core.ui.components.SectionCard
import com.example.buildingfexfrontend.core.ui.components.StatCard
import com.example.buildingfexfrontend.core.ui.components.StatusChip
import com.example.buildingfexfrontend.core.ui.components.VerticalGap
import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.incidents.domain.model.IncidentStatus
import com.example.buildingfexfrontend.ui.theme.BfError
import com.example.buildingfexfrontend.ui.theme.BfErrorContainer
import com.example.buildingfexfrontend.ui.theme.BfSuccess
import com.example.buildingfexfrontend.ui.theme.BfSuccessContainer
import com.example.buildingfexfrontend.ui.theme.BfWarning
import com.example.buildingfexfrontend.ui.theme.BfWarningContainer

@Composable
fun AdminDashboardScreen(container: AppContainer) {
    val viewModel: AdminDashboardViewModel = appViewModel {
        AdminDashboardViewModel(
            container.finances,
            container.collections,
            container.incidents,
            container.reservations,
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = string("dash.adminTitle"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = string("dash.adminSubtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VerticalGap(16)

        when {
            state.loading -> FullScreenLoading()
            state.error != null -> ErrorState(state.error!!, onRetry = viewModel::load)
            else -> {
                val kpi = state.kpi
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        label = string("dash.residents"),
                        value = kpi.totalResidents.toString(),
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = string("dash.occupied"),
                        value = kpi.occupiedUnits.toString(),
                        modifier = Modifier.weight(1f),
                        accent = com.example.buildingfexfrontend.ui.theme.BfSuccess,
                    )
                }
                VerticalGap(8)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        label = string("dash.empty"),
                        value = kpi.emptyUnits.toString(),
                        modifier = Modifier.weight(1f),
                        accent = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    StatCard(
                        label = string("dash.totalDebt"),
                        value = Dates.currency(kpi.totalDebt),
                        modifier = Modifier.weight(1f),
                        accent = MaterialTheme.colorScheme.error,
                    )
                }

                VerticalGap(16)
                SectionCard(title = string("dash.cashflowTitle")) {
                    if (state.chart.hasData) {
                        BarChart(
                            labels = state.chart.labels,
                            income = state.chart.income,
                            expenses = state.chart.expenses,
                        )
                    } else {
                        EmptyState(string("dash.noMovements"))
                    }
                }

                VerticalGap(16)
                SectionCard(title = string("dash.reservations")) {
                    if (state.todayReservations.isEmpty() && state.upcomingReservations.isEmpty()) {
                        EmptyState(string("dash.noReservations"))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (state.todayReservations.isNotEmpty()) {
                                Text(
                                    text = string("dash.todayReservations"),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                state.todayReservations.forEach { reservation ->
                                    ReservationRow(reservation)
                                }
                            }
                            if (state.upcomingReservations.isNotEmpty()) {
                                Text(
                                    text = string("dash.upcomingReservations"),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                state.upcomingReservations.forEach { reservation ->
                                    ReservationRow(reservation)
                                }
                            }
                        }
                    }
                }

                VerticalGap(16)
                SectionCard(title = string("dash.recentIncidents")) {
                    if (state.recentIncidents.isEmpty()) {
                        EmptyState(string("dash.noIncidents"))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.recentIncidents.forEach { incident ->
                                OutlinedCardBox {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                text = incident.description,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                            )
                                            Text(
                                                text = "${incident.residentName.ifBlank { "—" }} · " +
                                                    "${Dates.displayDateTime(incident.createdAt)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        StatusChip(
                                            text = incidentStatusLabel(incident.status),
                                            container = incidentContainer(incident.status),
                                            content = incidentContent(incident.status),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        VerticalGap(24)
    }
}

@Composable
private fun ReservationRow(reservation: com.example.buildingfexfrontend.socialspaces.domain.model.Reservation) {
    OutlinedCardBox {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = reservation.residentName.ifBlank { "—" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "${Dates.displayDate(reservation.date)} · " +
                        "${reservation.startTime} – ${reservation.endTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun incidentStatusLabel(status: String): String = when (status) {
    IncidentStatus.OPEN -> stringOf("dash.statusOpen")
    IncidentStatus.IN_PROGRESS -> stringOf("dash.statusInProgress")
    IncidentStatus.RESOLVED -> stringOf("dash.statusResolved")
    else -> status
}

private fun incidentContainer(status: String) = when (status) {
    IncidentStatus.OPEN -> BfErrorContainer
    IncidentStatus.IN_PROGRESS -> BfWarningContainer
    IncidentStatus.RESOLVED -> BfSuccessContainer
    else -> BfWarningContainer
}

private fun incidentContent(status: String) = when (status) {
    IncidentStatus.OPEN -> BfError
    IncidentStatus.IN_PROGRESS -> BfWarning
    IncidentStatus.RESOLVED -> BfSuccess
    else -> BfWarning
}
