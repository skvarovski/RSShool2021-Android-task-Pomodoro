package ru.lacars.mypomodo

import android.os.CountDownTimer

interface StopwatchListener {

    fun start(id: Int, currentMs: Long)

    fun stop(id: Int, currentMs: Long)

    fun delete(id: Int)

    fun toast(message: String)

}