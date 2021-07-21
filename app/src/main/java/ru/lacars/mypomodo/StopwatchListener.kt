package ru.lacars.mypomodo

import android.os.CountDownTimer

interface StopwatchListener {

    fun start(positionId: Int, currentMs: Long)

    fun stop(positionId: Int, currentMs: Long)

    fun delete(positionId: Int)

    fun toast(message: String)

    fun setTimer(timer: CountDownTimer, positionId: Int)
}