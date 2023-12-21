package com.example.flutter_with_kmm.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toNSDateComponents
import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

@Throws(IllegalStateException::class)
actual fun LocalDateTime.format(format: String): String? {
    val dateFormatter = NSDateFormatter()
    dateFormatter.dateFormat = format
    return dateFormatter.stringFromDate(
        toNSDate(NSCalendar.currentCalendar)
            ?: throw IllegalStateException("Could not convert kotlin date to NSDate $this")
    )
}

fun LocalDateTime.toNSDate(calendar: NSCalendar): NSDate? {
    val components = this.toNSDateComponents()
    return calendar.dateFromComponents(components)
}


internal actual fun String.parse(time: String): LocalDateTime? {
    val dateFormatter = NSDateFormatter()
    dateFormatter.dateFormat = this
    return dateFormatter.dateFromString(time)?.let {
        return it.toKotlinInstant().dateTime()
    }
}