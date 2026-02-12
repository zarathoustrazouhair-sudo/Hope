package com.syndic.app.ui.resident

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syndic.app.domain.repository.UserRepository
import com.syndic.app.util.SecurityUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangePinState(
    val oldPin: String = "",
    val newPin: String = "",
    val confirmPin: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class ChangePinViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChangePinState())
    val state: StateFlow<ChangePinState> = _state.asStateFlow()

    fun onOldPinChange(pin: String) {
        if (pin.length <= 4 && pin.all { it.isDigit() }) {
            _state.value = _state.value.copy(oldPin = pin, error = null)
        }
    }

    fun onNewPinChange(pin: String) {
        if (pin.length <= 4 && pin.all { it.isDigit() }) {
            _state.value = _state.value.copy(newPin = pin, error = null)
        }
    }

    fun onConfirmPinChange(pin: String) {
        if (pin.length <= 4 && pin.all { it.isDigit() }) {
            _state.value = _state.value.copy(confirmPin = pin, error = null)
        }
    }

    fun onChangePin() {
        viewModelScope.launch {
            val s = _state.value
            _state.value = s.copy(isLoading = true, error = null)

            // Validation
            if (s.newPin.length != 4) {
                _state.value = s.copy(isLoading = false, error = "Le nouveau code doit faire 4 chiffres")
                return@launch
            }
            if (s.newPin != s.confirmPin) {
                _state.value = s.copy(isLoading = false, error = "Les codes ne correspondent pas")
                return@launch
            }

            // Get Current User
            val currentUser = userRepository.getCurrentUser().firstOrNull()
            if (currentUser == null) {
                _state.value = s.copy(isLoading = false, error = "Utilisateur non trouvé")
                return@launch
            }

            // Verify Old PIN
            if (!SecurityUtils.validatePin(s.oldPin, currentUser.pinHash)) {
                _state.value = s.copy(isLoading = false, error = "Ancien code incorrect")
                return@launch
            }

            // Update PIN
            val newHash = SecurityUtils.hashPin(s.newPin)
            val updatedUser = currentUser.copy(pinHash = newHash)
            userRepository.createUser(updatedUser) // Re-saves user with new hash

            _state.value = s.copy(isLoading = false, isSuccess = true)
        }
    }
}
