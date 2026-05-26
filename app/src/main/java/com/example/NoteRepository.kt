package com.example

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    suspend fun insert(note: Note) {
        noteDao.insertNote(note)
    }

    suspend fun deleteById(id: String) {
        noteDao.deleteNoteById(id)
    }

    suspend fun clearNotes() {
        noteDao.clearNotes()
    }
}
