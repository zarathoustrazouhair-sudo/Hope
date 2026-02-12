package com.syndic.app.data.repository

import com.syndic.app.data.local.dao.UserDao
import com.syndic.app.data.local.entity.UserEntity
import com.syndic.app.data.local.entity.UserRole
import com.syndic.app.domain.repository.UserRepository
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton
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

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val postgrest: Postgrest,
    private val auth: Auth
) : UserRepository {

    override fun getCurrentUser(): Flow<UserEntity?> {
        // For Offline-First MVP, we might rely on a session manager, but here we check Supabase auth
        val currentUser = auth.currentUserOrNull()
        if (currentUser == null) return flowOf(null)
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

            val entity = mapDtoToEntity(result)
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

            postgrest.from("profiles").upsert(dto)
            syncUser()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllUsers(): List<UserEntity> {
        return userDao.getAllUsersSync()
    }

    override suspend fun createUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    override suspend fun getUserByApartment(apartment: String): UserEntity? {
        return userDao.getUserByApartment(apartment)
    }

    private fun mapDtoToEntity(dto: UserDto): UserEntity {
        return UserEntity(
            id = dto.id,
            email = dto.email,
            firstName = dto.first_name,
            lastName = dto.last_name,
            role = try { UserRole.valueOf(dto.role) } catch (e: Exception) { UserRole.RESIDENT },
            building = dto.building,
            apartmentNumber = dto.apartment_number,
            phoneNumber = null,
            cin = null,
            mandateStartDate = null,
            pinHash = null, // Sync from Supabase doesn't include PIN hash usually (security)
            createdAt = dto.created_at?.let { Date.from(Instant.parse(it)) },
            updatedAt = dto.updated_at?.let { Date.from(Instant.parse(it)) }
        )
    }
}
