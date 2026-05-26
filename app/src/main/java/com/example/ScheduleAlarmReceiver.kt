package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ScheduleAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskName = intent.getStringExtra("task_name") ?: "Task"
        val startTime = intent.getStringExtra("start_time") ?: "Now"
        
        NotificationHelper.showNotification(
            context,
            title = "Scheduled Task: $taskName",
            message = "Your task '$taskName' is scheduled to start at $startTime.",
            channelId = NotificationHelper.CHANNEL_STUDY_REMINDERS
        )
    }
}
