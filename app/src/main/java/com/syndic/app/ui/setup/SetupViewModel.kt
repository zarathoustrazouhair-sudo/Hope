package com.syndic.app.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syndic.app.data.local.entity.ProviderEntity
import com.syndic.app.data.local.entity.ResidenceConfigEntity
import com.syndic.app.data.local.entity.UserEntity
import com.syndic.app.data.local.entity.UserRole
import com.syndic.app.data.local.entity.TransactionType
import com.syndic.app.data.local.entity.PaymentMethod
import com.syndic.app.domain.repository.CommunityRepository
import com.syndic.app.domain.repository.ConfigRepository
import com.syndic.app.domain.repository.ProviderRepository
import com.syndic.app.domain.repository.TaskRepository
import com.syndic.app.domain.repository.TransactionRepository
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
    private val userRepository: UserRepository,
    private val providerRepository: ProviderRepository,
    private val transactionRepository: TransactionRepository,
    private val communityRepository: CommunityRepository,
    private val taskRepository: TaskRepository
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

    fun onCivilityChange(civility: String) {
        _state.value = _state.value.copy(syndicCivility = civility)
    }

    fun onEmailChange(email: String) {
        _state.value = _state.value.copy(syndicEmail = email, error = null)
    }

    fun onPhoneChange(phone: String) {
        // Enforce numeric only for the body
        if (phone.all { it.isDigit() }) {
            _state.value = _state.value.copy(syndicPhone = phone, error = null)
        }
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

    fun onMonthlyFeeChange(value: String) { _state.value = _state.value.copy(monthlyFee = value, error = null) }
    fun onConciergeSalaryChange(value: String) { _state.value = _state.value.copy(conciergeSalary = value, error = null) }
    fun onCleaningCostChange(value: String) { _state.value = _state.value.copy(cleaningCost = value, error = null) }
    fun onElectricityCostChange(value: String) { _state.value = _state.value.copy(electricityCost = value, error = null) }
    fun onWaterCostChange(value: String) { _state.value = _state.value.copy(waterCost = value, error = null) }
    fun onElevatorCostChange(value: String) { _state.value = _state.value.copy(elevatorCost = value, error = null) }
    fun onInsuranceCostChange(value: String) { _state.value = _state.value.copy(insuranceCost = value, error = null) }
    fun onDiversCostChange(value: String) { _state.value = _state.value.copy(diversCost = value, error = null) }

    fun onNextStep() {
        val currentState = _state.value
        when (currentState.currentStep) {
            SetupStep.WELCOME -> {
                if (currentState.residenceName.isBlank()) {
                    _state.value = currentState.copy(error = "Le nom de la résidence est obligatoire")
                } else {
                    _state.value = currentState.copy(currentStep = SetupStep.SYNDIC_INFO, error = null)
                }
            }
            SetupStep.SYNDIC_INFO -> {
                if (currentState.syndicEmail.isBlank()) {
                    _state.value = currentState.copy(error = "Email obligatoire")
                } else if (currentState.syndicPhone.length < 9) { // Simple check for now
                    _state.value = currentState.copy(error = "Numéro de téléphone invalide")
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
            SetupStep.SYNDIC_INFO -> _state.value = currentState.copy(currentStep = SetupStep.WELCOME, error = null)
            SetupStep.MASTER_PIN -> _state.value = currentState.copy(currentStep = SetupStep.SYNDIC_INFO, error = null)
            SetupStep.FINANCIAL_CONFIG -> _state.value = currentState.copy(currentStep = SetupStep.MASTER_PIN, error = null)
            SetupStep.SECURITY_CHECK -> _state.value = currentState.copy(currentStep = SetupStep.FINANCIAL_CONFIG, error = null)
            else -> {}
        }
    }

    private fun isValidFinancials(): Boolean {
        val s = _state.value
        return try {
            s.monthlyFee.ifBlank { "0" }.toDouble()
            // ... strict check if needed, mostly parseable is enough
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
                syndicCivility = s.syndicCivility,
                syndicEmail = s.syndicEmail,
                syndicPhone = "+212${s.syndicPhone}", // Store with prefix
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
            seedDemoData(s.residenceName, s.monthlyFee.toDoubleOrNull() ?: 200.0)

            // 3. Complete
            _state.value = s.copy(isLoading = false, currentStep = SetupStep.COMPLETE)
        }
    }

    private suspend fun seedDemoData(buildingName: String, monthlyFee: Double) {
        val defaultPinHash = SecurityUtils.hashPin("0000")

        // 1. Create Residents (AP1 - AP15)
        for (i in 1..15) {
            val apartment = "AP$i"
            val userId = UUID.randomUUID().toString()
            val user = UserEntity(
                id = userId,
                email = "ap$i@residence.com",
                firstName = "Résident",
                lastName = apartment,
                role = UserRole.RESIDENT,
                building = buildingName,
                apartmentNumber = apartment,
                pinHash = defaultPinHash,
                phoneNumber = null,
                cin = null,
                mandateStartDate = null,
                createdAt = Date(),
                updatedAt = Date()
            )
            userRepository.createUser(user)

            // Create Mock Balance via "Reprise de Solde"
            val initialBalance = when {
                i <= 5 -> monthlyFee * 3 // Gold (Advance)
                i <= 10 -> 0.0 // Green
                else -> -monthlyFee * 2 // Red (Debt)
            }

            if (initialBalance > 0) {
                // Credit (Advance) -> Fake Payment
                transactionRepository.createTransaction(
                    userId = userId,
                    amount = initialBalance,
                    type = TransactionType.PAIEMENT,
                    label = "Reprise Solde (Avance)",
                    paymentMethod = PaymentMethod.VIREMENT
                )
            } else if (initialBalance < 0) {
                // Debt -> Fake Expense (Simulated Debit) or we create a negative "Reprise" if system allows,
                // but usually "Debit" means expense. Here we create a "Sortie" type transaction labeled "Reprise Solde (Dette)"
                // Note: The system logic usually sums (Payments - Expenses).
                // To create a debt for a user, we create an Expense linked to them (if supported) or just create a negative payment.
                // Or create a standard transaction. Let's assume creating a "DEPENSE" type linked to the user works as a debit.
                // However, `createTransaction` for DEPENSE usually requires provider/category.
                // Let's create a DEPENSE with category "Legacy Debt".
                transactionRepository.createTransaction(
                    userId = userId,
                    amount = kotlin.math.abs(initialBalance),
                    type = TransactionType.DEPENSE,
                    label = "Reprise Solde (Dette)",
                    provider = "Ancien Syndic",
                    category = "Dette Antérieure"
                )
            }
        }

        // 2. Create Providers
        val providers = listOf(
            ProviderEntity(id = UUID.randomUUID().toString(), name = "LYDEC (Eau/Elec)", phone = "0522000000", cin = null, category = "Services Publics"),
            ProviderEntity(id = UUID.randomUUID().toString(), name = "AXA Assurance", phone = "0522111111", cin = null, category = "Assurance"),
            ProviderEntity(id = UUID.randomUUID().toString(), name = "OTIS Ascenseurs", phone = "0522222222", cin = null, category = "Maintenance"),
            ProviderEntity(id = UUID.randomUUID().toString(), name = "Jardinier Express", phone = "0600000000", cin = null, category = "Jardinage"),
            ProviderEntity(id = UUID.randomUUID().toString(), name = "Société Nettoyage", phone = "0611111111", cin = null, category = "Ménage")
        )
        providers.forEach {
            providerRepository.createProvider(it.name, it.phone, it.category, it.cin)
        }

        // 3. Create Expenses (to populate Cash Flow)
        val demoProvider = providers.first()
        transactionRepository.createTransaction(
            userId = null,
            amount = 500.0,
            type = TransactionType.DEPENSE,
            label = "Facture Eau Potable",
            provider = demoProvider.name,
            category = "Eau"
        )
        transactionRepository.createTransaction(
            userId = null,
            amount = 1200.0,
            type = TransactionType.DEPENSE,
            label = "Nettoyage Mensuel",
            provider = providers.last().name,
            category = "Ménage"
        )

        // 4. Create Blog Posts
        communityRepository.createPost(
            "Bienvenue sur Syndic La Mondiale",
            "Nous sommes heureux de vous accueillir sur votre nouvelle application de gestion de résidence. Transparence et efficacité sont nos maîtres mots.",
            "SYNDIC",
            "OFFICIAL"
        )
        communityRepository.createPost(
            "Rappel : Tri des déchets",
            "Merci de bien vouloir respecter les consignes de tri sélectif. Les poubelles jaunes sont pour le plastique et le carton.",
            "SYNDIC",
            "INFO"
        )

        // 5. Create Tasks
        taskRepository.createTask("Vérifier étanchéité Toiture", Date(), false)
        taskRepository.createTask("Payer le Gardien", Date(), true)
    }
}
