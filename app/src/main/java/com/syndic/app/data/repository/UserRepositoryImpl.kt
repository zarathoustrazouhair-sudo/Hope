package com.syndic.app.data.repository

import com.syndic.app.data.local.dao.UserDao
import com.syndic.app.data.local.entity.UserEntity
import com.syndic.app.data.local.entity.UserRole
import com.syndic.app.domain.repository.UserRepository
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.Serializable
import javax.inject.Inject
import java.time.Instant
import java.util.Date

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val first_name: String,
    val last_name: String,
    val role: String,
    val building: String,
    val apartment_number: String,
    val created_at: String? = null,
    val updated_at: String? = null
)

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val postgrest: Postgrest,
    private val auth: Auth
) : UserRepository {

    override fun getCurrentUser(): Flow<UserEntity?> {
        val currentUser = auth.currentUserOrNull() ?: return flowOf(null)
        return userDao.getUser(currentUser.id)
    }

    override suspend fun syncUser(): Result<Unit> {
        val user = auth.currentUserOrNull() ?: return Result.failure(Exception("Not authenticated"))

        return try {
            val result = postgrest.from("profiles").select {
                filter {
                    eq("id", user.id)
                }
            }.decodeSingle<UserDto>()

            val entity = UserEntity(
                id = result.id,
                email = result.email,
                firstName = result.first_name,
                lastName = result.last_name,
                role = try { UserRole.valueOf(result.role) } catch (e: Exception) { UserRole.RESIDENT },
                building = result.building,
                apartmentNumber = result.apartment_number,
                phoneNumber = null,
                cin = null,
                mandateStartDate = null,
                createdAt = result.created_at?.let { Date.from(Instant.parse(it)) },
                updatedAt = result.updated_at?.let { Date.from(Instant.parse(it)) }
            )

            userDao.insertUser(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createUserProfile(firstName: String, lastName: String, building: String, apartment: String): Result<Unit> {
        val user = auth.currentUserOrNull() ?: return Result.failure(Exception("Not authenticated"))
        val email = user.email ?: return Result.failure(Exception("No email found"))

        return try {
            val dto = UserDto(
                id = user.id,
                email = email,
                first_name = firstName,
                last_name = lastName,
                role = UserRole.RESIDENT.name,
                building = building,
                apartment_number = apartment
            )

            // Using upsert to avoid conflict with the SQL Trigger which might have already created the profile
            postgrest.from("profiles").upsert(dto)

            // Sync immediately to local DB
            syncUser()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
