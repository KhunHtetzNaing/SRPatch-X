package com.htetz.srpatchx.domain

import android.content.Context
import androidx.annotation.StringRes
import com.htetz.srpatchx.R
import com.htetz.srpatchx.utils.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import java.io.File
suspend fun <T> CoroutineScope.runWithMinimumDelay(
    minimumDelayMs: Long = 1000,
    operation: suspend () -> T,
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

fun Long.toReadableTime(context: Context): String {
    return with(DateUtils) { context.formatRelativeDate(this@toReadableTime) }
}

fun Context.formatSize(bytes: Long): String {
    if (bytes <= 0) return getString(R.string.size_zero)

    val units = intArrayOf(
        R.string.unit_byte,
        R.string.unit_kb,
        R.string.unit_mb,
        R.string.unit_gb,
        R.string.unit_tb,
        R.string.unit_pb,
        R.string.unit_eb,
    )
    val thresholds = longArrayOf(
        1,
        1_000,
        1_000_000,
        1_000_000_000,
        1_000_000_000_000,
        1_000_000_000_000_000,
        1_000_000_000_000_000_000,
    )

    val digitGroups = (63 - bytes.countLeadingZeroBits()) / 10
    val index = digitGroups.coerceAtMost(units.lastIndex)

    @StringRes val unitRes = units[index]
    return getString(R.string.size_format, bytes / thresholds[index].toDouble(), getString(unitRes))
}

fun Long.toReadableSize(context: Context): String = context.formatSize(this)

fun File.isApkFile() = name.endsWith(".apk")
