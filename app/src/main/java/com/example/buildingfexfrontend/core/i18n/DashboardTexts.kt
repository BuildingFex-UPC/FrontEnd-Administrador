package com.example.buildingfexfrontend.core.i18n

/**
 * ES/EN texts for both dashboards (`dash.*`).
 * Feature-owned by dashboard/presentation (Admin/Resident screens, VMs, Charts).
 */
val DashboardTexts: Map<String, TextPair> = mapOf(
    // Admin dashboard
    "dash.adminTitle" to ("Dashboard" to "Dashboard"),
    "dash.adminSubtitle" to ("Resumen operativo del edificio." to "Operational summary of the building."),
    "dash.residents" to ("Residentes" to "Residents"),
    "dash.occupied" to ("Ocupados" to "Occupied"),
    "dash.empty" to ("Vacíos" to "Vacant"),
    "dash.totalDebt" to ("Deuda total" to "Total debt"),
    "dash.cashflowTitle" to ("Ingresos vs egresos (6 meses)" to "Income vs expenses (6 months)"),
    "dash.noMovements" to ("Aún no hay movimientos para graficar." to "There are no transactions to chart yet."),
    "dash.recentIncidents" to ("Incidencias recientes" to "Recent incidents"),
    "dash.noIncidents" to ("No hay incidencias registradas." to "There are no registered incidents."),
    "dash.reservations" to ("Reservas" to "Reservations"),
    "dash.upcomingReservations" to ("Próximas reservas" to "Upcoming reservations"),
    "dash.noReservations" to ("No hay reservas registradas." to "There are no registered reservations."),
    "dash.statusOpen" to ("Abierta" to "Open"),
    "dash.statusInProgress" to ("En curso" to "In progress"),
    "dash.statusResolved" to ("Resuelta" to "Resolved"),

    // Charts legend
    "dash.legendIncome" to ("Ingresos" to "Income"),
    "dash.legendExpenses" to ("Egresos" to "Expenses"),

    // Resident dashboard
    "dash.greeting" to ("Hola, {name}" to "Hello, {name}"),
    "dash.residentFallback" to ("residente" to "resident"),
    "dash.residentSubtitle" to ("Resumen de tu edificio." to "Summary of your building."),
    "dash.pendingBalance" to ("Saldo pendiente" to "Pending balance"),
    "dash.openIncidentsLabel" to ("Incidencias abiertas" to "Open incidents"),
    "dash.todayReservations" to ("Reservas hoy" to "Reservations today"),
    "dash.activeAnnouncements" to ("Comunicados activos" to "Active announcements"),
    "dash.paymentsTitle" to ("Estado de pagos" to "Payment status"),
    "dash.noFees" to ("Aún no tienes cuotas registradas." to "You don't have any registered fees yet."),
    "dash.slicePaid" to ("Pagado" to "Paid"),
    "dash.slicePending" to ("Pendiente" to "Pending"),
    "dash.sliceOverdue" to ("Vencido" to "Overdue"),
    "dash.overdueNotice" to ("Tienes {amount} vencidos." to "You have {amount} overdue."),
    "dash.incidentsTitle" to ("Incidencias" to "Incidents"),
    "dash.chipOpen" to ("Abiertas: {n}" to "Open: {n}"),
    "dash.chipInProgress" to ("En curso: {n}" to "In progress: {n}"),
    "dash.chipResolved" to ("Resueltas: {n}" to "Resolved: {n}"),
    "dash.upcomingReservations" to ("Próximas reservas" to "Upcoming reservations"),
    "dash.noUpcomingReservations" to ("No tienes reservas próximas." to "You have no upcoming reservations."),
    "dash.defaultSpace" to ("Espacio social" to "Social space"),
    "dash.today" to ("Hoy" to "Today"),
    "dash.announcementsTitle" to ("Comunicados" to "Announcements"),
)
