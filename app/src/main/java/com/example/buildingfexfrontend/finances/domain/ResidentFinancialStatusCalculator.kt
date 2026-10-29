package com.example.buildingfexfrontend.finances.domain

import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.finances.domain.model.Receipt
import com.example.buildingfexfrontend.residents.domain.model.Resident
import java.time.LocalDate

data class ResidentFinancialStatus(
    val resident: Resident,
    val nextPaymentDate: String,
    val daysUntilNextPayment: Int,
    /** 'None' (no receipts yet) | 'Paid' | 'Pending' | 'Overdue' */
    val overallStatus: String,
    val receipts: List<Receipt>,
    val pendingAmount: Double,
) {
    val isOverdue: Boolean get() = overallStatus == "Overdue"
    val isPaid: Boolean get() = overallStatus == "Paid"

    /** True when the resident has no receipts at all (nothing is due yet). */
    val hasNoReceipts: Boolean get() = overallStatus == "None"
}

/**
 * Port of the web `financesStore.getResidentFinancialStatus` business rules:
 * next payment date, countdown and overall status per resident.
 */
object ResidentFinancialStatusCalculator {

    fun calculate(
        residents: List<Resident>,
        receipts: List<Receipt>,
        today: LocalDate = LocalDate.now(),
    ): List<ResidentFinancialStatus> {
        val todayStr = Dates.localDateIsoString(java.util.Date.from(today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()))
        return residents.map { resident ->
            val own = receipts.filter { it.residentId == resident.id }
            var nextPaymentDate = parseDate(resident.admissionDate) ?: parseDate("2024-01-01")!!
            nextPaymentDate = nextPaymentDate.plusDays(30)

            if (own.isNotEmpty()) {
                val unpaid = own.filter { it.status != Receipt.STATUS_PAID }
                if (unpaid.isNotEmpty()) {
                    val oldest = unpaid.minOfOrNull { parseDate(it.dueDate) ?: today }
                    if (oldest != null) nextPaymentDate = oldest
                } else {
                    val latest = own.maxOfOrNull { parseDate(it.dueDate) ?: today }
                    if (latest != null) nextPaymentDate = latest.plusDays(30)
                }
            }

            val daysUntil = (nextPaymentDate.toEpochDay() - today.toEpochDay()).toInt()

            val hasOverdue = own.any {
                it.status == Receipt.STATUS_OVERDUE ||
                    (it.status == Receipt.STATUS_PENDING && todayStr >= it.dueDate)
            }
            val hasPending = own.any { it.status == Receipt.STATUS_PENDING }

            // A resident with no receipts has nothing pending: showing
            // "Pending"/"Overdue" here used to look like a real debt in the
            // admin app while the resident account (and its dashboard) showed
            // nothing, because no receipt actually exists.
            val overall = when {
                own.isEmpty() -> "None"
                hasOverdue -> "Overdue"
                hasPending -> "Pending"
                else -> "Paid"
            }

            ResidentFinancialStatus(
                resident = resident,
                nextPaymentDate = nextPaymentDate.toString(),
                daysUntilNextPayment = daysUntil,
                overallStatus = overall,
                receipts = own,
                pendingAmount = own.filter { it.status != Receipt.STATUS_PAID }
                    .sumOf { it.amount + it.lateFee },
            )
        }
    }

    private fun parseDate(value: String?): LocalDate? = Dates.ymdOrNull(value)
}
