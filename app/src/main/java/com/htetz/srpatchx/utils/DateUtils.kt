package com.htetz.srpatchx.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    // Cached formatters to avoid recreation
    private val dayFormatter by lazy { SimpleDateFormat("EEEE", Locale.getDefault()) }
    private val monthDayFormatter by lazy { SimpleDateFormat("MMM d", Locale.getDefault()) }
    private val fullDateFormatter by lazy { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    // Time constants for better readability
    private const val MINUTE_IN_MILLIS = 60_000L
    private const val HOUR_IN_MILLIS = 60 * MINUTE_IN_MILLIS
    private const val DAY_IN_MILLIS = 24 * HOUR_IN_MILLIS
    private const val WEEK_IN_MILLIS = 7 * DAY_IN_MILLIS

    fun formatRelativeDate(date: Long) = formatRelativeDate(Date(date))
    fun formatRelativeDate(date: Date): String {
        val now = System.currentTimeMillis()
        val fileTime = date.time
        val diffMillis = now - fileTime

        // Early return for recent times (most common case)
        return when {
            diffMillis < MINUTE_IN_MILLIS -> "Just now"
            diffMillis < HOUR_IN_MILLIS -> formatMinutesAgo(diffMillis)
            diffMillis < DAY_IN_MILLIS -> formatHoursAgo(diffMillis)
            else -> formatOlderDate(date, fileTime, diffMillis)
        }
    }

    private fun formatMinutesAgo(diffMillis: Long): String {
        val minutes = (diffMillis / MINUTE_IN_MILLIS).toInt()
        return "$minutes minute${if (minutes == 1) "" else "s"} ago"
    }

    private fun formatHoursAgo(diffMillis: Long): String {
        val hours = (diffMillis / HOUR_IN_MILLIS).toInt()
        return "$hours hour${if (hours == 1) "" else "s"} ago"
    }

    private fun formatOlderDate(date: Date, fileTime: Long, diffMillis: Long): String {
        val nowCal = Calendar.getInstance()
        val dateCal = Calendar.getInstance().apply { timeInMillis = fileTime }

        val nowYear = nowCal.get(Calendar.YEAR)
        val nowDay = nowCal.get(Calendar.DAY_OF_YEAR)
        val fileYear = dateCal.get(Calendar.YEAR)
        val fileDay = dateCal.get(Calendar.DAY_OF_YEAR)

        return when {
            // Yesterday (same year, day difference of 1)
            isYesterday(nowYear, fileYear, nowDay, fileDay) -> "Yesterday"

            // This week (same year and week)
            isThisWeek(nowCal, dateCal, nowYear, fileYear, diffMillis) ->
                dayFormatter.format(date)

            // This year
            nowYear == fileYear -> monthDayFormatter.format(date)

            // Previous years
            else -> fullDateFormatter.format(date)
        }
    }

    private fun isYesterday(nowYear: Int, fileYear: Int, nowDay: Int, fileDay: Int): Boolean =
        nowYear == fileYear && nowDay - fileDay == 1

    private fun isThisWeek(
        nowCal: Calendar,
        dateCal: Calendar,
        nowYear: Int,
        fileYear: Int,
        diffMillis: Long
    ): Boolean =
        nowYear == fileYear &&
                nowCal.get(Calendar.WEEK_OF_YEAR) == dateCal.get(Calendar.WEEK_OF_YEAR) &&
                diffMillis < WEEK_IN_MILLIS
}