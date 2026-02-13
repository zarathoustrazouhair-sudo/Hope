package com.syndic.app.ui.setup

enum class SetupStep {
    WELCOME,
    MASTER_PIN,
    FINANCIAL_CONFIG,
    SECURITY_CHECK,
    COMPLETE
}

data class SetupState(
    val currentStep: SetupStep = SetupStep.WELCOME,
    val isLoading: Boolean = false,
    val error: String? = null,

    // Config Data
    val residenceName: String = "",
    val masterPin: String = "",
    val masterPinConfirm: String = "",

    // Financial Inputs (Strings for easier editing)
    val monthlyFee: String = "",
    val conciergeSalary: String = "",
    val cleaningCost: String = "",
    val electricityCost: String = "",
    val waterCost: String = "",
    val elevatorCost: String = "",
    val insuranceCost: String = "",
    val diversCost: String = "" // Maintenance/Divers
)
