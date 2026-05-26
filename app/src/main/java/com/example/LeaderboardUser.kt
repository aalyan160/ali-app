package com.example

data class LeaderboardUser(
    val id: String,
    val username: String,
    val initials: String,
    val weeklyMinutes: Int,
    val isCurrentUser: Boolean
)
