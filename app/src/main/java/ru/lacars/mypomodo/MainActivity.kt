package ru.lacars.mypomodo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import ru.lacars.mypomodo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), StopwatchListener {

    private lateinit var binding: ActivityMainBinding

    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }

        binding.addNewStopwatchButton.setOnClickListener {
            val currentMs = binding.etAddTime.text.toString().toLong()*60*100
            val stopwatchItem = Stopwatch(nextId++, currentMs, currentMs, false)
            stopwatches.add(stopwatchItem)
            //Log.d("TEST","time add MS is = ${currentMs}")
            Log.d("TEST","add item = ${stopwatchItem}")
            stopwatchAdapter.submitList(stopwatches.toList())

        }
    }

    private fun getBackgroundColor(id: Int): Int {

        val typedValue = TypedValue()
        /*if (stopwatches[id].isFinished) {
            theme.resolveAttribute(R.attr.colorSecondaryVariant, typedValue,true)
        } else {
            //theme.resolveAttribute(R.attr.windowBackground,typedValue,true)
        }*/

        return typedValue.data

    }


    override fun start(id: Int, currentMs: Long) {
        changeStopwatch(id, currentMs, true)
    }

    override fun stop(id: Int, currentMs: Long) {
        changeStopwatch(id, currentMs, false)
    }



    override fun delete(id: Int) {
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
        stopwatchAdapter.notifyDataSetChanged()
    }

    override fun toast(message: String) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
        //Log.d("TEST","${stopwatches[positionId]}")

    }

    override fun setTimer(timer: CountDownTimer, positionId: Int) {
        TODO("Not yet implemented")
    }


    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean) {
        //val newTimers = mutableListOf<Stopwatch>()
        //перебор всех таймеров
        stopwatches.forEach {
            /*if (it.id == id) {
                newTimers.add(Stopwatch(it.id,it.positionId, it.startMs, currentMs ?: it.currentMs, isStarted ))
            } else {
                //остальные таймеры по умолчанию - остановленные.
                newTimers.add(Stopwatch(it.id, it.positionId, it.startMs, it.currentMs, false))
            }*/
        }

        stopwatches.forEach {
            if (it.id == id) {
                it.isStarted = isStarted
                it.currentMs = currentMs ?: it.currentMs
            } else {
                it.isStarted = false
            }
        }


        Log.d("TEST","Change watch = ${stopwatches.find { it.id == id }}")


        //stopwatches.clear()
        //stopwatches.addAll(newTimers)
        stopwatchAdapter.submitList(stopwatches.toList())
        stopwatchAdapter.notifyDataSetChanged()

    }

}