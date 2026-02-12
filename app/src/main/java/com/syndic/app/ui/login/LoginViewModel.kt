package com.syndic.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syndic.app.data.local.entity.UserRole
import com.syndic.app.domain.repository.ConfigRepository
import com.syndic.app.domain.repository.UserRepository
import com.syndic.app.util.SecurityUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LoginMode {
    SYNDIC, RESIDENT
}

data class LoginState(
    val mode: LoginMode = LoginMode.RESIDENT,
    val syndicPin: String = "",
    val residentPin: String = "",
    val selectedApartment: String = "AP1",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val authenticatedRole: UserRole? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun switchMode(mode: LoginMode) {
        _state.value = _state.value.copy(mode = mode, error = null, syndicPin = "", residentPin = "")
    }

    fun onSyndicPinChange(pin: String) {
        if (pin.length <= 6 && pin.all { it.isDigit() }) {
             _state.value = _state.value.copy(syndicPin = pin, error = null)
        }
    }

    fun onResidentPinChange(pin: String) {
        if (pin.length <= 4 && pin.all { it.isDigit() }) {
             _state.value = _state.value.copy(residentPin = pin, error = null)
        }
    }

    fun onApartmentSelected(apartment: String) {
         _state.value = _state.value.copy(selectedApartment = apartment, error = null)
    }

    fun onLogin() {
        val s = _state.value
        _state.value = s.copy(isLoading = true, error = null)

        viewModelScope.launch {
            if (s.mode == LoginMode.SYNDIC) {
                val config = configRepository.getConfig().firstOrNull()
                // Use validatePin from SecurityUtils
                // If config exists and pin matches
                if (config != null && SecurityUtils.validatePin(s.syndicPin, config.masterPinHash)) {
                    _state.value = s.copy(isLoading = false, isAuthenticated = true, authenticatedRole = UserRole.SYNDIC)
                } else {
                    _state.value = s.copy(isLoading = false, error = "Code Maître Incorrect")
                }
            } else {
                // Resident Mode
                // Get User by Apartment
                val user = userRepository.getUserByApartment(s.selectedApartment)
                if (user != null && SecurityUtils.validatePin(s.residentPin, user.pinHash)) {
                    _state.value = s.copy(isLoading = false, isAuthenticated = true, authenticatedRole = UserRole.RESIDENT)
                } else {
                    _state.value = s.copy(isLoading = false, error = "PIN Résident Incorrect")
                }
            }
        }
    }
}
