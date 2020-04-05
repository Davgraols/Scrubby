package com.hackthecrisis.scrubby

import android.content.Context
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit


class WashActivity : AppCompatActivity() {
    private val wifiWorkerRepeatInternal: Long = 30
    private val wifiWorkerFlexInterval: Long = 25
    private val wifiWorkerTag = "WifiWorker"
    private val workManager = WorkManager.getInstance(application)
    private lateinit var networkMonitor: NetworkMonitor
    private val timerPreset: Long = 20000
    private val handler = Handler()

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            startTimer()
        }

        PeriodicWorkRequest
            .Builder(WifiWorker::class.java, wifiWorkerRepeatInternal,
                TimeUnit.MINUTES, wifiWorkerFlexInterval, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .build())
            .build()
            .also {
                workManager.enqueueUniquePeriodicWork(wifiWorkerTag, ExistingPeriodicWorkPolicy.REPLACE, it)
            }
        networkMonitor = NetworkMonitor(this)
        networkMonitor.checkNetworkStatus()
        networkMonitor.listenForWifiChanges()
    }

    override fun onResume() {
        super.onResume()
        networkMonitor.checkNetworkStatus()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when(item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startTimer() {
        Log.d(TAG, "Starting timer")
        val countDownText = findViewById<TextView>(R.id.countDownTimer)
        val startButton = findViewById<FloatingActionButton>(R.id.fab)
        startButton.setImageResource(R.drawable.twotone_pan_tool_24)
        startButton.isClickable = false
        object : CountDownTimer(timerPreset, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countDownText.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                Log.d(TAG, "Timer finished")
                countDownText.text = getString(R.string.countdownFinishedText)
                vibrate()
                handler.postDelayed({resetTimer()}, 2000)
            }
        }.start()
    }

    private fun resetTimer() {
        val countDownText = findViewById<TextView>(R.id.countDownTimer)
        val startButton = findViewById<FloatingActionButton>(R.id.fab)
        countDownText.text = (timerPreset/1000).toString()
        startButton.isClickable = true
        startButton.setImageResource(android.R.drawable.ic_media_play)
    }

    private fun vibrate() {
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val amplitude = 150
        val vibrationLength: Long = 500
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(vibrationLength, amplitude))
        }
    }
}
