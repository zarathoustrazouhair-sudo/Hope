package com.syndic.app.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syndic.app.data.local.entity.ResidenceConfigEntity
import com.syndic.app.data.local.entity.UserEntity
import com.syndic.app.data.local.entity.UserRole
import com.syndic.app.domain.repository.ConfigRepository
import com.syndic.app.domain.repository.UserRepository
import com.syndic.app.util.SecurityUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SetupState())
    val state: StateFlow<SetupState> = _state.asStateFlow()

    init {
        checkIfAlreadySetup()
    }

    private fun checkIfAlreadySetup() {
        viewModelScope.launch {
            if (configRepository.isSetupComplete()) {
                _state.value = _state.value.copy(currentStep = SetupStep.COMPLETE)
            }
        }
    }

    fun onResidenceNameChange(name: String) {
        _state.value = _state.value.copy(residenceName = name, error = null)
    }

    fun onMasterPinChange(pin: String) {
        if (pin.length <= 6 && pin.all { it.isDigit() }) {
            _state.value = _state.value.copy(masterPin = pin, error = null)
        }
    }

    fun onMasterPinConfirmChange(pin: String) {
        if (pin.length <= 6 && pin.all { it.isDigit() }) {
            _state.value = _state.value.copy(masterPinConfirm = pin, error = null)
        }
    }

    fun onMonthlyFeeChange(value: String) {
         _state.value = _state.value.copy(monthlyFee = value, error = null)
    }

    fun onConciergeSalaryChange(value: String) {
         _state.value = _state.value.copy(conciergeSalary = value, error = null)
    }

    fun onCleaningCostChange(value: String) {
         _state.value = _state.value.copy(cleaningCost = value, error = null)
    }

    fun onElectricityCostChange(value: String) {
         _state.value = _state.value.copy(electricityCost = value, error = null)
    }

    fun onWaterCostChange(value: String) {
         _state.value = _state.value.copy(waterCost = value, error = null)
    }

    fun onElevatorCostChange(value: String) {
         _state.value = _state.value.copy(elevatorCost = value, error = null)
    }

    fun onInsuranceCostChange(value: String) {
         _state.value = _state.value.copy(insuranceCost = value, error = null)
    }

    fun onDiversCostChange(value: String) {
         _state.value = _state.value.copy(diversCost = value, error = null)
    }

    fun onNextStep() {
        val currentState = _state.value
        when (currentState.currentStep) {
            SetupStep.WELCOME -> {
                if (currentState.residenceName.isBlank()) {
                    _state.value = currentState.copy(error = "Le nom de la résidence est obligatoire")
                } else {
                    _state.value = currentState.copy(currentStep = SetupStep.MASTER_PIN, error = null)
                }
            }
            SetupStep.MASTER_PIN -> {
                if (currentState.masterPin.length < 4) {
                    _state.value = currentState.copy(error = "Le PIN doit contenir au moins 4 chiffres")
                } else if (currentState.masterPin != currentState.masterPinConfirm) {
                    _state.value = currentState.copy(error = "Les codes PIN ne correspondent pas")
                } else {
                    _state.value = currentState.copy(currentStep = SetupStep.FINANCIAL_CONFIG, error = null)
                }
            }
            SetupStep.FINANCIAL_CONFIG -> {
                if (isValidFinancials()) {
                    _state.value = currentState.copy(currentStep = SetupStep.SECURITY_CHECK, error = null)
                }
            }
            SetupStep.SECURITY_CHECK -> {
                saveConfigAndSeed()
            }
            SetupStep.COMPLETE -> { /* No-op */ }
        }
    }

    fun onBackStep() {
         val currentState = _state.value
        when (currentState.currentStep) {
            SetupStep.MASTER_PIN -> _state.value = currentState.copy(currentStep = SetupStep.WELCOME, error = null)
            SetupStep.FINANCIAL_CONFIG -> _state.value = currentState.copy(currentStep = SetupStep.MASTER_PIN, error = null)
            SetupStep.SECURITY_CHECK -> _state.value = currentState.copy(currentStep = SetupStep.FINANCIAL_CONFIG, error = null)
            else -> {}
        }
    }

    private fun isValidFinancials(): Boolean {
        val s = _state.value
        return try {
            s.monthlyFee.ifBlank { "0" }.toDouble()
            s.conciergeSalary.ifBlank { "0" }.toDouble()
            s.cleaningCost.ifBlank { "0" }.toDouble()
            s.electricityCost.ifBlank { "0" }.toDouble()
            s.waterCost.ifBlank { "0" }.toDouble()
            s.elevatorCost.ifBlank { "0" }.toDouble()
            s.insuranceCost.ifBlank { "0" }.toDouble()
            s.diversCost.ifBlank { "0" }.toDouble()
            true
        } catch (e: NumberFormatException) {
            _state.value = s.copy(error = "Veuillez entrer des montants valides")
            false
        }
    }

    private fun saveConfigAndSeed() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val s = _state.value

            // 1. Save Residence Configuration
            val config = ResidenceConfigEntity(
                id = "config_v1",
                residenceName = s.residenceName,
                monthlyFee = s.monthlyFee.toDoubleOrNull() ?: 0.0,
                conciergeSalary = s.conciergeSalary.toDoubleOrNull() ?: 0.0,
                cleaningCost = s.cleaningCost.toDoubleOrNull() ?: 0.0,
                electricityCost = s.electricityCost.toDoubleOrNull() ?: 0.0,
                waterCost = s.waterCost.toDoubleOrNull() ?: 0.0,
                elevatorCost = s.elevatorCost.toDoubleOrNull() ?: 0.0,
                insuranceCost = s.insuranceCost.toDoubleOrNull() ?: 0.0,
                diversCost = s.diversCost.toDoubleOrNull() ?: 0.0,
                masterPinHash = SecurityUtils.hashPin(s.masterPin),
                isSetupComplete = true
            )

            configRepository.saveConfig(config)

            // 2. Seed Resident Users (AP1 to AP15)
            val defaultPinHash = SecurityUtils.hashPin("0000")
            for (i in 1..15) {
                val user = UserEntity(
                    id = UUID.randomUUID().toString(),
                    email = "ap$i@residence.com", // Placeholder email
                    firstName = "Résident",
                    lastName = "AP$i",
                    role = UserRole.RESIDENT,
                    building = s.residenceName,
                    apartmentNumber = "AP$i",
                    pinHash = defaultPinHash,
                    phoneNumber = null,
                    cin = null,
                    mandateStartDate = null,
                    createdAt = Date(),
                    updatedAt = Date()
                )
                userRepository.createUser(user)
            }

            // 3. Complete
            _state.value = s.copy(isLoading = false, currentStep = SetupStep.COMPLETE)
        }
    }
}
