package com.example

data class XpLeaderboardUser(
    val id: String,
    val username: String,
    val initials: String,
    val xp: Int,
    val isCurrentUser: Boolean
)
