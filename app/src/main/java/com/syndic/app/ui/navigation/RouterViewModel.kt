package com.syndic.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syndic.app.domain.repository.ConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RouterState(
    val isLoading: Boolean = true,
    val isSetupComplete: Boolean = false
)

@HiltViewModel
class RouterViewModel @Inject constructor(
    private val configRepository: ConfigRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RouterState())
    val state: StateFlow<RouterState> = _state.asStateFlow()

    init {
        refreshSetupState()
    }

    fun refreshSetupState() {
        viewModelScope.launch {
            val isComplete = configRepository.isSetupComplete()
            _state.value = RouterState(isLoading = false, isSetupComplete = isComplete)
        }
    }
}
