package com.example

import kotlinx.coroutines.flow.Flow

class StudySessionRepository(private val studySessionDao: StudySessionDao) {
    val allSessions: Flow<List<StudySession>> = studySessionDao.getAllSessions()

    suspend fun insert(session: StudySession) {
        studySessionDao.insertSession(session)
    }

    suspend fun getAllSessionsSync(): List<StudySession> {
        return studySessionDao.getAllSessionsSync()
    }

    suspend fun deleteById(id: String) {
        studySessionDao.deleteById(id)
    }

    suspend fun clear() {
        studySessionDao.clearAll()
    }
}
