package com.example

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatMessageRepository(private val chatMessageDao: ChatMessageDao) {
    val lastMessages: Flow<List<ChatMessage>> = chatMessageDao.getLast20Messages()

    suspend fun insertMessage(message: ChatMessage) {
        chatMessageDao.insertMessage(message)
    }

    suspend fun clearMessages() {
        chatMessageDao.clearMessages()
    }
}
