package ru.lacars.mypomodo

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*


class ForegroundService : Service() {

    private var isServiceStarted = false
    private var notificationManager: NotificationManager? = null
    private var job: Job? = null

    private val builder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pomodo")
            .setGroup("Pomodo")
            .setGroupSummary(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent())
            .setSilent(true)
            .setSmallIcon(R.drawable.ic_baseline_access_alarm_24)
    }

    override fun onCreate() {
        Log.d("TEST","Service onCreate")
        super.onCreate()
        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    @DelicateCoroutinesApi
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        processCommand(intent)
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @DelicateCoroutinesApi
    private fun processCommand(intent: Intent?) {
        when (intent?.extras?.getString(COMMAND_ID) ?: INVALID) {
            COMMAND_START -> {
                val endTime = intent?.extras?.getLong(STARTED_TIMER_TIME_MS) ?: return
                commandStart(endTime)
            }
            COMMAND_STOP -> commandStop()
            INVALID -> return
        }
    }

    @DelicateCoroutinesApi
    private fun commandStart(startTime: Long) {
        if (isServiceStarted) {
            return
        }
        Log.i("TAG", "commandStart()")
        try {
            moveToStartedState()
            startForegroundAndShowNotification()
            continueTimer(startTime)
        } finally {
            isServiceStarted = true
        }
    }

    @DelicateCoroutinesApi
    private fun continueTimer(endTime: Long) {
        job = GlobalScope.launch(Dispatchers.Main) {
            var howTikTakTimer = endTime - System.currentTimeMillis()
            updateStorage(howTikTakTimer)
            while (howTikTakTimer > 0L) {

                notificationManager?.notify(
                    NOTIFICATION_ID,
                    getNotification(
                        (howTikTakTimer).displayTime() //.dropLast(3)
                    )
                )
                delay(INTERVAL)
                updateStorage(howTikTakTimer)
                howTikTakTimer = endTime - System.currentTimeMillis()


            }
            // когда задача по отсчёту выполнена
            timerTikTakEnded()

        }
    }

    private fun updateStorage(leftTime: Long) {

        try {
            val sharedPreferences: SharedPreferences = getSharedPreferences(STORE_NAME, MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.putString(TIME_LEFT, leftTime.toString())
            editor.apply()
            Log.d("TEST","update SharePreference")
        } catch (e: Exception) {
            Log.d("TEST","Error update SharePreference")
        }

    }

    private fun timerTikTakEnded() {
        try {
            Toast.makeText(baseContext, "Timer finish", Toast.LENGTH_SHORT).show()

        } catch (e : Exception) {
            Log.d("TEST","Ошибка нотификации")
        }
    }


    private fun commandStop() {

        if (!isServiceStarted) {
            return
        }
        Log.i("TAG", "commandStop()")
        try {
            job?.cancel()
            stopForeground(true)
            stopSelf()
            Log.d("TEST","Service command stop")
        } finally {
            isServiceStarted = false
        }
    }

    private fun moveToStartedState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("TAG", "moveToStartedState(): Running on Android O or higher")
            startForegroundService(Intent(this, ForegroundService::class.java))
        } else {
            Log.d("TAG", "moveToStartedState(): Running on Android N or lower")
            startService(Intent(this, ForegroundService::class.java))
        }
    }

    private fun startForegroundAndShowNotification() {
        createChannel()
        val notification = getNotification("content")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun getNotification(content: String) = builder.setContentText(content).build()


    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "pomodoro"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(
                CHANNEL_ID, channelName, importance
            )
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getPendingIntent(): PendingIntent? {
        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.putExtra(LAST_TIME_MS,1100L)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(this, REQUEST_CODE_RESUME, resultIntent, PendingIntent.FLAG_ONE_SHOT)
    }

    private companion object {

        private const val CHANNEL_ID = "Channel_ID"
        private const val NOTIFICATION_ID = 777
        private const val INTERVAL = 1000L
    }
}