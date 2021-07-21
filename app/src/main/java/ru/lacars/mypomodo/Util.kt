package ru.lacars.mypomodo

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

