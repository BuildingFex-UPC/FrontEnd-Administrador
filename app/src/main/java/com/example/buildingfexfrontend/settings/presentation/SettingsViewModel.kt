package com.example.buildingfexfrontend.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.model.UserProfile
import com.example.buildingfexfrontend.core.i18n.stringOf
import com.example.buildingfexfrontend.iam.application.AuthUseCases
import com.example.buildingfexfrontend.subscription.application.SubscriptionUseCases
import com.example.buildingfexfrontend.subscription.domain.model.Subscription
import com.example.buildingfexfrontend.subscription.domain.model.SubscriptionPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val loading: Boolean = true,
    val profile: UserProfile? = null,
    val isAdmin: Boolean = false,
    val subscription: Subscription? = null,
    val planLoading: Boolean = false,
    val busyPlanId: String? = null,
    val pendingPlanId: String? = null,
    val openCheckoutUrl: String? = null,
    val message: String? = null,
    val error: String? = null,
    val loadError: String? = null,
    // Profile editing.
    val showEditProfile: Boolean = false,
    val editName: String = "",
    val editEmail: String = "",
    val editDni: String = "",
    val editAddress: String = "",
    val editCompany: String = "",
    val editRuc: String = "",
    val editLoading: Boolean = false,
    val editError: String? = null,
    // Plan change confirmation (id of the plan awaiting confirmation).
    val confirmPlanId: String? = null,
)

/** Profile + subscription plan management (port of AppSettingsView). */
class SettingsViewModel(
    private val auth: AuthUseCases,
    private val subscription: SubscriptionUseCases,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, loadError = null) }
        viewModelScope.launch {
            try {
                val profile = auth.refreshProfile()
                val isAdmin = profile?.isAdmin == true
                var sub: Subscription? = null
                if (isAdmin) {
                    sub = runCatching { subscription.current() }.getOrNull()
                }
                _state.update {
                    it.copy(loading = false, profile = profile, isAdmin = isAdmin, subscription = sub)
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(loading = false, loadError = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun requestPlanChange(planId: String) {
        val s = _state.value
        if (!s.isAdmin || s.busyPlanId != null || s.planLoading) return
        if (planId == SubscriptionPlan.FREE.id && s.subscription?.planId == planId) return
        // Changing the plan has billing consequences, so it always goes
        // through an explicit confirmation first.
        _state.update { it.copy(confirmPlanId = planId, message = null, error = null) }
    }

    fun cancelPlanChange() = _state.update { it.copy(confirmPlanId = null) }

    fun confirmPlanChange() {
        val planId = _state.value.confirmPlanId ?: return
        _state.update { it.copy(confirmPlanId = null) }
        selectPlan(planId)
    }

    fun selectPlan(planId: String) {
        val s = _state.value
        if (!s.isAdmin || s.busyPlanId != null || s.planLoading) return
        if (planId == SubscriptionPlan.FREE.id && s.subscription?.planId == planId) return

        _state.update { it.copy(busyPlanId = planId, message = null, error = null) }
        viewModelScope.launch {
            try {
                if (planId == SubscriptionPlan.FREE.id) {
                    val updated = subscription.changePlan(planId)
                    _state.update {
                        it.copy(
                            busyPlanId = null,
                            subscription = updated,
                            message = stringOf("set.msg.planFree"),
                        )
                    }
                } else {
                    val checkout = subscription.checkout(planId)
                    when {
                        checkout.demo -> {
                            subscription.confirm(planId = planId, demo = true)
                            val updated = runCatching { subscription.current() }.getOrNull()
                            _state.update {
                                it.copy(
                                    busyPlanId = null,
                                    subscription = updated ?: it.subscription,
                                    message = stringOf("set.msg.planDemo"),
                                )
                            }
                        }

                        !checkout.initPoint.isNullOrBlank() -> _state.update {
                            it.copy(
                                busyPlanId = null,
                                pendingPlanId = planId,
                                openCheckoutUrl = checkout.initPoint,
                                message = stringOf("set.msg.checkoutOpened"),
                            )
                        }

                        else -> _state.update {
                            it.copy(busyPlanId = null, error = stringOf("set.error.checkoutStart"))
                        }
                    }
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(busyPlanId = null, error = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    /**
     * Called when Mercado Pago redirects back into the in-app checkout WebView.
     * Extracts `payment_id` from the back_url query and confirms the plan change.
     */
    fun onCheckoutRedirect(uri: android.net.Uri) {
        _state.update { it.copy(openCheckoutUrl = null) }
        val flow = uri.getQueryParameter("subscription")
        val status = uri.getQueryParameter("status")
        val paymentId = (uri.getQueryParameter("payment_id")
            ?: uri.getQueryParameter("collection_id"))?.toLongOrNull()
        when {
            flow == "failure" -> _state.update {
                it.copy(error = stringOf("set.msg.paymentFailed"))
            }

            flow == "pending" || status == "pending" || status == "in_process" -> _state.update {
                it.copy(message = stringOf("set.msg.paymentPending"))
            }

            paymentId != null -> confirmPendingCheckoutWith(paymentId)

            else -> refreshSubscription()
        }
    }

    private fun refreshSubscription() {
        val planId = _state.value.pendingPlanId ?: return
        _state.update { it.copy(busyPlanId = planId, error = null) }
        viewModelScope.launch {
            try {
                val updated = runCatching { subscription.current() }.getOrNull()
                val activated = updated?.planId == planId
                _state.update {
                    it.copy(
                        busyPlanId = null,
                        pendingPlanId = if (activated) null else planId,
                        subscription = updated ?: it.subscription,
                        message = if (activated) {
                            stringOf("set.msg.planActivated")
                        } else {
                            stringOf("set.msg.paymentPending")
                        },
                    )
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(busyPlanId = null, error = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    private fun confirmPendingCheckoutWith(paymentId: Long) {
        val planId = _state.value.pendingPlanId ?: return
        _state.update { it.copy(busyPlanId = planId, error = null) }
        viewModelScope.launch {
            try {
                val result = subscription.confirm(planId = planId, paymentId = paymentId.toString(), demo = false)
                val updated = result.subscription ?: runCatching { subscription.current() }.getOrNull()
                _state.update {
                    it.copy(
                        busyPlanId = null,
                        pendingPlanId = null,
                        subscription = updated ?: it.subscription,
                        message = if (result.activated) stringOf("set.msg.planActivated")
                        else stringOf("set.msg.paymentPending"),
                    )
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(busyPlanId = null, error = AppException.unexpected(e).userMessage())
                }
            }
        }
    }

    fun consumeCheckoutUrl() = _state.update { it.copy(openCheckoutUrl = null) }

    // --- Profile editing ---

    fun openEditProfile() {
        val p = _state.value.profile ?: return
        _state.update {
            it.copy(
                showEditProfile = true,
                editName = p.name,
                editEmail = p.email.orEmpty(),
                editDni = p.dni.orEmpty(),
                editAddress = p.address.orEmpty(),
                editCompany = p.company.orEmpty(),
                editRuc = p.ruc.orEmpty(),
                editError = null,
            )
        }
    }

    fun closeEditProfile() =
        _state.update { it.copy(showEditProfile = false, editError = null) }

    fun onEditNameChange(v: String) =
        _state.update { it.copy(editName = v, editError = null) }

    fun onEditEmailChange(v: String) =
        _state.update { it.copy(editEmail = v, editError = null) }

    fun onEditDniChange(v: String) =
        _state.update { it.copy(editDni = v, editError = null) }

    fun onEditAddressChange(v: String) =
        _state.update { it.copy(editAddress = v, editError = null) }

    fun onEditCompanyChange(v: String) =
        _state.update { it.copy(editCompany = v, editError = null) }

    fun onEditRucChange(v: String) =
        _state.update { it.copy(editRuc = v, editError = null) }

    fun saveProfile() {
        val s = _state.value
        val current = s.profile ?: return
        if (s.editName.isBlank()) {
            _state.update { it.copy(editError = stringOf("set.edit.nameRequired")) }
            return
        }
        _state.update { it.copy(editLoading = true, editError = null) }
        viewModelScope.launch {
            try {
                val updated = auth.updateProfile(
                    current.copy(
                        name = s.editName.trim(),
                        email = s.editEmail.trim().lowercase().ifBlank { null },
                        dni = s.editDni.trim().ifBlank { null },
                        address = s.editAddress.trim().ifBlank { null },
                        company = s.editCompany.trim().ifBlank { null },
                        ruc = s.editRuc.trim().ifBlank { null },
                    ),
                )
                _state.update {
                    it.copy(
                        editLoading = false,
                        showEditProfile = false,
                        profile = updated,
                        message = stringOf("set.msg.profileSaved"),
                    )
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(
                        editLoading = false,
                        editError = AppException.unexpected(e).userMessage(),
                    )
                }
            }
        }
    }

    fun logout() = auth.logout()

    fun dismissMessage() = _state.update { it.copy(message = null, error = null) }
}

