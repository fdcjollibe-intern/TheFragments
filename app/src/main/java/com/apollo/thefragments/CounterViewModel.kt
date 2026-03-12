package com.apollo.thefragments

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel

class CounterViewModel : ViewModel() {

    var survivingCount: Int = 0

    var nonstopCount: Int = 0

    private val handler = Handler(Looper.getMainLooper())

    var onNonstopTick: ((Int) -> Unit)? = null

    private val nonstopTicker = object : Runnable {
        override fun run() {
            nonstopCount++
            onNonstopTick?.invoke(nonstopCount)
            handler.postDelayed(this, 1000)
        }
    }

    init {
        handler.post(nonstopTicker)
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(nonstopTicker)
    }
}