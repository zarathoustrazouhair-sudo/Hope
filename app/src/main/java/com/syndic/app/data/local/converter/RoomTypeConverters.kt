package com.syndic.app.data.local.converter

import androidx.room.TypeConverter
import com.syndic.app.data.local.entity.IncidentPriority
import com.syndic.app.data.local.entity.IncidentStatus
import com.syndic.app.data.local.entity.TransactionType
import com.syndic.app.data.local.entity.UserRole
import java.util.Date

class RoomTypeConverters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromUserRole(value: String?): UserRole? {
        return value?.let { enumValueOf<UserRole>(it) }
    }

    @TypeConverter
    fun userRoleToString(role: UserRole?): String? {
        return role?.name
    }

    @TypeConverter
    fun fromIncidentStatus(value: String?): IncidentStatus? {
        return value?.let { enumValueOf<IncidentStatus>(it) }
    }

    @TypeConverter
    fun incidentStatusToString(status: IncidentStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun fromIncidentPriority(value: String?): IncidentPriority? {
        return value?.let { enumValueOf<IncidentPriority>(it) }
    }

    @TypeConverter
    fun incidentPriorityToString(priority: IncidentPriority?): String? {
        return priority?.name
    }

    @TypeConverter
    fun fromTransactionType(value: String?): TransactionType? {
        return value?.let { enumValueOf<TransactionType>(it) }
    }

    @TypeConverter
    fun transactionTypeToString(type: TransactionType?): String? {
        return type?.name
    }
}
