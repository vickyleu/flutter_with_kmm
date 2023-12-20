package com.example.flutter_with_kmm.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DateTimeFormatter(private val format: String) {
    fun parse(date: String): LocalDateTime? {
        return format.parse(date)
    }
    fun format(date: LocalDateTime): String? {
        return date.format(format)
    }
}

fun Instant.dateTime():LocalDateTime{
    return this.toLocalDateTime(TimeZone.currentSystemDefault())
}

internal expect fun String.parse(time: String): LocalDateTime?
expect fun LocalDateTime.format(format: String): String?