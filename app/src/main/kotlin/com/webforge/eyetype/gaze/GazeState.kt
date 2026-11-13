package com.webforge.eyetype.gaze

import java.util.concurrent.CopyOnWriteArraySet

/** In-process, camera-frame-only gaze updates shared by the tracker and IME. */
object GazeState {
    data class Reading(val x: Float, val y: Float, val confidence: Float)

    private val listeners = CopyOnWriteArraySet<(Reading?) -> Unit>()

    @Volatile
    var latest: Reading? = null
        private set

    fun subscribe(listener: (Reading?) -> Unit) {
        listeners.add(listener)
        listener(latest)
    }

    fun unsubscribe(listener: (Reading?) -> Unit) {
        listeners.remove(listener)
    }

    fun publish(reading: Reading?) {
        latest = reading
        listeners.forEach { it(reading) }
    }
}
