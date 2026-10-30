package com.example.buildingfexfrontend.finances.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.i18n.stringOf
import com.example.buildingfexfrontend.finances.application.FinanceUseCases
import com.example.buildingfexfrontend.finances.domain.ResidentFinancialStatus
import com.example.buildingfexfrontend.finances.domain.model.FinanceSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FinanceUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val settings: FinanceSettings? = null,
    val baseInput: String = "",
    val lateInput: String = "",
    val savingSettings: Boolean = false,
    val statuses: List<ResidentFinancialStatus> = emptyList(),
    val busy: Boolean = false,
    val message: String? = null,
)

class FinanceViewModel(private val useCases: FinanceUseCases) : ViewModel() {

    private val _state = MutableStateFlow(FinanceUiState())
    val state: StateFlow<FinanceUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                runCatching { useCases.autoApplyLateFees() }
                val settings = useCases.settings()
                val statuses = useCases.residentStatuses()
                _state.update {
                    it.copy(
                        loading = false,
                        settings = settings,
                        baseInput = settings.baseMonthlyExpense.toString(),
                        lateInput = settings.lateFeeRate.toString(),
                        statuses = statuses,
                    )
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(loading = false, error = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun onBaseChange(value: String) = _state.update { it.copy(baseInput = value) }

    fun onLateChange(value: String) = _state.update { it.copy(lateInput = value) }

    fun saveSettings() {
        val s = _state.value
        val base = s.baseInput.toDoubleOrNull()
        val late = s.lateInput.toDoubleOrNull()
        if (base == null || late == null) {
            _state.update { it.copy(message = stringOf("fin.settings.invalid")) }
            return
        }
        _state.update { it.copy(savingSettings = true, message = null) }
        viewModelScope.launch {
            try {
                val updated = useCases.updateSettings(FinanceSettings(base, late))
                _state.update {
                    it.copy(
                        savingSettings = false,
                        settings = updated,
                        message = stringOf("fin.settings.saved"),
                    )
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(savingSettings = false, message = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun generateMonthlyReceipts() = runAction("generar") {
        val created = useCases.generateMonthlyReceipts()
        stringOf("fin.msg.receiptsGenerated").replace("{n}", created.toString())
    }

    fun createOverdueReceipts() = runAction("vencidos") {
        val created = useCases.createOverdueReceipts()
        if (created == 0) {
            stringOf("fin.msg.overdueNone")
        } else {
            stringOf("fin.msg.overdueCreated").replace("{n}", created.toString())
        }
    }

    fun waiveLateFees(status: ResidentFinancialStatus) = runAction("perdonar") {
        val waived = useCases.waiveLateFees(status.resident.id)
        if (waived == 0) {
            stringOf("fin.msg.noLateFee")
        } else {
            stringOf("fin.msg.lateFeeWaived").replace("{n}", waived.toString())
        }
    }

    fun reconcile(residentStatus: ResidentFinancialStatus) {
        val target = residentStatus.receipts
            .filter { it.status != com.example.buildingfexfrontend.finances.domain.model.Receipt.STATUS_PAID }
            .minByOrNull { it.dueDate }
            ?: return
        runAction("pago") {
            useCases.reconcilePayment(target)
            stringOf("fin.msg.paymentRegistered")
        }
    }

    fun generateFor(residentStatus: ResidentFinancialStatus) {
        val resident = residentStatus.resident
        runAction("recibo") {
            useCases.generateInitialReceipt(resident.id, resident.admissionDate)
            stringOf("fin.msg.receiptFor").replace("{name}", resident.name)
        }
    }

    fun dismissMessage() = _state.update { it.copy(message = null) }

    private fun runAction(key: String, block: suspend () -> String) {
        _state.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            try {
                val message = block()
                val statuses = runCatching {
                    useCases.autoApplyLateFees()
                    useCases.residentStatuses()
                }.getOrDefault(_state.value.statuses)
                _state.update { it.copy(busy = false, statuses = statuses, message = message) }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(busy = false, message = AppException.unexpected(e).userMessage())
                }
            }
        }
    }
}
