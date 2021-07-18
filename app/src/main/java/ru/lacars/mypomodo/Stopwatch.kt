package ru.lacars.mypomodo

data class Stopwatch(
    val id: Int,
    var startMs: Long,
    var currentMs: Long,
    var isStarted: Boolean
)