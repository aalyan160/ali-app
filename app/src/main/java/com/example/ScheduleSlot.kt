package com.example

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.util.UUID

@Entity(tableName = "schedule_slots")
@Serializable
data class ScheduleSlot(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val startTime: String, // format: "HH:mm"
    val endTime: String,   // format: "HH:mm"
    val taskName: String,
    val colorHex: String,  // hex string, e.g. "#00FFCC"
    val timestamp: Long = System.currentTimeMillis(),
    @SerialName("user_id") val userId: String = ""
)
