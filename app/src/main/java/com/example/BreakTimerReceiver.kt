package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BreakTimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences(
            "settings", Context.MODE_PRIVATE)
        val breakReminder = prefs.getBoolean(
            "break_reminder", true)
        if (!breakReminder) return

        NotificationHelper.showNotification(
            context,
            title = "Break Over! \uD83D\uDCAA",
            message = "Time to get back to work. Stay Locked In!",
            channelId = NotificationHelper.CHANNEL_BREAK_ALERTS,
            notificationId = 1001
        )
    }
}
