package ru.lacars.mypomodo

import android.os.CountDownTimer

data class Stopwatch(
    val id: Int,
    var startMs: Long,
    var currentMs: Long,
    var isStarted: Boolean,
    /*var isFinished: Boolean,*/
) {
    var timer: CountDownTimer? = null
}