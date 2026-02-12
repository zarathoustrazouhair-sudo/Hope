package com.syndic.app.domain.repository

import com.syndic.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<UserEntity?>
    suspend fun syncUser(): Result<Unit>
    suspend fun createUserProfile(firstName: String, lastName: String, building: String, apartment: String): Result<Unit>
    suspend fun getAllUsers(): List<UserEntity>
    suspend fun createUser(user: UserEntity) // For offline seeding
    suspend fun getUserByApartment(apartment: String): UserEntity?
}
