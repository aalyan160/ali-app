package com.example

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.util.UUID

@Entity(tableName = "tasks")
@Serializable
data class Task(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val priority: String, // "High", "Medium", "Low"
    @SerialName("is_completed") val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    @SerialName("user_id") val userId: String = ""
)
