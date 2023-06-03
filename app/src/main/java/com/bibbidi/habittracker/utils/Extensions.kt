package com.bibbidi.habittracker.utils

import android.text.Editable
import android.view.View
import android.widget.EditText
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

fun Duration.toGoalTimeString(): String {
    val hour = toHoursPart()
    val minute = toMinutesPart()
    return if (hour == 0 && minute == 0) {
        "0s"
    } else if (hour == 0) {
        "${minute}s"
    } else if (minute == 0) {
        "${hour}h"
    } else {
        "${hour}h ${minute}s"
    }
}

fun Long.asLocalDate(): LocalDate {
    val instance = Instant.ofEpochMilli(this)
    return instance.atZone(ZoneId.systemDefault()).toLocalDate()
}

fun LocalDate.asLong(): Long {
    val dateTime = atStartOfDay()
    val zoneDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault())
    return zoneDateTime.toInstant().toEpochMilli()
}

fun String.toTwoDigits(): String = padStart(2, '0')

fun Int.toTwoDigits(): String = toString().toTwoDigits()

fun Editable.toTwoDigits(): String = toString().toTwoDigits()

fun EditText.toFixToTwoDigits() {
    onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
        if (!hasFocus) {
            val paddedText = text.toTwoDigits()
            setText(paddedText)
        }
    }
}
