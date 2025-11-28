package com.htetz.srpatchx.domain

import com.htetz.srpatchx.utils.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

suspend fun <T> CoroutineScope.runWithMinimumDelay(
    minimumDelayMs: Long = 1000,
    operation: suspend () -> T
): T {
    val startTime = System.currentTimeMillis()
    val result = operation()
    val elapsed = System.currentTimeMillis() - startTime
    val remainingDelay = minimumDelayMs - elapsed

    if (remainingDelay > 0) {
        delay(remainingDelay)
    }

    return result
}

fun Long.toReadableTime(): String {
    return DateUtils.formatRelativeDate(this)
}

fun Long.toReadableSize(): String {
    if (this <= 0) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB")
    val thresholds = longArrayOf(
        1,
        1_000,
        1_000_000,
        1_000_000_000,
        1_000_000_000_000,
        1_000_000_000_000_000,
        1_000_000_000_000_000_000
    )

    val digitGroups = (63 - this.countLeadingZeroBits()) / 10
    val index = digitGroups.coerceAtMost(units.lastIndex)

    return String.format(
        Locale.getDefault(),
        "%.2f %s",
        this / thresholds[index].toDouble(),
        units[index]
    )
}

fun File.isApkFile() = name.endsWith(".apk")