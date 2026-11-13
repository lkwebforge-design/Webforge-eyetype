package com.webforge.eyetype.gaze

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import kotlin.math.abs

/**
 * On-device face/iris landmark reader.
 *
 * The returned coordinates are raw eye-in-eye ratios, not screen coordinates.
 * They must pass through GazeCalibration before being used to select a key.
 */
class FaceGazeTracker(context: Context) : AutoCloseable {
    data class RawGaze(
        val x: Float,
        val y: Float,
        val confidence: Float
    )

    private val landmarker: FaceLandmarker

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("face_landmarker.task")
            .build()

        val options = FaceLandmarker.FaceLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.VIDEO)
            .setNumFaces(1)
            .setMinFaceDetectionConfidence(0.65f)
            .setMinFacePresenceConfidence(0.65f)
            .setMinTrackingConfidence(0.65f)
            .build()

        landmarker = FaceLandmarker.createFromOptions(context, options)
    }

    /** Call from a background camera-analysis thread with increasing timestamps. */
    fun estimate(bitmap: Bitmap, timestampMs: Long): RawGaze? {
        val image = BitmapImageBuilder(bitmap).build()
        val result = landmarker.detectForVideo(image, timestampMs)
        val face = result.faceLandmarks().firstOrNull() ?: return null
        if (face.size < 478) return null

        fun x(index: Int) = face[index].x()
        fun y(index: Int) = face[index].y()

        // Iris centres and eye corners/eyelids from the 478-point face mesh.
        val rightWidth = x(133) - x(33)
        val leftWidth = x(263) - x(362)
        if (abs(rightWidth) < 0.008f || abs(leftWidth) < 0.008f) return null

        val rightX = (x(468) - x(33)) / rightWidth
        val leftX = (x(473) - x(362)) / leftWidth

        val rightHeight = y(145) - y(159)
        val leftHeight = y(374) - y(386)
        if (abs(rightHeight) < 0.004f || abs(leftHeight) < 0.004f) return null

        val rightY = (y(468) - y(159)) / rightHeight
        val leftY = (y(473) - y(386)) / leftHeight

        val gazeX = (rightX + leftX) / 2f
        val gazeY = (rightY + leftY) / 2f
        if (!gazeX.isFinite() || !gazeY.isFinite()) return null

        // Reject grossly inconsistent/occluded eye readings rather than typing.
        val eyeDisagreement = abs(rightX - leftX) + abs(rightY - leftY)
        val inPlausibleRange = gazeX in -0.25f..1.25f && gazeY in -0.25f..1.25f
        if (!inPlausibleRange || eyeDisagreement > 0.65f) return null

        val lightingFactor = lightingConfidence(bitmap)
        val confidence = (1f - (eyeDisagreement / 0.65f)).coerceIn(0f, 0.85f) * lightingFactor
        return RawGaze(gazeX.coerceIn(0f, 1f), gazeY.coerceIn(0f, 1f), confidence)
    }

    /** Down-weight tracking in very dim or strongly overexposed frames. */
    private fun lightingConfidence(bitmap: Bitmap): Float {
        var total = 0.0
        var count = 0
        val stepX = (bitmap.width / 24).coerceAtLeast(1)
        val stepY = (bitmap.height / 18).coerceAtLeast(1)
        var y = 0
        while (y < bitmap.height) {
            var x = 0
            while (x < bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                val red = (pixel shr 16) and 0xFF
                val green = (pixel shr 8) and 0xFF
                val blue = pixel and 0xFF
                total += 0.2126 * red + 0.7152 * green + 0.0722 * blue
                count++
                x += stepX
            }
            y += stepY
        }
        val mean = if (count == 0) 0.0 else total / count
        return when {
            mean < 30.0 -> 0.30f
            mean < 65.0 -> (0.30 + (mean - 30.0) / 35.0 * 0.70).toFloat()
            mean > 245.0 -> 0.40f
            mean > 215.0 -> (1.0 - (mean - 215.0) / 30.0 * 0.60).toFloat()
            else -> 1.0f
        }.coerceIn(0.30f, 1.0f)
    }

    override fun close() {
        landmarker.close()
    }
}
