package com.example

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.util.UUID

@Entity(tableName = "notes")
@Serializable
data class Note(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: String, // "Quick" or "Detailed"
    val title: String = "",
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    @SerialName("user_id") val userId: String = ""
)
