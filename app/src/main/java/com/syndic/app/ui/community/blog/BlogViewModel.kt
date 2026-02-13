package com.syndic.app.ui.community.blog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syndic.app.data.local.entity.UserRole
import com.syndic.app.domain.repository.CommunityRepository
import com.syndic.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlogViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BlogUiState())
    val uiState: StateFlow<BlogUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
        checkRole()
    }

    private fun checkRole() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser().firstOrNull()
            val isSyndic = user?.role == UserRole.SYNDIC || user?.role == UserRole.ADJOINT
            _uiState.update { it.copy(isSyndic = isSyndic) }
        }
    }

    private fun loadPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            communityRepository.getAllPosts().collectLatest { posts ->
                _uiState.update { it.copy(posts = posts, isLoading = false) }
            }
        }
    }

    fun createPost(title: String, content: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val user = userRepository.getCurrentUser().firstOrNull()

            if (user == null) {
                _uiState.update { it.copy(isLoading = false, error = "Utilisateur non trouvé") }
                return@launch
            }

            // Verify role again strictly before action
            if (user.role != UserRole.SYNDIC && user.role != UserRole.ADJOINT) {
                _uiState.update { it.copy(isLoading = false, error = "Non autorisé") }
                return@launch
            }

            val result = communityRepository.createPost(
                title = title,
                content = content,
                authorId = user.id, // Using ID, UI can resolve name if needed or we store name
                category = "Annonce" // Default for now
            )

            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false) }
                // List updates automatically via Flow
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }
}
