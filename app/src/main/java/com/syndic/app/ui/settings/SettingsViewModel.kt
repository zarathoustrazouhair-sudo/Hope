package com.syndic.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syndic.app.data.local.entity.ResidenceConfigEntity
import com.syndic.app.domain.repository.ConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val isLoading: Boolean = true,
    val residenceName: String = "",
    val syndicPhone: String = "",
    val syndicEmail: String = "",
    val conciergeSalary: String = "",
    val cleaningCost: String = "",
    val electricityCost: String = "",
    val waterCost: String = "",
    val elevatorCost: String = "",
    val insuranceCost: String = "",
    val diversCost: String = "",
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val configRepository: ConfigRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private var currentConfig: ResidenceConfigEntity? = null

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val config = configRepository.getConfig().firstOrNull()
            if (config != null) {
                currentConfig = config
                _state.value = SettingsState(
                    isLoading = false,
                    residenceName = config.residenceName,
                    syndicPhone = config.syndicPhone,
                    syndicEmail = config.syndicEmail,
                    conciergeSalary = config.conciergeSalary.toString(),
                    cleaningCost = config.cleaningCost.toString(),
                    electricityCost = config.electricityCost.toString(),
                    waterCost = config.waterCost.toString(),
                    elevatorCost = config.elevatorCost.toString(),
                    insuranceCost = config.insuranceCost.toString(),
                    diversCost = config.diversCost.toString()
                )
            } else {
                _state.value = _state.value.copy(isLoading = false, error = "Configuration non trouvée")
            }
        }
    }

    fun onResidenceNameChange(v: String) { _state.value = _state.value.copy(residenceName = v) }
    fun onPhoneChange(v: String) { _state.value = _state.value.copy(syndicPhone = v) }
    fun onEmailChange(v: String) { _state.value = _state.value.copy(syndicEmail = v) }
    fun onConciergeChange(v: String) { _state.value = _state.value.copy(conciergeSalary = v) }
    fun onCleaningChange(v: String) { _state.value = _state.value.copy(cleaningCost = v) }
    fun onElectricityChange(v: String) { _state.value = _state.value.copy(electricityCost = v) }
    fun onWaterChange(v: String) { _state.value = _state.value.copy(waterCost = v) }
    fun onElevatorChange(v: String) { _state.value = _state.value.copy(elevatorCost = v) }
    fun onInsuranceChange(v: String) { _state.value = _state.value.copy(insuranceCost = v) }
    fun onDiversChange(v: String) { _state.value = _state.value.copy(diversCost = v) }

    fun saveSettings() {
        viewModelScope.launch {
            val s = _state.value
            val config = currentConfig ?: return@launch

            val updatedConfig = config.copy(
                residenceName = s.residenceName,
                syndicPhone = s.syndicPhone,
                syndicEmail = s.syndicEmail,
                conciergeSalary = s.conciergeSalary.toDoubleOrNull() ?: 0.0,
                cleaningCost = s.cleaningCost.toDoubleOrNull() ?: 0.0,
                electricityCost = s.electricityCost.toDoubleOrNull() ?: 0.0,
                waterCost = s.waterCost.toDoubleOrNull() ?: 0.0,
                elevatorCost = s.elevatorCost.toDoubleOrNull() ?: 0.0,
                insuranceCost = s.insuranceCost.toDoubleOrNull() ?: 0.0,
                diversCost = s.diversCost.toDoubleOrNull() ?: 0.0,
                updatedAt = System.currentTimeMillis()
            )

            configRepository.saveConfig(updatedConfig)
            // Show toast or navigate back logic here (handled by UI via callback usually)
        }
    }
}
