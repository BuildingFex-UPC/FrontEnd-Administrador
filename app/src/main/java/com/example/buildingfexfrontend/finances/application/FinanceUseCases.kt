package com.example.buildingfexfrontend.finances.application

import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.finances.domain.ResidentFinancialStatus
import com.example.buildingfexfrontend.finances.domain.ResidentFinancialStatusCalculator
import com.example.buildingfexfrontend.finances.domain.model.DashboardKpi
import com.example.buildingfexfrontend.finances.domain.model.FinanceSettings
import com.example.buildingfexfrontend.finances.domain.model.Payment
import com.example.buildingfexfrontend.finances.domain.model.Receipt
import com.example.buildingfexfrontend.finances.domain.repository.FinancesRepository
import com.example.buildingfexfrontend.finances.domain.repository.ReceiptPatchData

/**
 * Admin-side finance use cases: settings, receipt generation, reconciliation
 * and the automatic late-fee rule (ported from the web finances store).
 */
class FinanceUseCases(
    private val repository: FinancesRepository,
    @Suppress("unused") private val session: SessionRepository,
) {
    val settings = GetSettingsUseCase(repository)
    val updateSettings = UpdateSettingsUseCase(repository)
    val residentStatuses = ResidentStatusesUseCase(repository)
    val generateMonthlyReceipts = GenerateMonthlyReceiptsUseCase(repository)
    val generateInitialReceipt = GenerateInitialReceiptUseCase(repository)
    val createOverdueReceipts = CreateOverdueReceiptsUseCase(repository)
    val reconcilePayment = ReconcilePaymentUseCase(repository)
    val autoApplyLateFees = AutoApplyLateFeesUseCase(repository)
    val waiveLateFees = WaiveLateFeesUseCase(repository)
    val kpis = GetKpisUseCase(repository)
    val allPayments = GetAllPaymentsUseCase(repository)
    val receipts = ListReceiptsUseCase(repository)
}

class GetSettingsUseCase(private val repository: FinancesRepository) {
    suspend operator fun invoke(): FinanceSettings = repository.getSettings()
}

class UpdateSettingsUseCase(private val repository: FinancesRepository) {
    suspend operator fun invoke(settings: FinanceSettings): FinanceSettings {
        if (settings.baseMonthlyExpense < 0) {
            throw AppException("SETTINGS_INVALID", "Monto base inválido.")
        }
        return repository.updateSettings(settings)
    }
}

class ResidentStatusesUseCase(private val repository: FinancesRepository) {
    suspend operator fun invoke(): List<ResidentFinancialStatus> {
        val residents = repository.listResidents()
        val receipts = repository.listReceipts()
        return ResidentFinancialStatusCalculator.calculate(residents, receipts)
    }
}

class GenerateMonthlyReceiptsUseCase(private val repository: FinancesRepository) {
    /** Issues the next cycle only for residents that are fully paid up. */
    suspend operator fun invoke(): Int {
        val settings = repository.getSettings()
        val statuses = ResidentStatusesUseCase(repository)()
        var created = 0
        statuses.filter { it.isPaid }.forEach { status ->
            repository.createReceipt(
                Receipt(
                    residentId = status.resident.id,
                    issueDate = Dates.todayYmd(),
                    dueDate = status.nextPaymentDate,
                    amount = settings.baseMonthlyExpense,
                    status = Receipt.STATUS_PENDING,
                    lateFee = 0.0,
                ),
            )
            created++
        }
        return created
    }
}

class GenerateInitialReceiptUseCase(private val repository: FinancesRepository) {
    suspend operator fun invoke(residentId: String, admissionDate: String?): Receipt? {
        val alreadyPending = repository.listReceipts().any {
            it.residentId == residentId && it.status != Receipt.STATUS_PAID
        }
        if (alreadyPending) {
            throw AppException(
                "RECEIPT_ALREADY_PENDING",
                "Este residente ya tiene un recibo pendiente. Regístralo primero o espera a que pague.",
            )
        }
        val settings = repository.getSettings()
        val base = Dates.ymdOrNull(admissionDate) ?: return null
        val dueDate = base.plusDays(30).toString()
        return repository.createReceipt(
            Receipt(
                residentId = residentId,
                issueDate = Dates.todayYmd(),
                dueDate = dueDate,
                amount = settings.baseMonthlyExpense,
                status = Receipt.STATUS_PENDING,
                lateFee = 0.0,
            ),
        )
    }
}

/**
 * Registers a payment: marks the receipt as paid and records a matching
 * [Payment] row. The payment row is what feeds the resident app's "total
 * paid" (it sums `payments`, not receipt status), so without it the admin
 * could settle a debt and the resident's paid total would never move.
 * It does NOT issue the next month's receipt; that only happens through the
 * explicit "Generar recibos del mes" action.
 */
class ReconcilePaymentUseCase(private val repository: FinancesRepository) {
    suspend operator fun invoke(receipt: Receipt) {
        val id = receipt.id ?: return
        repository.updateReceipt(id, ReceiptPatchData(status = Receipt.STATUS_PAID))
        // Settling is always for the full outstanding amount (base + mora).
        val total = receipt.amount + receipt.lateFee
        if (total > 0) {
            repository.addPayment(
                Payment(
                    residentId = receipt.residentId,
                    feeId = id,
                    feeMonth = Dates.monthYearLabel(receipt.dueDate),
                    concept = receipt.concept.ifBlank { "Mantenimiento" },
                    amount = total,
                    paidAt = Dates.nowIso(),
                    method = "Efectivo",
                ),
            )
        }
    }
}

/**
 * Applies the configured late fee automatically whenever a receipt is past
 * due (the web nightly cron, now run on every finance load). It also heals
 * legacy `Overdue` rows back to `Pending`: the backend checkout and
 * reconcile only accept `Pending` receipts, so overdue ones must stay
 * payable with the mora carried in `lateFee`. Receipts tagged as waived are
 * skipped so a forgiven mora is never re-charged.
 */
class AutoApplyLateFeesUseCase(private val repository: FinancesRepository) {
    suspend operator fun invoke(residentId: String? = null): Int {
        val settings = repository.getSettings()
        val receipts = if (residentId == null) {
            repository.listReceipts()
        } else {
            repository.listReceiptsByResident(residentId)
        }
        var updated = 0
        receipts.forEach { receipt ->
            if (receipt.status == Receipt.STATUS_PAID) return@forEach
            val waived = receipt.concept.contains(Receipt.MORA_WAIVED_MARKER)
            val needsHeal = receipt.status == Receipt.STATUS_OVERDUE
            val needsFee = Dates.isPastDue(receipt.dueDate) && !waived && receipt.lateFee == 0.0
            if (needsHeal || needsFee) {
                repository.updateReceipt(
                    receipt.id ?: return@forEach,
                    ReceiptPatchData(
                        status = if (needsHeal) Receipt.STATUS_PENDING else null,
                        lateFee = if (needsFee) receipt.amount * settings.lateFeeRate else null,
                    ),
                )
                updated++
            }
        }
        return updated
    }
}

/**
 * Waives every outstanding late fee for one resident: the mora is zeroed,
 * the receipt goes back to `Pending` (payable) and it is tagged so
 * [AutoApplyLateFeesUseCase] never charges the fee again.
 */
class WaiveLateFeesUseCase(private val repository: FinancesRepository) {
    suspend operator fun invoke(residentId: String): Int {
        val targets = repository.listReceiptsByResident(residentId).filter {
            it.status != Receipt.STATUS_PAID && it.lateFee > 0.0
        }
        targets.forEach { receipt ->
            repository.updateReceipt(
                receipt.id ?: return@forEach,
                ReceiptPatchData(
                    status = Receipt.STATUS_PENDING,
                    lateFee = 0.0,
                    concept = waivedConcept(receipt.concept),
                ),
            )
        }
        return targets.size
    }

    private fun waivedConcept(concept: String): String {
        val base = concept.ifBlank { "Mantenimiento" }
        return if (base.contains(Receipt.MORA_WAIVED_MARKER)) {
            base
        } else {
            "$base (${Receipt.MORA_WAIVED_MARKER})"
        }
    }
}

/**
 * Issues receipts whose due date already passed (issued 40 days ago, due
 * 10 days ago) for every resident without an open receipt, so the
 * "Apply late fees" simulation has overdue receipts to flag.
 */
class CreateOverdueReceiptsUseCase(private val repository: FinancesRepository) {
    suspend operator fun invoke(): Int {
        val settings = repository.getSettings()
        val statuses = ResidentStatusesUseCase(repository)()
        val issueDate = java.time.LocalDate.now().minusDays(40)
        val dueDate = issueDate.plusDays(30).toString()
        var created = 0
        statuses.filter { status ->
            status.receipts.none { it.status != Receipt.STATUS_PAID }
        }.forEach { status ->
            repository.createReceipt(
                Receipt(
                    residentId = status.resident.id,
                    issueDate = issueDate.toString(),
                    dueDate = dueDate,
                    amount = settings.baseMonthlyExpense,
                    status = Receipt.STATUS_PENDING,
                    lateFee = 0.0,
                ),
            )
            created++
        }
        return created
    }
}

class GetKpisUseCase(private val repository: FinancesRepository) {
    suspend operator fun invoke(): DashboardKpi = repository.getKpis()
}

class GetAllPaymentsUseCase(private val repository: FinancesRepository) {
    suspend operator fun invoke() = repository.listAllPayments()
}

class ListReceiptsUseCase(private val repository: FinancesRepository) {
    suspend operator fun invoke() = repository.listReceipts()
}
