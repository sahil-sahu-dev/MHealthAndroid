package com.example.voicerecorder
import android.os.Handler
import android.os.Looper
import java.time.Duration


interface OnTimerTickListener {
    fun onTimerClick(duration: String)
}

class Timer(listener: OnTimerTickListener) {
    private var handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    var duration = 0L
    private var delay = 100L

    init {
        runnable = Runnable {
            duration += delay
            handler.postDelayed(runnable, delay)
            listener.onTimerClick(duration.toString())

        }
    }

    fun start() {
        handler.postDelayed(runnable, delay)
    }

    fun pause() {
        handler.removeCallbacks(runnable)
    }

    fun stop() {
        handler.removeCallbacks(runnable)
        duration = 0L
    }

    fun formatTime(): String {
        val milli = duration % 1000
        val seconds = (duration / 1000) % 60
        val minutes = (duration / (1000 * 60)) % 60
        val hrs = (duration / (1000 * 60 * 60))

        val formatted: String = if(hrs > 0)
            "%02d:%02d:%02d:%02d".format(hrs, minutes, seconds, milli/10)
        else
            "%02d:%02d:%02d".format(minutes, seconds, milli/10)

        return formatted
    }

}