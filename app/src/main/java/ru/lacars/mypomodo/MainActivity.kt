package ru.lacars.mypomodo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import android.widget.Toast
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import ru.lacars.mypomodo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), StopwatchListener, LifecycleObserver {

    private lateinit var binding: ActivityMainBinding

    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0
    private var startTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

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

        //тестовое время
        startTime = 6000L
        /*lifecycleScope.launch(Dispatchers.Main) {
            while (true) {
                binding.timerView.text = (System.currentTimeMillis() - startTime).displayTime()
                delay(INTERVAL)
            }
        }*/


    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        Log.d("TEST","LifeCycle on STOP EVENT")
        val stopwatchCurrentTimer = stopwatches.find { it.isStarted }
        if (stopwatchCurrentTimer != null && stopwatchCurrentTimer.currentMs > 0L) {
            stopwatchCurrentTimer.timer?.cancel()
            //stopwatchCurrentTimer.isStarted = false
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            // закидываем будущее время окончания (текущий остаток + текущее время)
            startIntent.putExtra(STARTED_TIMER_TIME_MS, stopwatchCurrentTimer.currentMs + System.currentTimeMillis())
            startService(startIntent)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    private companion object {

        private const val INTERVAL = 10L
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

    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean) {
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