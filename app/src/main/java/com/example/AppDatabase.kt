package com.example

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Task::class, ScheduleSlot::class, Note::class, StudySession::class, ChatMessage::class], version = 10, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun scheduleSlotDao(): ScheduleSlotDao
    abstract fun noteDao(): NoteDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun chatMessageDao(): ChatMessageDao
}

object DatabaseProvider {
    @Volatile
    private var database: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
                override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS `chat_messages` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `text` TEXT NOT NULL, `isUser` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)")
                }
            }
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "lockedin_database"
            ).addMigrations(MIGRATION_9_10)
             .fallbackToDestructiveMigration(true).build()
            database = instance
            instance
        }
    }
}
