package ru.lacars.mypomodo

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import ru.lacars.mypomodo.databinding.StopwatchItemBinding

class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener,
    private val resources: Resources
) : RecyclerView.ViewHolder(binding.root) {

    private var timer: CountDownTimer? = null

    fun bind(stopwatch: Stopwatch) {
        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
        binding.circleCustomView.setPeriod(stopwatch.startMs)
        binding.circleCustomView.setCurrent(stopwatch.currentMs)

        if (stopwatch.isStarted) {
            startTimer(stopwatch)
        } else {
            stopTimer(stopwatch)
        }

        initButtonsListeners(stopwatch)
    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {
        binding.startPauseButton.setOnClickListener {
            Log.d("TEST","$stopwatch")
            if (stopwatch.isStarted) {
                listener.stop(stopwatch.id, stopwatch.currentMs)
            } else {
                listener.start(stopwatch.id)
            }
        }

        binding.restartButton.setOnClickListener { listener.reset(stopwatch.id) }

        binding.deleteButton.setOnClickListener { listener.delete(stopwatch.id) }
    }

    private fun startTimer(stopwatch: Stopwatch) {
        //val drawable = resources.getDrawable(R.drawable.ic_baseline_pause_24)
        //binding.startPauseButton.setImageDrawable(drawable)
        binding.startPauseButton.text = "START"
        binding.circleCustomView.setCurrent(stopwatch.currentMs)
        binding.startPauseButton.isEnabled = true

        timer?.cancel()
        timer = getCountDownTimer(stopwatch)
        timer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer(stopwatch: Stopwatch) {
        //val drawable = resources.getDrawable(R.drawable.ic_baseline_play_arrow_24)
        //binding.startPauseButton.setImageDrawable(drawable)
        binding.startPauseButton.text = "STOP"
        binding.circleCustomView.setCurrent(stopwatch.currentMs)

        timer?.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }


    private fun endTimer(stopwatch: Stopwatch) {
        stopTimer(stopwatch)
        binding.startPauseButton.isEnabled = false
        binding.stopwatchLayout.background = resources.getDrawable(R.color.purple_200)

    }

    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        return object : CountDownTimer(stopwatch.startMs, UNIT_INTERVAL) {
            val interval = UNIT_INTERVAL

            override fun onTick(millisUntilFinished: Long) {
                stopwatch.currentMs -= interval
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                binding.circleCustomView.setCurrent(stopwatch.currentMs)

            }

            override fun onFinish() {
                stopwatch.currentMs = 0L
                endTimer(stopwatch)
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                Log.d("TEST","$stopwatch")
                //ContextCompat.getColor(resources.,R.color.purple_200)

            }
        }
    }

    private fun Long.displayTime(): String {
        if (this <= 0L ) {
            return END_TIME
        }

        val h = this / 1000 / 3600
        val m = this / 1000 % 3600 / 60
        val s = this / 1000 % 60
        //val ms = this % 1000 / 10

        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}" //:${displaySlot(ms)}
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {

        private const val UNIT_INTERVAL = 100L
        private const val END_TIME = "00:00:00"

        //private const val START_TIME = "00:00:00" //:00
        //private const val PERIOD = 1000L *  60L * 24L // Day
        //private const val UNIT_TEN_MS = 10L
        //private const val PERIOD stopwatch. //= 1000L *  60L * 24L // Day
    }
}