package com.example

import android.app.Application
import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val syncMutex = Mutex()
    private val sharedPreferences = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _userName = MutableLiveData<String>(sharedPreferences.getString("user_name", "") ?: "")
    val userName: LiveData<String> get() = _userName
    
    private val _leaderboardList = MutableLiveData<List<LeaderboardUser>>(emptyList())
    val leaderboardList: LiveData<List<LeaderboardUser>> get() = _leaderboardList
    
    private val _xpLeaderboardList = MutableLiveData<List<XpLeaderboardUser>>(emptyList())
    val xpLeaderboardList: LiveData<List<XpLeaderboardUser>> get() = _xpLeaderboardList

    fun fetchXpLeaderboard() {
        viewModelScope.launch {
            try {
                val currentUserId = supabase.auth.currentUserOrNull()?.id ?: ""
                val currentName = _userName.value.takeIf { !it.isNullOrBlank() } ?: "Me"
                val currentInitials = currentName.take(2).uppercase()
                
                val profiles = try {
                    val rawObjects = supabase.from("users")
                        .select()
                        .decodeList<kotlinx.serialization.json.JsonObject>()
                        
                    rawObjects.map { obj ->
                        UserProfile(
                            id = obj["id"]?.jsonPrimitive?.contentOrNull ?: "",
                            full_name = obj["full_name"]?.jsonPrimitive?.contentOrNull,
                            streak = obj["streak"],
                            xp = obj["xp"],
                            study_commitment_duration = obj["study_commitment_duration"],
                            study_commitment_start_date = obj["study_commitment_start_date"]
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("Leaderboard", "Error decoding all users for XP", e)
                    emptyList()
                }
                
                var topUsers = profiles.map { profile ->
                    val isCurrent = profile.id == currentUserId
                    var rawName = profile.full_name?.takeIf { it.isNotBlank() } ?: "Anonymous"
                    if (rawName.startsWith("User ", ignoreCase = true) || rawName.matches(Regex("(?i)^user[a-f0-9]{4}\$"))) {
                        rawName = "Anonymous"
                    }
                    val name = if (isCurrent) currentName else rawName
                    val initials = if (isCurrent) currentInitials else name.take(2).uppercase()
                    val xpToUse = if (isCurrent) maxOf(_userXP.value ?: 0, profile.getXp()) else profile.getXp()
                    XpLeaderboardUser(profile.id, name, initials, xpToUse, isCurrent)
                }.toMutableList()

                if (currentUserId.isNotEmpty() && topUsers.none { it.id == currentUserId }) {
                    val currentXp = _userXP.value ?: 0
                    topUsers.add(XpLeaderboardUser(currentUserId, currentName, currentInitials, currentXp, true))
                }
                
                val finalTopUsers = topUsers.sortedByDescending { it.xp }.take(10)
                _xpLeaderboardList.postValue(finalTopUsers)
            } catch (e: Exception) {
                android.util.Log.e("Leaderboard", "Error fetching XP leaderboard", e)
                val msg = e.message ?: "Unknown error"
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(getApplication(), "XP Leaderboard failed: $msg", android.widget.Toast.LENGTH_LONG).show()
                }
                
                // Fallback to local user
                val currentUserId = supabase.auth.currentUserOrNull()?.id ?: ""
                if (currentUserId.isNotEmpty()) {
                    val currentName = _userName.value.takeIf { !it.isNullOrBlank() } ?: "Me"
                    val currentInitials = currentName.take(2).uppercase()
                    val currentXp = _userXP.value ?: 0
                    _xpLeaderboardList.postValue(listOf(XpLeaderboardUser(currentUserId, currentName, currentInitials, currentXp, true)))
                }
            }
        }
    }

    fun fetchLeaderboard() {
        viewModelScope.launch {
            try {
                val currentUserId = supabase.auth.currentUserOrNull()?.id ?: ""
                
                val cal = java.util.Calendar.getInstance()
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                cal.set(java.util.Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                val startOfWeekMs = cal.timeInMillis
                val startOfWeekIso = java.time.Instant.ofEpochMilli(startOfWeekMs).toString()
                
                val sessionDtos = try {
                    val rawObjects = supabase.from("sessions")
                        .select {
                            filter {
                                gte("completed_at", startOfWeekIso)
                            }
                        }
                        .decodeList<kotlinx.serialization.json.JsonObject>()
                        
                    rawObjects.map { obj ->
                        SupabaseSessionDto(
                            id = obj["id"],
                            timestamp = obj["completed_at"]?.jsonPrimitive?.contentOrNull,
                            durationMinutes = obj["duration_minutes"],
                            userId = obj["user_id"]?.jsonPrimitive?.contentOrNull
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("Leaderboard", "Error decoding sessions", e)
                    emptyList()
                }

                val sessions = sessionDtos.mapNotNull { dto ->
                    try {
                        if (dto.timestamp == null) return@mapNotNull null
                        val localMs = parseTimestampToMs(dto.timestamp)
                        StudySession(dto.getIdString(), localMs, dto.getDurationMinutes(), dto.userId ?: "")
                    } catch (e: Exception) {
                        null
                    }
                }.toMutableList()

                // Add local sessions that correspond to the current week
                val _localSessions = studySessionRepository.getAllSessionsSync()
                val currentWeekLocal = _localSessions.filter { it.timestamp >= startOfWeekMs && it.userId == currentUserId }
                for (localSession in currentWeekLocal) {
                    if (sessions.none { it.id == localSession.id }) {
                        sessions.add(localSession)
                    }
                }

                val userMinutes = sessions.groupBy { it.userId }
                    .mapValues { entry ->
                        entry.value.sumOf { it.durationMinutes }
                    }
                
                val currentName = _userName.value.takeIf { !it.isNullOrBlank() } ?: "Me"
                val currentInitials = currentName.take(2).uppercase()
                
                val userIds = userMinutes.keys.filter { it.isNotBlank() }
                val profileMap = mutableMapOf<String, UserProfile>()
                if (userIds.isNotEmpty()) {
                    try {
                        val profiles = try {
                            val rawObjects = supabase.from("users")
                            .select {
                                filter {
                                    isIn("id", userIds)
                                }
                            }.decodeList<kotlinx.serialization.json.JsonObject>()
                            
                            rawObjects.map { obj ->
                                UserProfile(
                                    id = obj["id"]?.jsonPrimitive?.contentOrNull ?: "",
                                    full_name = obj["full_name"]?.jsonPrimitive?.contentOrNull,
                                    streak = obj["streak"],
                                    xp = obj["xp"],
                                    study_commitment_duration = obj["study_commitment_duration"],
                                    study_commitment_start_date = obj["study_commitment_start_date"]
                                )
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("Leaderboard", "Error decoding weekly users", e)
                            emptyList()
                        }
                        profileMap.putAll(profiles.associateBy { it.id })
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                
                var topUsers = userMinutes.map { (userId, totalMins) ->
                    val isCurrent = userId == currentUserId
                    var rawName = profileMap[userId]?.full_name?.takeIf { it.isNotBlank() } ?: "Anonymous"
                    if (rawName.startsWith("User ", ignoreCase = true) || rawName.matches(Regex("(?i)^user[a-f0-9]{4}\$"))) {
                        rawName = "Anonymous"
                    }
                    val name = if (isCurrent) currentName else rawName
                    val initials = if (isCurrent) currentInitials else name.take(2).uppercase()
                    LeaderboardUser(userId, name, initials, totalMins, isCurrent)
                }.sortedByDescending { it.weeklyMinutes }

                if (topUsers.isEmpty() && currentUserId.isNotEmpty()) {
                    topUsers = listOf(LeaderboardUser(currentUserId, currentName, currentInitials, 0, true))
                } else if (currentUserId.isNotEmpty() && !topUsers.any { it.id == currentUserId }) {
                    topUsers = topUsers + LeaderboardUser(currentUserId, currentName, currentInitials, 0, true)
                    topUsers = topUsers.sortedByDescending { it.weeklyMinutes }
                }

                _leaderboardList.postValue(topUsers)
            } catch (e: Exception) {
                android.util.Log.e("Leaderboard", "Error fetching weekly leaderboard", e)
                val msg = e.message ?: "Unknown error"
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(getApplication(), "Weekly Leaderboard failed: $msg", android.widget.Toast.LENGTH_LONG).show()
                }
                
                // Fallback to local user
                val currentUserId = supabase.auth.currentUserOrNull()?.id ?: ""
                if (currentUserId.isNotEmpty()) {
                    val currentName = _userName.value.takeIf { !it.isNullOrBlank() } ?: "Me"
                    val currentInitials = currentName.take(2).uppercase()
                    _leaderboardList.postValue(listOf(LeaderboardUser(currentUserId, currentName, currentInitials, 0, true)))
                }
            }
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            if (syncMutex.isLocked) {
                android.util.Log.d("Sync", "Sync already in progress. Skipping duplicate call.")
                return@launch
            }
            syncMutex.withLock {
                try {
                    val user = supabase.auth.currentUserOrNull() ?: return@withLock
                    android.util.Log.d("Sync", "Safe sync starting under lock...")
                    
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        android.widget.Toast.makeText(
                            getApplication(),
                            "Syncing cloud data...",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                    
                    // 1. Sync study sessions from Supabase to local Room database FIRST.
                    syncSessionsFromSupabase()
                    
                    // 2. Sync other items
                    syncTasksFromSupabase()
                    syncScheduleSlotsFromSupabase()
                    syncNotesFromSupabase()
                    
                    // 3. Sync profile from Supabase
                    syncUserProfileFromSupabase()
                    
                    // 4. Update the profile on Supabase with correct merged metrics
                    syncUserProfileToSupabase()
                    
                    // 5. Update Leaderboards
                    fetchLeaderboard()
                    fetchXpLeaderboard()
                    
                    android.util.Log.d("Sync", "Safe sync completed successfully under lock")
                } catch (e: java.util.ConcurrentModificationException) {
                    // Ignore concurrent list changes if they happen
                } catch (e: Exception) {
                    android.util.Log.e("Sync", "Error during safe sync", e)
                }
            }
        }
    }

    fun setUserName(name: String) {
        sharedPreferences.edit().putString("user_name", name).apply()
        _userName.postValue(name)
        viewModelScope.launch {
            syncUserProfileToSupabase()
        }
    }

    private val _userXP = MutableLiveData<Int>(sharedPreferences.getInt("user_xp", 0))
    val userXP: LiveData<Int> get() = _userXP

    fun addXP(amount: Int) {
        val oldXp = (_userXP.value ?: 0)
        val oldLevel = (oldXp / 100) + 1
        val newXp = oldXp + amount
        sharedPreferences.edit().putInt("user_xp", newXp).commit()
        _userXP.postValue(newXp)
        
        val newLevel = (newXp / 100) + 1
        if (newLevel > oldLevel) {
            NotificationHelper.showNotification(
                getApplication(),
                "Level Up!",
                "You reached Level $newLevel!",
                NotificationHelper.CHANNEL_ACHIEVEMENTS
            )
        }
        
        viewModelScope.launch {
            syncUserProfileToSupabase()
            checkAndTriggerAchievements()
        }
    }

    fun deductXP(amount: Int): Boolean {
        val currentXp = _userXP.value ?: 0
        if (currentXp < amount) return false
        val newXp = currentXp - amount
        sharedPreferences.edit().putInt("user_xp", newXp).commit()
        _userXP.postValue(newXp)
        viewModelScope.launch {
            syncUserProfileToSupabase()
        }
        return true
    }

    fun isRewardUnlocked(rewardId: String): Boolean {
        return sharedPreferences.getBoolean("reward_unlocked_$rewardId", false)
    }

    fun unlockReward(rewardId: String) {
        sharedPreferences.edit().putBoolean("reward_unlocked_$rewardId", true).apply()
    }

    // Settings
    private val _notificationSound = MutableLiveData<Boolean>(sharedPreferences.getBoolean("notification_sound", true))
    val notificationSound: LiveData<Boolean> get() = _notificationSound
    fun setNotificationSound(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("notification_sound", enabled).apply()
        _notificationSound.postValue(enabled)
    }

    private val _breakReminder = MutableLiveData<Boolean>(sharedPreferences.getBoolean("break_reminder", true))
    val breakReminder: LiveData<Boolean> get() = _breakReminder
    fun setBreakReminder(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("break_reminder", enabled).apply()
        _breakReminder.postValue(enabled)
    }

    private val _studyCommitmentDuration = MutableLiveData<Int>(sharedPreferences.getInt("study_commitment_duration", 1))
    val studyCommitmentDuration: LiveData<Int> get() = _studyCommitmentDuration
    
    private val _studyCommitmentStartDate = MutableLiveData<Long?>(
        if (sharedPreferences.contains("study_commitment_start_date")) {
            sharedPreferences.getLong("study_commitment_start_date", 0L)
        } else {
            val now = System.currentTimeMillis()
            sharedPreferences.edit().putLong("study_commitment_start_date", now).apply()
            now
        }
    )
    val studyCommitmentStartDate: LiveData<Long?> get() = _studyCommitmentStartDate

    private val _weeklyGoalHours = MutableLiveData<Int>(sharedPreferences.getInt("weekly_goal_hours", 10))
    val weeklyGoalHours: LiveData<Int> get() = _weeklyGoalHours

    fun setWeeklyGoalHours(hours: Int) {
        _weeklyGoalHours.value = hours
        sharedPreferences.edit().putInt("weekly_goal_hours", hours).apply()
    }

    fun setStudyCommitmentDuration(months: Int) {
        sharedPreferences.edit().putInt("study_commitment_duration", months).apply()
        _studyCommitmentDuration.postValue(months)
        
        // Also update start date if not set, or reset
        val now = System.currentTimeMillis()
        sharedPreferences.edit().putLong("study_commitment_start_date", now).apply()
        _studyCommitmentStartDate.postValue(now)
        
        viewModelScope.launch {
            syncUserProfileToSupabase()
        }
    }

    private val _appLanguage = MutableLiveData<String>(sharedPreferences.getString("app_language", "English") ?: "English")
    val appLanguage: LiveData<String> get() = _appLanguage
    fun setAppLanguage(language: String) {
        sharedPreferences.edit().putString("app_language", language).apply()
        _appLanguage.postValue(language)
    }

    private val _isDarkMode = MutableLiveData<Boolean?>(
        if (sharedPreferences.contains("is_dark_mode")) sharedPreferences.getBoolean("is_dark_mode", false) else null
    )
    val isDarkMode: LiveData<Boolean?> get() = _isDarkMode
    
    private val _isFocusModeOn = MutableLiveData<Boolean>(false)
    val isFocusModeOn: LiveData<Boolean> get() = _isFocusModeOn
    
    fun toggleFocusMode(context: Context) {
        val current = _isFocusModeOn.value ?: false
        if (!current) {
            if (FocusModeHelper.hasDndAccess(context)) {
                FocusModeHelper.setFocusMode(context, true)
                _isFocusModeOn.value = true
            } else {
                FocusModeHelper.requestDndAccess(context)
            }
        } else {
            if (FocusModeHelper.hasDndAccess(context)) {
                FocusModeHelper.setFocusMode(context, false)
            }
            _isFocusModeOn.value = false
        }
    }

    fun setDarkMode(isDark: Boolean) {
        sharedPreferences.edit().putBoolean("is_dark_mode", isDark).apply()
        _isDarkMode.value = isDark
    }

    private val _dailyReminderTime = MutableLiveData<String?>(sharedPreferences.getString("reminder_time", null))
    val dailyReminderTime: LiveData<String?> get() = _dailyReminderTime

    fun setDailyReminder(hour: Int, minute: Int) {
        val timeString = String.format("%02d:%02d", hour, minute)
        sharedPreferences.edit().putString("reminder_time", timeString).apply()
        _dailyReminderTime.value = timeString
        NotificationHelper.scheduleDailyReminder(getApplication(), hour, minute)
    }

    fun clearDailyReminder() {
        sharedPreferences.edit().remove("reminder_time").apply()
        _dailyReminderTime.value = null
        NotificationHelper.cancelDailyReminder(getApplication())
    }

    val currentSessionStatus = supabase.auth.sessionStatus


    private val _authError = MutableLiveData<String?>(null)
    val authError: LiveData<String?> get() = _authError

    private val _isLoadingAuth = MutableLiveData<Boolean>(false)
    val isLoadingAuth: LiveData<Boolean> get() = _isLoadingAuth

    private val _isLoadingTasks = MutableLiveData<Boolean>(false)
    val isLoadingTasks: LiveData<Boolean> get() = _isLoadingTasks

    private val _isLoadingNotes = MutableLiveData<Boolean>(false)
    val isLoadingNotes: LiveData<Boolean> get() = _isLoadingNotes

    private val _isLoadingSchedule = MutableLiveData<Boolean>(false)
    val isLoadingSchedule: LiveData<Boolean> get() = _isLoadingSchedule

    fun signUp(emailStr: String, passwordStr: String, fullName: String) {
        if (emailStr.isBlank() || passwordStr.isBlank() || fullName.isBlank()) {
            _authError.value = "Name, email and password cannot be empty"
            return
        }
        viewModelScope.launch {
            _isLoadingAuth.value = true
            _authError.value = null
            try {
                val user = supabase.auth.signUpWith(Email) {
                    email = emailStr.trim()
                    password = passwordStr.trim()
                }
                // Save name locally
                setUserName(fullName)
                // Insert into users table
                try {
                    val authUser = supabase.auth.currentUserOrNull()
                    if (authUser != null) {
                        @kotlinx.serialization.Serializable
                        data class InsertUserDto(val id: String, val full_name: String, val streak: Int, val xp: Int)
                        val newProfile = InsertUserDto(id = authUser.id, full_name = fullName.trim(), streak = 0, xp = 0)
                        supabase.from("users").insert(newProfile)
                    }
                } catch (e: Exception) {
                    // ignore
                }
            } catch (e: Exception) {
                val msg = e.message ?: ""
                if (msg.contains("UnknownHostException") || msg.contains("ConnectException") || msg.contains("Failed to connect") || e is java.net.UnknownHostException || e is java.net.ConnectException) {
                    _authError.value = "No internet connection. Please check your network."
                } else {
                    _authError.value = "Email already in use"
                }
            } finally {
                _isLoadingAuth.value = false
            }
        }
    }

    fun signIn(emailStr: String, passwordStr: String) {
        if (emailStr.isBlank() || passwordStr.isBlank()) {
            _authError.value = "Email and password cannot be empty"
            return
        }
        viewModelScope.launch {
            _isLoadingAuth.value = true
            _authError.value = null
            try {
                supabase.auth.signInWith(Email) {
                    email = emailStr.trim()
                    password = passwordStr.trim()
                }
                triggerSync()
            } catch (e: Exception) {
                val msg = e.message ?: ""
                if (msg.contains("UnknownHostException") || msg.contains("ConnectException") || msg.contains("Failed to connect") || e is java.net.UnknownHostException || e is java.net.ConnectException) {
                    _authError.value = "No internet connection. Please check your network."
                } else {
                    _authError.value = "Invalid email or password"
                }
            } finally {
                _isLoadingAuth.value = false
            }
        }
    }

    fun signInWithGoogle(token: String) {
        viewModelScope.launch {
            _isLoadingAuth.value = true
            _authError.value = null
            try {
                supabase.auth.signInWith(io.github.jan.supabase.gotrue.providers.builtin.IDToken) {
                    idToken = token
                    provider = io.github.jan.supabase.gotrue.providers.Google
                }
                try {
                    val authUser = supabase.auth.currentUserOrNull()
                    if (authUser != null) {
                        val existingProfile = supabase.from("users").select { filter { eq("id", authUser.id) } }.decodeList<UserProfile>()
                        if (existingProfile.isEmpty()) {
                            @kotlinx.serialization.Serializable
                            data class InsertUserDto(val id: String, val full_name: String, val streak: Int, val xp: Int)
                            val newProfile = InsertUserDto(id = authUser.id, full_name = "", streak = 0, xp = 0)
                            supabase.from("users").insert(newProfile)
                        }
                    }
                } catch (e: Exception) {
                    // ignore
                }
                triggerSync()
            } catch (e: Exception) {
                _authError.value = e.message ?: "Google sign in failed"
            } finally {
                _isLoadingAuth.value = false
            }
        }
    }

    fun sendPasswordResetEmail(emailStr: String) {
        if (emailStr.isBlank()) {
            _authError.value = "Email cannot be empty"
            return
        }
        viewModelScope.launch {
            _isLoadingAuth.value = true
            _authError.value = null
            try {
                supabase.auth.resetPasswordForEmail(email = emailStr.trim())
                _authError.value = "Password reset email sent!"
            } catch (e: Exception) {
                val msg = e.message ?: ""
                if (msg.contains("UnknownHostException") || msg.contains("ConnectException") || msg.contains("Failed to connect") || e is java.net.UnknownHostException || e is java.net.ConnectException) {
                    _authError.value = "No internet connection. Please check your network."
                } else {
                    _authError.value = "Failed to send reset email"
                }
            } finally {
                _isLoadingAuth.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                supabase.auth.signOut()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                val user = supabase.auth.currentUserOrNull()
                if (user != null) {
                    supabase.from("users").delete { filter { eq("id", user.id) } }
                    supabase.from("sessions").delete { filter { eq("user_id", user.id) } }
                    supabase.from("tasks").delete { filter { eq("user_id", user.id) } }
                    supabase.from("notes").delete { filter { eq("user_id", user.id) } }
                    // Also delete schedule slots if needed, or chat...
                    supabase.from("schedule_slots").delete { filter { eq("user_id", user.id) } }
                    
                    // Note: This does not delete the user from Supabase auth
                    // We just wipe their public data and log them out
                }
                
                // Clear local database
                repository.clearTasks()
                scheduleRepository.clear()
                noteRepository.clearNotes()
                studySessionRepository.clear()
                chatMessageRepository.clearMessages()
                
                // Erase shared prefs
                sharedPreferences.edit().clear().apply()
                prefs.edit().clear().apply()

                supabase.auth.signOut()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun dismissAuthError() {
        _authError.value = null
    }

    fun getCurrentUserEmail(): String? {
        return supabase.auth.currentUserOrNull()?.email
    }

    private val database = DatabaseProvider.getDatabase(application)
    private val repository = TaskRepository(database.taskDao())
    private val scheduleRepository = ScheduleRepository(database.scheduleSlotDao())
    private val noteRepository = NoteRepository(database.noteDao())
    private val studySessionRepository = StudySessionRepository(database.studySessionDao())
    private val chatMessageRepository = ChatMessageRepository(database.chatMessageDao())

    private val prefs = application.getSharedPreferences("lockedin_prefs", Context.MODE_PRIVATE)

    private val _canClaimDailyReward = MutableLiveData<Boolean>(checkCanClaimReward())
    val canClaimDailyReward: LiveData<Boolean> get() = _canClaimDailyReward

    private fun checkCanClaimReward(): Boolean {
        val today = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())
        val lastClaimed = prefs.getString("last_daily_reward_date", "")
        return lastClaimed != today
    }

    fun claimDailyReward() {
        if (checkCanClaimReward()) {
            val today = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())
            prefs.edit().putString("last_daily_reward_date", today).apply()
            // XP increases only when sessions are completed.
            _canClaimDailyReward.postValue(false)
        }
    }

    private val _commitmentDays = MutableLiveData<Int>(prefs.getInt("commitment_days", -1))
    val commitmentDays: LiveData<Int> get() = _commitmentDays

    private val _commitmentEndTimestamp = MutableLiveData<Long>(prefs.getLong("commitment_end_timestamp", -1L))
    val commitmentEndTimestamp: LiveData<Long> get() = _commitmentEndTimestamp

    private val _commitmentSet = MutableLiveData<Boolean>(prefs.getBoolean("commitment_set", false))
    val commitmentSet: LiveData<Boolean> get() = _commitmentSet

    fun saveCommitment(days: Int) {
        val endTimestamp = System.currentTimeMillis() + days * 24L * 60 * 60 * 1000L
        prefs.edit()
            .putInt("commitment_days", days)
            .putLong("commitment_end_timestamp", endTimestamp)
            .putBoolean("commitment_set", true)
            .apply()

        _commitmentDays.value = days
        _commitmentEndTimestamp.value = endTimestamp
        _commitmentSet.value = true
    }

    fun clearCommitment() {
        prefs.edit()
            .remove("commitment_days")
            .remove("commitment_end_timestamp")
            .remove("commitment_set")
            .apply()

        _commitmentDays.value = -1
        _commitmentEndTimestamp.value = -1L
        _commitmentSet.value = false
    }

    val tasks: LiveData<List<Task>> = repository.allTasks.asLiveData()
    val scheduleSlots: LiveData<List<ScheduleSlot>> = scheduleRepository.allSlots.asLiveData()
    val notes: LiveData<List<Note>> = noteRepository.allNotes.asLiveData()
    val studySessions: LiveData<List<StudySession>> = studySessionRepository.allSessions.asLiveData()
    val lastChatMessages: LiveData<List<ChatMessage>> = chatMessageRepository.lastMessages.asLiveData()
    val streak: LiveData<Int> = studySessionRepository.allSessions
        .map { sessions -> calculateStreak(sessions) }
        .asLiveData()

    val bestStreak: LiveData<Int> = studySessionRepository.allSessions
        .map { sessions -> calculateBestStreak(sessions) }
        .asLiveData()

    val weeklyStudyMinutes: LiveData<Int> = studySessionRepository.allSessions
        .map { sessions ->
            val sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
            sessions.filter { it.timestamp >= sevenDaysAgo }.sumOf { it.durationMinutes }
        }
        .asLiveData()

    val weeklySessionsCompleted: LiveData<Int> = studySessionRepository.allSessions
        .map { sessions ->
            val sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
            sessions.count { it.timestamp >= sevenDaysAgo }
        }
        .asLiveData()

    val weeklyStudyData: LiveData<List<Float>> = studySessionRepository.allSessions
        .map { sessions ->
            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val startOfToday = calendar.timeInMillis
            
            val dailyMinutes = FloatArray(7) { 0f }
            // indices: 6 is today, 5 is yesterday, ..., 0 is 6 days ago
            
            for (session in sessions) {
                if (session.timestamp >= startOfToday - 6L * 24 * 60 * 60 * 1000L) {
                    val daysAgo = ((startOfToday - session.timestamp) / (24L * 60 * 60 * 1000L)).toInt().coerceIn(0, 6)
                    val diffToToday = if (session.timestamp >= startOfToday) 0 else daysAgo + 1
                    
                    if (diffToToday in 0..6) {
                        dailyMinutes[6 - diffToToday] += session.durationMinutes.toFloat()
                    }
                }
            }
            dailyMinutes.toList()
        }
        .asLiveData()

    fun addTask(name: String?, priority: String?) {
        val safeName = name?.trim() ?: return
        if (safeName.isEmpty()) return
        val safePriority = priority ?: "Medium"

        viewModelScope.launch {
            try {
                val user = try { supabase.auth.currentUserOrNull() } catch (e: Exception) { null }
                val userId = user?.id ?: ""
                val task = Task(
                    id = java.util.UUID.randomUUID().toString(),
                    name = safeName, 
                    priority = safePriority, 
                    isCompleted = false,
                    timestamp = System.currentTimeMillis(),
                    userId = userId
                )
                
                repository.insert(task)
                
                if (userId.isNotEmpty()) {
                    try {
                        supabase.from("tasks").insert(task)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleTaskComplete(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            repository.update(updatedTask)
            
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                try {
                    supabase.from("tasks").update(
                        {
                            set("is_completed", updatedTask.isCompleted)
                        }
                    ) {
                        filter {
                            eq("id", updatedTask.id)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.update(task)
            
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                try {
                    supabase.from("tasks").update(
                        {
                            set("is_completed", task.isCompleted)
                            set("name", task.name)
                            set("priority", task.priority)
                        }
                    ) {
                        filter {
                            eq("id", task.id)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteById(task.id)
            
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                try {
                    supabase.from("tasks").delete {
                        filter {
                            eq("id", task.id)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    fun addScheduleSlot(startTime: String, endTime: String, taskName: String, colorHex: String) {
        if (taskName.isBlank() || startTime.isBlank() || endTime.isBlank()) return
        viewModelScope.launch {
            val user = supabase.auth.currentUserOrNull()
            val userId = user?.id ?: ""
            val slot = ScheduleSlot(
                startTime = startTime.trim(),
                endTime = endTime.trim(),
                taskName = taskName.trim(),
                colorHex = colorHex,
                userId = userId
            )
            scheduleRepository.insert(slot)
            NotificationHelper.scheduleNotification(getApplication(), slot)

            if (userId.isNotEmpty()) {
                try {
                    supabase.from("schedule_slots").insert(slot)
                } catch (e: Exception) {
                    // Ignore for now
                }
            }
        }
    }

    fun deleteScheduleSlot(slot: ScheduleSlot) {
        viewModelScope.launch {
            scheduleRepository.deleteById(slot.id)
            NotificationHelper.cancelNotification(getApplication(), slot)

            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                try {
                    supabase.from("schedule_slots").delete {
                        filter {
                            eq("id", slot.id)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    fun addChatMessage(text: String, isUser: Boolean) {
        viewModelScope.launch {
            chatMessageRepository.insertMessage(ChatMessage(text = text, isUser = isUser))
        }
    }

    fun clearChatMessages() {
        viewModelScope.launch {
            chatMessageRepository.clearMessages()
        }
    }

    fun addNote(type: String, title: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val user = supabase.auth.currentUserOrNull()
            val userId = user?.id ?: ""
            val note = Note(
                type = type,
                title = title.trim(),
                content = content.trim(),
                userId = userId
            )
            noteRepository.insert(note)

            if (userId.isNotEmpty()) {
                try {
                    supabase.from("notes").insert(note)
                } catch (e: Exception) {
                    // Ignore for now
                }
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.deleteById(note.id)

            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                try {
                    supabase.from("notes").delete {
                        filter {
                            eq("id", note.id)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    enum class SessionType { POMODORO_25, EXTENDED_50, CUSTOM }

    private val _selectedSessionType = MutableLiveData<SessionType>(SessionType.POMODORO_25)
    val selectedSessionType: LiveData<SessionType> get() = _selectedSessionType

    private val _customMinutes = MutableLiveData<Int>(15)
    val customMinutes: LiveData<Int> get() = _customMinutes

    private val _isLockedIn = MutableLiveData<Boolean>(false)
    val isLockedIn: LiveData<Boolean> get() = _isLockedIn

    private val _isOnBreak = MutableLiveData<Boolean>(false)
    val isOnBreak: LiveData<Boolean> get() = _isOnBreak

    private val _isPaused = MutableLiveData<Boolean>(false)
    val isPaused: LiveData<Boolean> get() = _isPaused

    private val _timeRemainingString = MutableLiveData<String>("25:00")
    val timeRemainingString: LiveData<String> get() = _timeRemainingString

    private val _sessionProgress = MutableLiveData<Float>(1.0f)
    val sessionProgress: LiveData<Float> get() = _sessionProgress

    private val _sessionsCompleted = MutableLiveData<Int>(0)
    val sessionsCompleted: LiveData<Int> get() = _sessionsCompleted

    private val _sessionCompletedMessage = MutableLiveData<String?>(null)
    val sessionCompletedMessage: LiveData<String?> get() = _sessionCompletedMessage

    private val _sessionLabel = MutableLiveData<String>("Focus Session")
    val sessionLabel: LiveData<String> get() = _sessionLabel

    fun setSessionLabel(label: String) {
        _sessionLabel.value = label
    }

    private val _showCompletionDialog = MutableLiveData<Boolean>(false)
    val showCompletionDialog: LiveData<Boolean> get() = _showCompletionDialog

    private val _lastEarnedXp = MutableLiveData<Int>(0)
    val lastEarnedXp: LiveData<Int> get() = _lastEarnedXp

    fun dismissCompletionDialog() {
        _showCompletionDialog.value = false
    }

    private val ambientSoundManager = AmbientSoundManager()
    private val _currentSound = MutableLiveData<AmbientSoundManager.SoundType?>(null)
    val currentSound: LiveData<AmbientSoundManager.SoundType?> get() = _currentSound
    
    fun playAmbientSound(type: AmbientSoundManager.SoundType) {
        ambientSoundManager.playSound(type)
        _currentSound.value = type
    }
    
    fun stopAmbientSound() {
        ambientSoundManager.stopSound()
        _currentSound.value = null
    }

    private var countdownTimer: CountDownTimer? = null
    
    val totalSessionDurationMs: Long
        get() {
            return when (_selectedSessionType.value) {
                SessionType.POMODORO_25 -> 25 * 60 * 1000L
                SessionType.EXTENDED_50 -> 50 * 60 * 1000L
                SessionType.CUSTOM -> (_customMinutes.value ?: 15) * 60 * 1000L
                null -> 25 * 60 * 1000L
            }
        }
    
    private var remainingTimeMs = 25 * 60 * 1000L
    private var initialDurationMs = 25 * 60 * 1000L

    init {
        android.widget.Toast.makeText(getApplication(), "ViewModel initialized", android.widget.Toast.LENGTH_SHORT).show()
        resetTimerToSelected()
        
        viewModelScope.launch {
            try {
                val localSessions = studySessionRepository.getAllSessionsSync()
                _sessionsCompleted.postValue(localSessions.size)
            } catch (e: Exception) {
                // Ignore
            }
        }
        
        viewModelScope.launch {
            try {
                if (supabase.auth.currentUserOrNull() != null) {
                    triggerSync()
                }
            } catch (t: Throwable) {
                // Safely handle/ignore any startup Supabase or init exceptions
            }
        }
        
        viewModelScope.launch {
            try {
                currentSessionStatus.collect { status ->
                    try {
                        when (status) {
                            is SessionStatus.Authenticated -> {
                                triggerSync()
                            }
                            is SessionStatus.NotAuthenticated -> {
                                repository.clearTasks()
                                noteRepository.clearNotes()
                                scheduleRepository.clear()
                                studySessionRepository.clear()
                                sharedPreferences.edit().putInt("user_xp", 0).apply()
                                _userXP.postValue(0)
                                _sessionsCompleted.postValue(0)
                            }
                            else -> {}
                        }
                    } catch (e: Exception) {
                        // ignore inside collect to avoid breaking the Flow collector
                    }
                }
            } catch (t: Throwable) {
                // Safely handle/ignore any outer exceptions
            }
        }
    }

    private suspend fun syncTasksFromSupabase() {
        try {
            _isLoadingTasks.postValue(true)
            val user = supabase.auth.currentUserOrNull() ?: return
            val remoteTasks = supabase.from("tasks")
                .select { filter { eq("user_id", user.id) } }
                .decodeList<Task>()
                
            repository.clearTasks()
            remoteTasks.forEach { repository.insert(it) }
        } catch (e: Exception) {
            android.util.Log.e("SyncTasks", "Error syncing tasks from Supabase", e)
        } finally {
            _isLoadingTasks.postValue(false)
        }
    }

    private suspend fun syncNotesFromSupabase() {
        try {
            _isLoadingNotes.postValue(true)
            val user = supabase.auth.currentUserOrNull() ?: return
            val remoteNotes = supabase.from("notes")
                .select { filter { eq("user_id", user.id) } }
                .decodeList<Note>()
                
            noteRepository.clearNotes()
            remoteNotes.forEach { noteRepository.insert(it) }
        } catch (e: Exception) {
            android.util.Log.e("SyncNotes", "Error syncing notes from Supabase", e)
        } finally {
            _isLoadingNotes.postValue(false)
        }
    }

    private suspend fun syncScheduleSlotsFromSupabase() {
        try {
            _isLoadingSchedule.postValue(true)
            val user = supabase.auth.currentUserOrNull() ?: return
            val remoteSlots = supabase.from("schedule_slots")
                .select { filter { eq("user_id", user.id) } }
                .decodeList<ScheduleSlot>()
                
            scheduleRepository.clear()
            remoteSlots.forEach { 
                scheduleRepository.insert(it)
                NotificationHelper.scheduleNotification(getApplication(), it)
            }
        } catch (e: Exception) {
            android.util.Log.e("SyncSchedule", "Error syncing schedule slots from Supabase", e)
        } finally {
            _isLoadingSchedule.postValue(false)
        }
    }

    private fun parseTimestampToMs(timestampStr: String): Long {
        val trimmed = timestampStr.trim()
        if (trimmed.toLongOrNull() != null) {
            return trimmed.toLong()
        }
        try {
            return java.time.Instant.parse(trimmed).toEpochMilli()
        } catch (e1: Exception) {
            var normalized = trimmed.replace(" ", "T")
            if (!normalized.contains("Z") && !normalized.contains("+") && normalized.indexOf('-', 10) == -1) {
                normalized += "Z"
            }
            try {
                return java.time.Instant.parse(normalized).toEpochMilli()
            } catch (e2: Exception) {
                val formats = listOf(
                    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                    "yyyy-MM-dd'T'HH:mm:ssXXX",
                    "yyyy-MM-dd HH:mm:ss.SSS",
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd"
                )
                var parsedTime: Long? = null
                for (pattern in formats) {
                    try {
                        val sdf = java.text.SimpleDateFormat(pattern, java.util.Locale.US)
                        if (!pattern.contains("X")) {
                            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                        }
                        parsedTime = sdf.parse(trimmed)?.time
                        if (parsedTime != null) break
                    } catch (ex: Exception) {
                        // Try next pattern
                    }
                }
                return parsedTime ?: System.currentTimeMillis()
            }
        }
    }

    private suspend fun syncSessionsFromSupabase() {
        try {
            val user = supabase.auth.currentUserOrNull() ?: return
            val remoteSessionDtos = supabase.from("sessions")
                .select { filter { eq("user_id", user.id) } }
                .decodeList<SupabaseSessionDto>()

            android.util.Log.d("SyncSessions", "Fetched ${remoteSessionDtos.size} sessions from Supabase")

            val remoteSessions = remoteSessionDtos.mapNotNull { dto ->
                try {
                    if (dto.timestamp == null) return@mapNotNull null
                    val ms = parseTimestampToMs(dto.timestamp)
                    StudySession(dto.getIdString(), ms, dto.getDurationMinutes(), dto.userId ?: "")
                } catch (pe: Exception) {
                    android.util.Log.e("SyncSessions", "Error parsing individual session: ${dto.timestamp}", pe)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        android.widget.Toast.makeText(getApplication(), "Parse error: ${dto.timestamp}", android.widget.Toast.LENGTH_LONG).show()
                    }
                    null
                }
            }

            val localSessions = studySessionRepository.getAllSessionsSync()

            // Push local sessions to Supabase that is not in remote
            for (localSession in localSessions) {
                if (remoteSessionDtos.none { it.getIdString() == localSession.id }) {
                    try {
                        val isoTimestamp = java.time.Instant.ofEpochMilli(localSession.timestamp).toString()
                        supabase.from("sessions").insert(SupabaseSessionDto(localSession.id, isoTimestamp, localSession.durationMinutes, localSession.userId))
                        android.util.Log.d("SyncSessions", "Successfully pushed local session ${localSession.id} to Supabase")
                    } catch (e: Exception) {
                        android.util.Log.e("SyncSessions", "Failed to push offline session to Supabase", e)
                    }
                }
            }

            // Also re-insert remote ones (updates them locally)
            val updatedLocalSessions = studySessionRepository.getAllSessionsSync()
            studySessionRepository.clear()
            
            // Insert remote
            remoteSessions.forEach { studySessionRepository.insert(it) }
            
            // Re-insert unuploaded, wait actually if we clear then we lose failed uploads!
            // Let's just do a merge. No clearing.
            // DO NOT clear, just insert remote, so we keep offline ones that failed to upload
            // but wait, if it was deleted on remote, it stays local forever.
            // We have no soft-delete flag. We assume remote is source of truth after merging.
            studySessionRepository.clear()
            // Insert the merged union
            val finalLocalSessions = mutableListOf<StudySession>()
            finalLocalSessions.addAll(remoteSessions)
            // Add local sessions that were pushed successfully (or theoretically those not in remote)
            // To prevent local deletion issue on fetch, we'll just insert all local sessions back that aren't in remote
            localSessions.filter { loc -> remoteSessions.none { rem -> rem.id == loc.id } }.forEach { 
                finalLocalSessions.add(it)
            }
            finalLocalSessions.forEach { studySessionRepository.insert(it) }
            
            _sessionsCompleted.postValue(finalLocalSessions.size)

            val totalSessionMins = finalLocalSessions.sumOf { it.durationMinutes }
            val currentLocalXp = sharedPreferences.getInt("user_xp", 0)
            val finalXp = maxOf(currentLocalXp, totalSessionMins)
            sharedPreferences.edit().putInt("user_xp", finalXp).apply()
            _userXP.postValue(finalXp)

            android.util.Log.d("SyncSessions", "Successfully inserted ${remoteSessions.size} sessions into Room database")
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                android.widget.Toast.makeText(
                    getApplication(),
                    "Synced ${remoteSessions.size} sessions from Supabase",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            val msg = e.message ?: "Unknown error"
            android.util.Log.e("SyncSessions", "Error syncing sessions from Supabase", e)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                android.widget.Toast.makeText(
                    getApplication(),
                    "Error syncing sessions: $msg",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private suspend fun syncUserProfileFromSupabase() {
        try {
            val user = supabase.auth.currentUserOrNull() ?: return
            val remoteProfileList = supabase.from("users")
                .select { filter { eq("id", user.id) } }
                .decodeList<UserProfile>()
            
            val remoteProfile = remoteProfileList.firstOrNull()
            val sessions = studySessionRepository.getAllSessionsSync()
            val totalSessionMins = sessions.sumOf { it.durationMinutes }
            val earliestSessionTime = sessions.minOfOrNull { it.timestamp }

            if (remoteProfile != null) {
                if (!remoteProfile.full_name.isNullOrBlank()) {
                    setUserName(remoteProfile.full_name ?: "")
                }
                
                val currentLocalXp = sharedPreferences.getInt("user_xp", 0)
                val finalXp = maxOf(remoteProfile.getXp(), maxOf(currentLocalXp, totalSessionMins))
                sharedPreferences.edit().putInt("user_xp", finalXp).commit()
                _userXP.postValue(finalXp)
                
                val duration = remoteProfile.getStudyCommitmentDuration() ?: 1
                sharedPreferences.edit().putInt("study_commitment_duration", duration).commit()
                _studyCommitmentDuration.postValue(duration)
                
                // Set commitment_set so that they can bypass the commitment configuration screen
                prefs.edit().putBoolean("commitment_set", true).commit()
                _commitmentSet.postValue(true)
                
                var finalCommitmentStartDate = remoteProfile.getStudyCommitmentStartDate()
                if (earliestSessionTime != null) {
                    if (finalCommitmentStartDate == null || earliestSessionTime < finalCommitmentStartDate) {
                        finalCommitmentStartDate = earliestSessionTime
                    }
                }
                
                if (finalCommitmentStartDate != null) {
                    sharedPreferences.edit().putLong("study_commitment_start_date", finalCommitmentStartDate).apply()
                    _studyCommitmentStartDate.postValue(finalCommitmentStartDate)
                    
                    val endTimestamp = finalCommitmentStartDate + (duration * 30L * 24L * 60L * 60L * 1000L)
                    prefs.edit().putLong("commitment_end_timestamp", endTimestamp).apply()
                    _commitmentEndTimestamp.postValue(endTimestamp)
                } else {
                    val now = System.currentTimeMillis()
                    sharedPreferences.edit().putLong("study_commitment_start_date", now).apply()
                    _studyCommitmentStartDate.postValue(now)
                    
                    val endTimestamp = now + (duration * 30L * 24L * 60L * 60L * 1000L)
                    prefs.edit().putLong("commitment_end_timestamp", endTimestamp).apply()
                    _commitmentEndTimestamp.postValue(endTimestamp)
                }
            } else {
                // First time user, init profile
                val finalXp = maxOf(0, totalSessionMins)
                sharedPreferences.edit().putInt("user_xp", finalXp).apply()
                _userXP.postValue(finalXp)

                val currentName = sharedPreferences.getString("user_name", "") ?: ""
                @kotlinx.serialization.Serializable
                data class InsertUserDto(val id: String, val full_name: String, val streak: Int, val xp: Int)
                val newProfile = InsertUserDto(
                    id = user.id, 
                    full_name = currentName,
                    streak = 0, 
                    xp = finalXp
                )
                supabase.from("users").insert(newProfile)

                val startDate = earliestSessionTime ?: System.currentTimeMillis()
                sharedPreferences.edit().putLong("study_commitment_start_date", startDate).commit()
                _studyCommitmentStartDate.postValue(startDate)
            }
        } catch (e: Exception) {
            val msg = e.message ?: "Unknown error"
            android.util.Log.e("SyncUserProfile", "Error syncing user profile from Supabase", e)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                android.widget.Toast.makeText(
                    getApplication(),
                    "Error syncing profile: $msg",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun setSessionType(type: SessionType) {
        if (_isLockedIn.value == true || _isOnBreak.value == true) return
        _selectedSessionType.value = type
        resetTimerToSelected()
    }

    fun setCustomMinutes(minutes: Int) {
        if (_isLockedIn.value == true || _isOnBreak.value == true) return
        val validMins = minutes.coerceIn(1, 180) // simple sanity limit
        _customMinutes.value = validMins
        if (_selectedSessionType.value == SessionType.CUSTOM) {
            resetTimerToSelected()
        }
    }

    fun resetTimerToSelected() {
        val mins = when (_selectedSessionType.value) {
            SessionType.POMODORO_25 -> 25
            SessionType.EXTENDED_50 -> 50
            SessionType.CUSTOM -> _customMinutes.value ?: 15
            null -> 25
        }
        _timeRemainingString.value = String.format("%02d:00", mins)
        _sessionProgress.value = 1.0f
        remainingTimeMs = mins * 60 * 1000L
    }

    fun toggleLockIn() {
        if (_isLockedIn.value == true || _isOnBreak.value == true) {
            cancelTimer()
        } else {
            _sessionCompletedMessage.value = null
            _isOnBreak.value = false
            _isLockedIn.value = true
            initialDurationMs = totalSessionDurationMs
            remainingTimeMs = totalSessionDurationMs
            _isPaused.value = false
            startTimer(remainingTimeMs)
        }
    }

    fun dismissCompletedMessage() {
        _sessionCompletedMessage.value = null
    }

    fun togglePause() {
        if (_isLockedIn.value != true && _isOnBreak.value != true) return

        if (_isPaused.value == true) {
            _isPaused.value = false
            startTimer(remainingTimeMs)
            if (_isOnBreak.value == true) {
                NotificationHelper.scheduleBreakEndNotification(getApplication(), remainingTimeMs)
            }
        } else {
            _isPaused.value = true
            countdownTimer?.cancel()
            if (_isOnBreak.value == true) {
                NotificationHelper.cancelBreakEndNotification(getApplication())
            }
        }
    }

    private fun startTimer(durationMs: Long) {
        countdownTimer?.cancel()
        countdownTimer = object : CountDownTimer(durationMs, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTimeMs = millisUntilFinished
                val totalSeconds = (millisUntilFinished / 1000).toInt()
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                _timeRemainingString.value = String.format("%02d:%02d", minutes, seconds)
                _sessionProgress.value = millisUntilFinished.toFloat() / initialDurationMs.toFloat()
            }

            override fun onFinish() {
                try {
                    val toneGenerator = android.media.ToneGenerator(android.media.AudioManager.STREAM_ALARM, 100)
                    toneGenerator.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 500)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val isOnBreakNow = _isOnBreak.value ?: false
                if (isOnBreakNow) {
                    // Break completed!
                    _isOnBreak.value = false
                    _isPaused.value = false
                    resetTimerToSelected()
                    _sessionCompletedMessage.value = "Break complete! Ready to lock in? ⚡"
                    if (_breakReminder.value == true) {
                        NotificationHelper.showNotification(
                            getApplication(),
                            title = "Break Over! \uD83D\uDCAA",
                            channelId = NotificationHelper.CHANNEL_BREAK_ALERTS,
                            message = "Time to get back to work. Stay Locked In!",
                            notificationId = 1001
                        )
                    }
                } else {
                    // Focus session completed!
                    _isLockedIn.value = false
                    _isPaused.value = false
                    _sessionsCompleted.value = (_sessionsCompleted.value ?: 0) + 1
                    _sessionCompletedMessage.value = "Session Complete! Great work 🎉"
                    
                    // Record in database
                    val durationMins = when (_selectedSessionType.value) {
                        SessionType.POMODORO_25 -> 25
                        SessionType.EXTENDED_50 -> 50
                        SessionType.CUSTOM -> _customMinutes.value ?: 15
                        null -> 25
                    }
                    addXP(durationMins)
                    recordStudySession(durationMins)
                    _lastEarnedXp.value = durationMins
                    _showCompletionDialog.value = true

                    // Determine break duration
                    val breakMins = when (_selectedSessionType.value) {
                        SessionType.POMODORO_25 -> 5
                        SessionType.EXTENDED_50 -> 10
                        SessionType.CUSTOM -> 5
                        null -> 5
                    }
                    _isOnBreak.value = true
                    initialDurationMs = breakMins * 60 * 1000L
                    remainingTimeMs = initialDurationMs
                    NotificationHelper.scheduleBreakEndNotification(getApplication(), remainingTimeMs)
                    startTimer(remainingTimeMs)
                }
            }
        }.start()
    }

    fun cancelTimer() {
        countdownTimer?.cancel()
        NotificationHelper.cancelBreakEndNotification(getApplication())
        _isLockedIn.value = false
        _isOnBreak.value = false
        _isPaused.value = false
        resetTimerToSelected()
    }

    @kotlinx.serialization.Serializable
    private data class SupabaseSessionDto(
        val id: kotlinx.serialization.json.JsonElement? = null,
        @kotlinx.serialization.SerialName("completed_at") val timestamp: String? = null,
        @kotlinx.serialization.SerialName("duration_minutes") val durationMinutes: kotlinx.serialization.json.JsonElement? = null,
        @kotlinx.serialization.SerialName("user_id") val userId: String? = ""
    ) {
        constructor(id: String, timestamp: String?, duration_minutes: Int, userId: String) : this(
            id = kotlinx.serialization.json.JsonPrimitive(id),
            timestamp = timestamp,
            durationMinutes = kotlinx.serialization.json.JsonPrimitive(duration_minutes),
            userId = userId
        )

        fun getIdString(): String {
            val element = id ?: return java.util.UUID.randomUUID().toString()
            if (element is kotlinx.serialization.json.JsonPrimitive) {
                return element.content
            }
            return java.util.UUID.randomUUID().toString()
        }

        fun getDurationMinutes(): Int {
            val element = durationMinutes ?: return 0
            if (element is kotlinx.serialization.json.JsonPrimitive) {
                val content = element.content
                return content.toIntOrNull() ?: content.toDoubleOrNull()?.toInt() ?: 0
            }
            return 0
        }
    }

    fun recordStudySession(durationMins: Int) {
        viewModelScope.launch {
            val user = supabase.auth.currentUserOrNull()
            val userId = user?.id ?: ""
            val session = StudySession(
                durationMinutes = durationMins,
                userId = userId
            )
            studySessionRepository.insert(session)
            if (userId.isNotEmpty()) {
                try {
                    val isoTimestamp = java.time.Instant.ofEpochMilli(session.timestamp).toString()
                    supabase.from("sessions").insert(SupabaseSessionDto(session.id, isoTimestamp, session.durationMinutes, session.userId))
                } catch (e: Exception) {
                    // Ignore
                }
            }
            syncUserProfileToSupabase()
            checkAndTriggerAchievements()

            val sessions = studySessionRepository.getAllSessionsSync()
            val currentStreak = calculateStreak(sessions)
            if (currentStreak == 3 || currentStreak == 7 || currentStreak == 14 || currentStreak == 30) {
                NotificationHelper.showNotification(
                    getApplication(),
                    "Streak Milestone!",
                    "You studied $currentStreak days in a row!",
                    NotificationHelper.CHANNEL_STUDY_REMINDERS
                )
            }
        }
    }

    fun recordStudySessionWithOffset(durationMins: Int, daysOffset: Int) {
        viewModelScope.launch {
            val user = supabase.auth.currentUserOrNull()
            val userId = user?.id ?: ""
            val timestamp = System.currentTimeMillis() - daysOffset * 24 * 60 * 60 * 1000L
            val session = StudySession(
                timestamp = timestamp,
                durationMinutes = durationMins,
                userId = userId
            )
            studySessionRepository.insert(session)
            if (userId.isNotEmpty()) {
                try {
                    val isoTimestamp = java.time.Instant.ofEpochMilli(session.timestamp).toString()
                    supabase.from("sessions").insert(SupabaseSessionDto(session.id, isoTimestamp, session.durationMinutes, session.userId))
                } catch (e: Exception) {
                    // Ignore
                }
            }
            syncUserProfileToSupabase()
            checkAndTriggerAchievements()
        }
    }

    private fun isAfter10PM(timestamp: Long): Boolean {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        return hour >= 22
    }

    private fun isBefore8AM(timestamp: Long): Boolean {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        return hour < 8
    }

    fun checkAndTriggerAchievements() {
        viewModelScope.launch {
            try {
                val sessions = studySessionRepository.getAllSessionsSync()
                val currentStreak = calculateStreak(sessions)
                val currentXp = _userXP.value ?: 0
                val currentTasks = repository.allTasks.first()

                val achievementChecks = listOf(
                    Triple("first_session", "First Session") { sessions.isNotEmpty() },
                    Triple("streak_starter", "Streak Starter") { currentStreak >= 3 },
                    Triple("grinder", "Grinder") { sessions.size >= 10 },
                    Triple("night_owl", "Night Owl") { sessions.any { isAfter10PM(it.timestamp) } },
                    Triple("legend", "Legend") { currentXp >= 500 },
                    Triple("early_bird", "Early Bird") { sessions.any { isBefore8AM(it.timestamp) } },
                    Triple("focused", "Focused") { sessions.any { it.durationMinutes >= 50 } },
                    Triple("planner", "Planner") { currentTasks.size >= 5 },
                    Triple("consistent", "Consistent") { currentStreak >= 7 },
                    Triple("xp_hunter", "XP Hunter") { currentXp >= 200 }
                )

                for ((id, title, conditionCheck) in achievementChecks) {
                    if (conditionCheck()) {
                        val notifiedKey = "achievement_notified_$id"
                        val alreadyNotified = sharedPreferences.getBoolean(notifiedKey, false)
                        if (!alreadyNotified) {
                            sharedPreferences.edit().putBoolean(notifiedKey, true).apply()
                            NotificationHelper.showNotification(
                                getApplication(),
                                "Achievement Unlocked!",
                                "You earned $title!",
                                NotificationHelper.CHANNEL_ACHIEVEMENTS
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun deleteStudySession(session: StudySession) {
        viewModelScope.launch {
            studySessionRepository.deleteById(session.id)
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                try {
                    supabase.from("sessions").delete {
                        filter {
                            eq("id", session.id)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore error
                }
            }
            syncUserProfileToSupabase()
        }
    }

    private suspend fun syncUserProfileToSupabase() {
        try {
            val user = supabase.auth.currentUserOrNull() ?: return
            val sessions = studySessionRepository.getAllSessionsSync()
            val currentStreak = calculateStreak(sessions)
            val currentXp = maxOf(_userXP.value ?: 0, sharedPreferences.getInt("user_xp", 0))
            val commitmentDuration = sharedPreferences.getInt("study_commitment_duration", 1)
            val commitmentStartDate = sharedPreferences.getLong("study_commitment_start_date", System.currentTimeMillis())
            
            var currentName = sharedPreferences.getString("user_name", "") ?: ""
            if (currentName.isBlank()) {
                val metadataName = user.userMetadata?.get("full_name")?.jsonPrimitive?.contentOrNull
                if (!metadataName.isNullOrBlank()) {
                    currentName = metadataName
                    sharedPreferences.edit().putString("user_name", currentName).commit()
                    _userName.postValue(currentName)
                }
            }
            if (currentName.isBlank() && !user.email.isNullOrBlank()) {
                currentName = user.email!!.substringBefore("@")
                sharedPreferences.edit().putString("user_name", currentName).commit()
                _userName.postValue(currentName)
            }
            
            @kotlinx.serialization.Serializable
            data class UpsertUserDto(
                val id: String,
                val full_name: String,
                val streak: Int,
                val xp: Int
            )
            val profileToUpload = UpsertUserDto(
                id = user.id,
                full_name = currentName,
                streak = currentStreak,
                xp = currentXp
            )
            supabase.from("users").upsert(profileToUpload)
            
            fetchLeaderboard()
            fetchXpLeaderboard()
        } catch (e: Exception) {
            android.util.Log.e("MainViewModel", "Error syncing user profile to Supabase: ${e.message}", e)
        }
    }

    fun clearStudySessions() {
        viewModelScope.launch {
            studySessionRepository.clear()
        }
    }

    private fun calculateStreak(sessions: List<StudySession>): Int {
        if (sessions.isEmpty()) return 0

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getDefault()
        }
        
        val uniqueDays = sessions.map { sdf.format(java.util.Date(it.timestamp)) }
            .distinct()
            .sortedDescending()

        if (uniqueDays.isEmpty()) return 0

        val todayStr = sdf.format(java.util.Date())
        val yesterdayStr = sdf.format(java.util.Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L))

        val latestDay = uniqueDays.first()
        if (latestDay != todayStr && latestDay != yesterdayStr) {
            return 0
        }

        var streak = 0
        val calendar = java.util.Calendar.getInstance()
        val latestDate = sdf.parse(latestDay) ?: return 0
        calendar.time = latestDate

        for (day in uniqueDays) {
            val currentExpectedStr = sdf.format(calendar.time)
            if (day == currentExpectedStr) {
                streak++
                calendar.add(java.util.Calendar.DATE, -1)
            } else {
                break
            }
        }

        return streak
    }

    private fun calculateBestStreak(sessions: List<StudySession>): Int {
        if (sessions.isEmpty()) return 0

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getDefault()
        }
        
        val uniqueDays = sessions.map { sdf.format(java.util.Date(it.timestamp)) }
            .distinct()
            .sortedDescending()

        if (uniqueDays.isEmpty()) return 0

        val calendar = java.util.Calendar.getInstance()
        var maxStreak = 0
        var currentStreak = 0
        var expectedNextDayStr = ""

        for (day in uniqueDays) {
            if (currentStreak == 0) {
                currentStreak = 1
                maxStreak = maxOf(maxStreak, currentStreak)
                val date = sdf.parse(day)
                if (date != null) {
                    calendar.time = date
                    calendar.add(java.util.Calendar.DATE, -1)
                    expectedNextDayStr = sdf.format(calendar.time)
                }
            } else {
                if (day == expectedNextDayStr) {
                    currentStreak++
                    maxStreak = maxOf(maxStreak, currentStreak)
                    calendar.add(java.util.Calendar.DATE, -1)
                    expectedNextDayStr = sdf.format(calendar.time)
                } else {
                    currentStreak = 1
                    val date = sdf.parse(day)
                    if (date != null) {
                        calendar.time = date
                        calendar.add(java.util.Calendar.DATE, -1)
                        expectedNextDayStr = sdf.format(calendar.time)
                    }
                }
            }
        }
        return maxStreak
    }

    override fun onCleared() {
        super.onCleared()
        countdownTimer?.cancel()
    }
}
