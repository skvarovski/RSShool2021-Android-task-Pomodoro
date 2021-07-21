package ru.lacars.mypomodo


interface StopwatchListener {

    fun start(id: Int, currentMs: Long)

    fun stop(id: Int, currentMs: Long)

    fun delete(id: Int)

    fun toast(message: String)

}