package com.syndic.app.ui.community.blog

import com.syndic.app.data.local.entity.BlogPostEntity

data class BlogUiState(
    val posts: List<BlogPostEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSyndic: Boolean = false // To show FAB
)
