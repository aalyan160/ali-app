package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.showNotification(
            context,
            title = "Time to Study! \uD83D\uDD12",
            message = "Your goals are waiting. Stay Locked In!",
            channelId = NotificationHelper.CHANNEL_STUDY_REMINDERS,
            notificationId = 9999
        )
        
        // Reschedule for next day
        val hour = intent.getIntExtra("hour", -1)
        val minute = intent.getIntExtra("minute", -1)
        if (hour != -1 && minute != -1) {
            NotificationHelper.scheduleDailyReminder(context, hour, minute)
        }
    }
}
