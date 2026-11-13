package com.webforge.eyetype.gaze

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.webforge.eyetype.MainActivity
import com.webforge.eyetype.R
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Camera tracking runs only after the user starts it from the visible app.
 * The ongoing notification provides an always-visible stop action.
 */
class GazeCameraService : LifecycleService() {
    private var cameraExecutor: ExecutorService? = null
    private var tracker: FaceGazeTracker? = null
    private var cameraProvider: ProcessCameraProvider? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == ACTION_STOP) {
            stopTracking()
            return START_NOT_STICKY
        }

        if (checkSelfPermission(android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildNotification())
        startCamera()
        return START_STICKY
    }

    private fun startCamera() {
        if (cameraExecutor != null) return

        cameraExecutor = Executors.newSingleThreadExecutor()
        try {
            tracker = FaceGazeTracker(applicationContext)
        } catch (_: Exception) {
            GazeState.publish(null)
            stopSelf()
            return
        }

        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                cameraProvider = provider
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetResolution(android.util.Size(640, 480))
                    .build()

                analysis.setAnalyzer(cameraExecutor!!) { image ->
                    analyzeFrame(image)
                }

                provider.unbindAll()
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, analysis)
            } catch (_: Exception) {
                GazeState.publish(null)
                stopSelf()
            }
        }, mainExecutor)
    }

    private fun analyzeFrame(image: ImageProxy) {
        try {
            val source = image.toBitmap()
            val upright = rotateBitmap(source, image.imageInfo.rotationDegrees)
            val reading = tracker?.estimate(upright, SystemClock.elapsedRealtime())
            if (reading == null) {
                GazeState.publish(null)
            } else {
                GazeState.publish(GazeState.Reading(reading.x, reading.y, reading.confidence))
            }
            if (upright !== source && !upright.isRecycled) upright.recycle()
            if (!source.isRecycled) source.recycle()
        } catch (_: Exception) {
            GazeState.publish(null)
        } finally {
            image.close()
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun buildNotification(): Notification {
        val channelId = "eyetype_gaze_tracking"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "EyeType gaze tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, GazeCameraService::class.java).setAction(ACTION_STOP)
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val openIntent = PendingIntent.getActivity(
            this, 2, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("EyeType tracking is active")
            .setContentText("Camera processing stays on this device")
            .setContentIntent(openIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent)
            .build()
    }

    private fun stopTracking() {
        GazeState.publish(null)
        cameraProvider?.unbindAll()
        cameraProvider = null
        tracker?.close()
        tracker = null
        cameraExecutor?.shutdown()
        cameraExecutor = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        GazeState.publish(null)
        cameraProvider?.unbindAll()
        tracker?.close()
        cameraExecutor?.shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = super.onBind(intent)

    companion object {
        const val ACTION_STOP = "com.webforge.eyetype.STOP_GAZE_TRACKING"
        private const val NOTIFICATION_ID = 7401
    }
}
