package com.example

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleSlotDao {
    @Query("SELECT * FROM schedule_slots ORDER BY startTime ASC")
    fun getAllSlots(): Flow<List<ScheduleSlot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: ScheduleSlot)

    @Query("DELETE FROM schedule_slots WHERE id = :id")
    suspend fun deleteSlotById(id: String)

    @Query("DELETE FROM schedule_slots")
    suspend fun clearScheduleSlots()
}
