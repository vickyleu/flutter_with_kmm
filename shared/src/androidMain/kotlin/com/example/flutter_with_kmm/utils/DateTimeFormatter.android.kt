package com.example.flutter_with_kmm.utils

import android.os.Build
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

actual fun LocalDateTime.format(format: String): String? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        return this.toJavaLocalDateTime()
            .format(java.time.format.DateTimeFormatter.ofPattern(format))
    } else {
        // 获取kotlinx-datetime的年月日时分秒,兼容Android O以下版本
        val year = this.year
        val month = this.monthNumber
        val dayOfMonth = this.dayOfMonth
        val hour = this.hour
        val minute = this.minute
        val second = this.second
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth, hour, minute, second)
        return SimpleDateFormat(format, Locale.getDefault()).format(calendar.time)
    }
}

internal actual fun String.parse(time: String): LocalDateTime? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        return java.time.format.DateTimeFormatter.ofPattern(this).parse(time)
            ?.let {
                return java.time.LocalDateTime.from(it).toKotlinLocalDateTime()
            }
    } else {
        val date = SimpleDateFormat(this, Locale.getDefault()).parse(time)
        return date?.let {
            val calendar = Calendar.getInstance()
            calendar.time = it
            return LocalDateTime(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND)
            )
        }
    }
}
