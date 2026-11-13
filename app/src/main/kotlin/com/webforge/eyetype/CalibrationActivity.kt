package com.webforge.eyetype

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.webforge.eyetype.gaze.GazeCalibration
import com.webforge.eyetype.gaze.GazeCalibrationStore
import com.webforge.eyetype.gaze.GazeState
import kotlin.math.min

/** Five-point on-device calibration. Camera readings stay local to this app. */
class CalibrationActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private val calibration = GazeCalibration()
    private val recentReadings = ArrayDeque<GazeState.Reading>()
    private lateinit var root: FrameLayout
    private lateinit var status: TextView
    private lateinit var target: TextView
    private var targetIndex = 0
    private var retryCount = 0

    private val targets = listOf(
        0.15f to 0.18f,
        0.85f to 0.18f,
        0.50f to 0.50f,
        0.15f to 0.82f,
        0.85f to 0.82f
    )

    private val gazeListener: (GazeState.Reading?) -> Unit = { reading ->
        if (reading != null && reading.confidence >= 0.60f) {
            synchronized(recentReadings) {
                recentReadings.addLast(reading)
                while (recentReadings.size > 40) recentReadings.removeFirst()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root = FrameLayout(this).apply {
            setBackgroundColor(0xFF0C1510.toInt())
        }
        val instructions = TextView(this).apply {
            text = "EyeType calibration\nLook at each green dot and keep your eyes there until it moves."
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(0xFFF4FFF7.toInt())
            setPadding(24, 24, 24, 24)
        }
        status = TextView(this).apply {
            text = "Waiting for eye tracking…"
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(0xFFB7C8BD.toInt())
            setPadding(20, 18, 20, 24)
        }
        target = TextView(this).apply {
            text = "●"
            textSize = 48f
            gravity = Gravity.CENTER
            setTextColor(0xFF72F2A0.toInt())
            visibility = View.INVISIBLE
        }

        root.addView(instructions, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP
        ))
        root.addView(status, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM
        ))
        root.addView(target, FrameLayout.LayoutParams(64, 64))
        setContentView(root)

        GazeState.subscribe(gazeListener)
        root.post { showTarget() }
    }

    private fun showTarget() {
        if (targetIndex >= targets.size) {
            finishCalibration()
            return
        }

        val (x, y) = targets[targetIndex]
        val params = target.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP or Gravity.LEFT
        params.leftMargin = (root.width * x - target.width / 2f).toInt()
        params.topMargin = (root.height * y - target.height / 2f).toInt()
        target.layoutParams = params
        target.visibility = View.VISIBLE

        synchronized(recentReadings) { recentReadings.clear() }
        status.text = "Point " + (targetIndex + 1) + " of " + targets.size + " · Hold your gaze"
        handler.postDelayed({ captureTarget(x, y) }, TARGET_HOLD_MS)
    }

    private fun captureTarget(targetX: Float, targetY: Float) {
        val readings = synchronized(recentReadings) { recentReadings.toList() }
        if (readings.size < MIN_READINGS) {
            retryCount++
            if (retryCount > MAX_RETRIES) {
                status.text = "I can't get a steady eye reading. Improve the lighting, hold the phone steady, and try again."
                target.visibility = View.INVISIBLE
                return
            }
            status.text = "Eyes not steady yet · hold the dot for a moment longer"
            handler.postDelayed({ captureTarget(targetX, targetY) }, RETRY_HOLD_MS)
            return
        }

        val stable = readings.takeLast(min(readings.size, 20))
        val rawX = stable.map { it.x }.average().toFloat()
        val rawY = stable.map { it.y }.average().toFloat()

        val location = IntArray(2)
        root.getLocationOnScreen(location)
        val metrics = resources.displayMetrics
        val screenX = (location[0] + root.width * targetX) / metrics.widthPixels.toFloat()
        val screenY = (location[1] + root.height * targetY) / metrics.heightPixels.toFloat()

        calibration.addSample(
            GazeCalibration.Sample(rawX, rawY, screenX.coerceIn(0f, 1f), screenY.coerceIn(0f, 1f))
        )
        targetIndex++
        retryCount = 0
        handler.postDelayed({ showTarget() }, 250)
    }

    private fun finishCalibration() {
        target.visibility = View.INVISIBLE
        if (calibration.fit() && calibration.isUsable) {
            GazeCalibrationStore.save(this, calibration)
            status.text = "Calibration saved on this phone. You can return to EyeType."
        } else {
            status.text = "Calibration wasn't accurate enough. Please try again in steady, even lighting."
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        GazeState.unsubscribe(gazeListener)
        super.onDestroy()
    }

    companion object {
        private const val TARGET_HOLD_MS = 1600L
        private const val RETRY_HOLD_MS = 700L
        private const val MIN_READINGS = 8
        private const val MAX_RETRIES = 5
    }
}
