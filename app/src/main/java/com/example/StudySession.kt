package com.example

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.util.UUID

@Entity(tableName = "study_sessions")
@Serializable
data class StudySession(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    @SerialName("duration_minutes") val durationMinutes: Int,
    @SerialName("user_id") val userId: String = ""
)
