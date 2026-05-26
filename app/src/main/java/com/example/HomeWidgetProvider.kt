package com.example

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val database = DatabaseProvider.getDatabase(context)
        val dao = database.studySessionDao()
        
        CoroutineScope(Dispatchers.IO).launch {
            val sessions: List<StudySession> = try {
                dao.getAllSessionsSync()
            } catch (e: Exception) {
                emptyList()
            }
            
            val streak = WidgetStatsHelper.calculateStreak(sessions)
            val todayMins = WidgetStatsHelper.calculateTodayMinutes(sessions)
            
            withContext(Dispatchers.Main) {
                appWidgetIds.forEach { appWidgetId ->
                    updateAppWidget(context, appWidgetManager, appWidgetId, streak, todayMins)
                }
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        streak: Int,
        todayMins: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_home)
        
        views.setTextViewText(R.id.widget_streak_text, "Streak: $streak days")
        views.setTextViewText(R.id.widget_today_text, "Today: $todayMins mins")

        // Set pending intent for "Start Session" button
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("START_STUDY", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_start_button, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
