package com.apollo.thefragments

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel

// ViewModel is attached to the Activity's lifetime.
// It is NOT destroyed when you switch tabs — only when the Activity itself is finished.
class CounterViewModel : ViewModel() {

    // ─────────────────────────────────────────────────────────────
    // RIGHT COUNTER — survives tab switches, pauses when off-screen
    // ─────────────────────────────────────────────────────────────
    // This value lives here in the ViewModel.
    // The Fragment's ticker (in onStart/onStop) reads and writes this.
    // When the Fragment is destroyed, this value just sits here untouched.
    // When the Fragment comes back, it picks up from where it left off.
    var survivingCount: Int = 0

    // ─────────────────────────────────────────────────────────────
    // BOTTOM COUNTER — never stops, ticks even on other tabs
    // ─────────────────────────────────────────────────────────────
    // This counter's ticker lives HERE in the ViewModel, not in the Fragment.
    // So when the Fragment is destroyed on tab switch, the ticker keeps running.
    // The Fragment just reads nonstopCount to display it — it does not control it.
    var nonstopCount: Int = 0

    private val handler = Handler(Looper.getMainLooper())

    // Callback that the Fragment gives us so we can update the UI
    // even when the ticker fires while the Fragment is alive.
    var onNonstopTick: ((Int) -> Unit)? = null

    private val nonstopTicker = object : Runnable {
        override fun run() {
            nonstopCount++
            // If the Fragment is currently attached and visible, this pushes
            // the new value to the TextView. If Fragment is gone, onNonstopTick
            // is null so nothing crashes — the count just keeps incrementing silently.
            onNonstopTick?.invoke(nonstopCount)
            handler.postDelayed(this, 1000)
        }
    }

    // Called once when ViewModel is first created (first time you open the app).
    // Starts the nonstop ticker immediately — it never stops until the app closes.
    init {
        handler.post(nonstopTicker)
    }

    // Called by Android when the Activity is finished (app closed / back-pressed out).
    // This is the ONLY time the ViewModel is destroyed.
    // We clean up the handler here to avoid memory leaks.
    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(nonstopTicker)
    }
}