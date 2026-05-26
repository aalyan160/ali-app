package com.example

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    suspend fun getAllSessionsSync(): List<StudySession>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession)

    @Query("DELETE FROM study_sessions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM study_sessions")
    suspend fun clearAll()
}
