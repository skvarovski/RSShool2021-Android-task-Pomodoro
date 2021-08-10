package ru.lacars.mypomodo

import android.annotation.SuppressLint
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import android.util.Log
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import ru.lacars.mypomodo.databinding.StopwatchItemBinding
import androidx.appcompat.content.res.AppCompatResources.getDrawable

class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(stopwatch: Stopwatch) {
        //setIsRecyclable(false)

        stopwatch.positionId = adapterPosition

        //назначаем исходные данные
        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
        binding.circleCustomView.setPeriod(stopwatch.startMs)
        binding.circleCustomView.setCurrent(stopwatch.currentMs)
        // используем PositionID иначе сложно отловить
        //stopwatch.positionId = adapterPosition


        // из-за того, что переиспользовался одинаковый холдер необходим костыль :)
        // ну или ковыряемся в DiffUtill
        /*if (stopwatch.isFinished) {
            endTimer(stopwatch)
        } else {
            preStartTimer(stopwatch)
        }*/

        // что бы это срабатывало, нужно notifyDataChange или глубже оптимизировать
        if (stopwatch.isStarted) {
            startTimer(stopwatch)
        } else {
            stopTimer(stopwatch)
        }



        initButtonsListeners(stopwatch)
    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {
        if (stopwatch.isStarted) {
            with(binding) {
                startPauseButton.setOnClickListener{
                    stopwatch.timer?.cancel()
                    listener.stop(stopwatch.id, stopwatch.currentMs)
                }
            }

        } else {
            with(binding) {
                startPauseButton.setOnClickListener{
                    //если тайминг не закончился
                    if (stopwatch.currentMs != 0L) {
                        listener.start(stopwatch.id, stopwatch.currentMs)
                    } else {
                        listener.toast("Таймер закончился")
                    }
                }
            }
        }

        /*binding.startPauseButton.setOnClickListener {
            //setIsRecyclable(false)
            if (stopwatch.isStarted) {
                stopwatch.timer?.cancel()
                listener.stop(stopwatch.id, stopwatch.currentMs)

            } else {
                listener.start(stopwatch.id, stopwatch.currentMs)
                startTimer(stopwatch)

            }
            //Log.d("TEST","Button click start/stop = $stopwatch")
        }*/


        binding.deleteButton.setOnClickListener {
            //setIsRecyclable(false)
            stopwatch.isStarted = false
            (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
            stopwatch.timer?.cancel()
            stopwatch.timer = null
            listener.delete(stopwatch.id)
        }
    }


    //пред подготовка перед стартом, устанавливаем правильные данные
    private fun preStartTimer(stopwatch: Stopwatch) {
        binding.circleCustomView.setCurrent(stopwatch.currentMs)
        binding.startPauseButton.isEnabled = true
        binding.stopwatchLayout.background = null
        binding.startPauseButton.text = "START"
    }

    private fun startTimer(stopwatch: Stopwatch) {

        // пристёгиваем синглтон обьект к текущему обьекту
        stopwatch.timer?.cancel()
        stopwatch.timer = getCountDownTimer(stopwatch)
        stopwatch.timer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()

    }

    private fun stopTimer(stopwatch: Stopwatch) {

        stopwatch.timer?.cancel()
        //stopwatch.timer = null
        binding.circleCustomView.setCurrent(stopwatch.currentMs)
        stopwatch.isStarted = false
        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
        binding.startPauseButton.text = "START"




    }

    // когда таймер остановлен по времени
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun endTimer(stopwatch: Stopwatch) {
        stopTimer(stopwatch)
        stopwatch.timer?.cancel()
        //stopwatch.timer = null
        Log.d("TEST","End Timer")
        //stopwatch.isFinished = true
        binding.startPauseButton.isEnabled = false
        binding.blinkingIndicator.isInvisible = true
        //binding.stopwatchLayout.background = binding.root.context.resources.getDrawable(R.color.purple_200) //resources.getDrawable(R.color.purple_200)
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()

    }

    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        return object : CountDownTimer(stopwatch.currentMs, UNIT_INTERVAL) {

            override fun onTick(millisUntilFinished: Long) {
                //Log.d("TEST","$millisUntilFinished")
                stopwatch.currentMs = millisUntilFinished
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                binding.circleCustomView.setCurrent(stopwatch.currentMs)
                binding.startPauseButton.text = "STOP"

                //остановка таймера неестественным способом (уход ниже нуля)
                /*if (stopwatch.currentMs <= 0L) {
                    this.onFinish()
                    endTimer(stopwatch)
                }*/

            }
            //остановка таймера естественным способом.
            override fun onFinish() {
                stopwatch.currentMs = 0L
                binding.circleCustomView.setCurrent(stopwatch.currentMs)
                stopTimer(stopwatch) //endTimer(stopwatch)
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                Log.d("TEST","finish $stopwatch")

                //ContextCompat.getColor(resources.,R.color.purple_200)
                //binding.startPauseButton.isEnabled = false
                binding.blinkingIndicator.isInvisible = true
                //binding.stopwatchLayout.background = binding.root.context.resources.getDrawable(R.color.purple_200) //resources.getDrawable(R.color.purple_200)
                (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
                listener.toast("Timer finished")

            }
        }
    }



    private companion object {
        private const val UNIT_INTERVAL = 100L
        private const val END_TIME = "00:00:00"

    }
}