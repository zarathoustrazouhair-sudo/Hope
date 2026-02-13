package com.syndic.app.ui.finance.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syndic.app.data.local.entity.ProviderEntity
import com.syndic.app.domain.repository.ProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProviderViewModel @Inject constructor(
    private val providerRepository: ProviderRepository
) : ViewModel() {
    private val _providers = MutableStateFlow<List<ProviderEntity>>(emptyList())
    val providers: StateFlow<List<ProviderEntity>> = _providers.asStateFlow()

    init {
        viewModelScope.launch {
            providerRepository.getAllProviders().collectLatest {
                _providers.value = it
            }
        }
    }

    fun createProvider(name: String, phone: String, category: String, cin: String?) {
        viewModelScope.launch {
            providerRepository.createProvider(name, phone, category, cin)
        }
    }
}
