package com.example

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class UserProfile(
    val id: String = "",
    val full_name: String? = "",
    val streak: JsonElement? = null,
    val xp: JsonElement? = null,
    val study_commitment_duration: JsonElement? = null,
    val study_commitment_start_date: JsonElement? = null
) {
    constructor(
        id: String,
        full_name: String? = "",
        streak: Int? = 0,
        xp: Int? = 0,
        study_commitment_duration: Int? = null,
        study_commitment_start_date: Long? = null
    ) : this(
        id = id,
        full_name = full_name,
        streak = streak?.let { JsonPrimitive(it) },
        xp = xp?.let { JsonPrimitive(it) },
        study_commitment_duration = study_commitment_duration?.let { JsonPrimitive(it) },
        study_commitment_start_date = study_commitment_start_date?.let { JsonPrimitive(it) }
    )

    fun getStreak(): Int {
        val element = streak ?: return 0
        if (element is JsonPrimitive) {
            val content = element.content
            return content.toIntOrNull() ?: content.toDoubleOrNull()?.toInt() ?: 0
        }
        return 0
    }

    fun getXp(): Int {
        val element = xp ?: return 0
        if (element is JsonPrimitive) {
            val content = element.content
            return content.toIntOrNull() ?: content.toDoubleOrNull()?.toInt() ?: 0
        }
        return 0
    }

    fun getStudyCommitmentDuration(): Int? {
        val element = study_commitment_duration ?: return null
        if (element is JsonPrimitive) {
            val content = element.content
            return content.toIntOrNull() ?: content.toDoubleOrNull()?.toInt()
        }
        return null
    }

    fun getStudyCommitmentStartDate(): Long? {
        val element = study_commitment_start_date ?: return null
        if (element is JsonPrimitive) {
            val content = element.content
            if (element.isString) {
                val longVal = content.toLongOrNull()
                if (longVal != null) return longVal
                return try {
                    java.time.Instant.parse(content).toEpochMilli()
                } catch (e: Exception) {
                    null
                }
            } else {
                return content.toLongOrNull() ?: content.toDoubleOrNull()?.toLong()
            }
        }
        return null
    }
}
