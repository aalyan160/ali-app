package com.example

import kotlinx.coroutines.flow.Flow

class ScheduleRepository(private val scheduleSlotDao: ScheduleSlotDao) {
    val allSlots: Flow<List<ScheduleSlot>> = scheduleSlotDao.getAllSlots()

    suspend fun insert(slot: ScheduleSlot) {
        scheduleSlotDao.insertSlot(slot)
    }

    suspend fun deleteById(id: String) {
        scheduleSlotDao.deleteSlotById(id)
    }

    suspend fun clear() {
        scheduleSlotDao.clearScheduleSlots()
    }
}
