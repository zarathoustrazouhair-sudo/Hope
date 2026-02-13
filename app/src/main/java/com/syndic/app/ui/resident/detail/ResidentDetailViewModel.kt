package com.syndic.app.ui.resident.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syndic.app.data.local.entity.UserEntity
import com.syndic.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResidentDetailState(
    val user: UserEntity? = null,
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ResidentDetailViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ResidentDetailState())
    val state: StateFlow<ResidentDetailState> = _state.asStateFlow()

    fun loadResident(apartment: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val user = userRepository.getUserByApartment(apartment)
            _state.value = _state.value.copy(user = user, isLoading = false)
        }
    }

    fun updateResident(firstName: String, lastName: String, phone: String, email: String) {
        viewModelScope.launch {
            val currentUser = _state.value.user ?: return@launch
            val updatedUser = currentUser.copy(
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phone,
                email = email
            )
            // Assuming UserRepository has update method, otherwise we use create/insert logic (Room upsert)
            userRepository.createUser(updatedUser)
            _state.value = _state.value.copy(user = updatedUser, isEditing = false)
        }
    }

    fun toggleEdit() {
        _state.value = _state.value.copy(isEditing = !_state.value.isEditing)
    }
}
