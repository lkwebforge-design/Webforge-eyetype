package com.webforge.eyetype

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.webforge.eyetype.gaze.GazeCameraService
import com.webforge.eyetype.gaze.GazeState

class MainActivity : Activity() {
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 36, 28, 36)
            setBackgroundColor(0xFF101713.toInt())
        }

        val title = TextView(this).apply {
            text = "EyeType"
            textSize = 34f
            setTextColor(0xFFF4FFF7.toInt())
            gravity = Gravity.CENTER
        }
        val subtitle = TextView(this).apply {
            text = "Your keyboard, controlled by your gaze."
            textSize = 16f
            setTextColor(0xFFB7C8BD.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 28)
        }
        status = TextView(this).apply {
            text = "Ready. Enable the keyboard, then start on-device eye tracking."
            textSize = 14f
            setTextColor(0xFFE3EEE6.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }

        val enable = Button(this).apply {
            text = "1 · Enable EyeType Keyboard"
            setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        }
        val choose = Button(this).apply {
            text = "2 · Choose EyeType Keyboard"
            setOnClickListener {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            }
        }
        val startTracking = Button(this).apply {
            text = "3 · Start eye camera"
            setOnClickListener { requestCameraAndStart() }
        }
        val calibrate = Button(this).apply {
            text = "4 · Calibrate gaze"
            setOnClickListener { startActivity(Intent(this@MainActivity, CalibrationActivity::class.java)) }
        }
        val stopTracking = Button(this).apply {
            text = "Stop eye camera"
            setOnClickListener {
                stopService(Intent(this@MainActivity, GazeCameraService::class.java))
                status.text = "Eye tracking stopped."
            }
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(status)
        root.addView(enable)
        root.addView(choose)
        root.addView(startTracking)
        root.addView(calibrate)
        root.addView(stopTracking)
        setContentView(root)
    }

    private fun requestCameraAndStart() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startTrackingService()
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
        }
    }

    private fun startTrackingService() {
        val intent = Intent(this, GazeCameraService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
        else startService(intent)
        status.text = "Starting on-device face and iris tracking…"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                startTrackingService()
            } else {
                status.text = "Camera permission is needed for eye tracking. Keyboard touch input still works."
            }
        }
    }

    override fun onResume() {
        super.onResume()
        GazeState.subscribe(gazeListener)
    }

    override fun onPause() {
        GazeState.unsubscribe(gazeListener)
        super.onPause()
    }

    private val gazeListener: (GazeState.Reading?) -> Unit = { reading ->
        runOnUiThread {
            status.text = if (reading == null) {
                "Looking for your eyes… Keep your face visible and use even lighting. Gaze typing is not enabled until calibration is complete."
            } else {
                "Eyes detected · tracking confidence " + (reading.confidence * 100).toInt() + "% · calibration still required"
            }
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 7402
    }
}
