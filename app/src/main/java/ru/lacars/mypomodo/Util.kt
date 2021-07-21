package ru.lacars.mypomodo

// расширения для сервиса и холдера

fun Long.displayTime(): String {
    if (this <= 0L) {
        return "00:00:00"
    }
    val h = this / 1000 / 3600
    val m = this / 1000 % 3600 / 60
    val s = this / 1000 % 60

    return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}" //:${displaySlot(ms)}
}

fun displaySlot(count: Long): String {
    return if (count / 10L > 0) {
        "$count"
    } else {
        "0$count"
    }
}


const val START_TIME = "00:00:00:00"
const val INVALID = "INVALID"
const val COMMAND_START = "COMMAND_START"
const val COMMAND_STOP = "COMMAND_STOP"
const val COMMAND_ID = "COMMAND_ID"
const val LAST_TIME_MS = "LAST_TIME_MS"
const val STARTED_TIMER_TIME_MS = "STARTED_TIMER_TIME"

