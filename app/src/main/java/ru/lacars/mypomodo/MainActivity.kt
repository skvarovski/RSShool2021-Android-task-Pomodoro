package ru.lacars.mypomodo

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        stopwatchAdapter.setHasStableIds(true)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter

        }

        binding.addNewStopwatchButton.setOnClickListener {
            if (stopwatches.size < 8) {
                val currentMs = binding.etAddTime.text.toString().toLong()*60*1000
                val stopwatchItem = Stopwatch(nextId++, currentMs, currentMs, false)
                stopwatches.add(stopwatchItem)
                //Log.d("TEST","time add MS is = ${currentMs}")
                Log.d("TEST","add item = ${stopwatchItem}")
                stopwatchAdapter.submitList(stopwatches.toList())
            } else {
                toast("Limit of timers")
            }


        }

        //тестовое время
        //startTime = 6000L
        /*lifecycleScope.launch(Dispatchers.Main) {
            while (true) {
                binding.timerView.text = (System.currentTimeMillis() - startTime).displayTime()
                delay(INTERVAL)
            }
        }*/


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //if (requestCode == REQUEST_CODE_RESUME ) {
            Log.d("TEST","RESUME REQUEST CODE")
        //}
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        Log.d("TEST","LifeCycle on STOP EVENT")
        //если есть запущеный таймер и у него есть еще время
        val stopwatchCurrentTimer = stopwatches.find { it.isStarted }
        if (stopwatchCurrentTimer != null && stopwatchCurrentTimer.currentMs > 0L) {
            Log.d("TEST","StopWats = $stopwatchCurrentTimer")
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
        Log.d("TEST","LifeCycle on START EVENT")
        val stopwatchCurrentTimer = stopwatches.find { it.isStarted }
        Log.d("TEST","StopWats = $stopwatchCurrentTimer")
        //даём команду-интент остановки сервису
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)

        //необходимо получить оставшеся время и попытаться продолжить отсчёт
        loadLeftTimeFromStorageAndContinue()
    }

    private fun loadLeftTimeFromStorageAndContinue() {
        val sharedPreferences: SharedPreferences = getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
        val timeLeft: String? = sharedPreferences.getString(TIME_LEFT, null)
        if (timeLeft != null) {
            val stopwatchCurrentTimer = stopwatches.find { it.isStarted }
            stopwatchCurrentTimer?.currentMs = timeLeft.toLong()
            stopwatchAdapter.notifyDataSetChanged()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        // замечательный баг :
        /*  1) запустим таймер и сворачиваем приложение
            2) запускается фоновый сервис, убиваем свайпом приложение и оно за собой убивает сервис "команда стоп"
            3) открываем новое "второе" приложение и сворачиваем
            4) откуда-то появляется предыдущий таймер (из убитого "первого" приложения)
            5) фоновый сервис продолжает отсчитывать :)
            6) приходим к тому, что убивая всё - обнуляем список, на всякий случай

         */
        stopwatches.clear()
        // stop foreground service
        Log.d("TEST","Kill foreground service when kill app")
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)

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