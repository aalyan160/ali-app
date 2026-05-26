package com.example

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object WidgetStatsHelper {
    fun calculateStreak(sessions: List<StudySession>): Int {
        if (sessions.isEmpty()) return 0

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        
        val uniqueDays = sessions.map { sdf.format(Date(it.timestamp)) }
            .distinct()
            .sortedDescending()

        if (uniqueDays.isEmpty()) return 0

        val todayStr = sdf.format(Date())
        val yesterdayStr = sdf.format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L))

        val latestDay = uniqueDays.first()
        if (latestDay != todayStr && latestDay != yesterdayStr) {
            return 0
        }

        var streak = 0
        val calendar = Calendar.getInstance()
        val latestDate = sdf.parse(latestDay) ?: return 0
        calendar.time = latestDate

        for (day in uniqueDays) {
            val currentExpectedStr = sdf.format(calendar.time)
            if (day == currentExpectedStr) {
                streak++
                calendar.add(Calendar.DATE, -1)
            } else {
                break
            }
        }

        return streak
    }

    fun calculateTodayMinutes(sessions: List<StudySession>): Int {
        val todayCal = Calendar.getInstance()
        todayCal.set(Calendar.HOUR_OF_DAY, 0)
        todayCal.set(Calendar.MINUTE, 0)
        todayCal.set(Calendar.SECOND, 0)
        todayCal.set(Calendar.MILLISECOND, 0)
        val startOfToday = todayCal.timeInMillis
        return sessions.filter { it.timestamp >= startOfToday }.sumOf { it.durationMinutes }
    }
}
