package com.webforge.eyetype.gaze

import android.content.Context

/** Stores calibration samples locally on the device; no gaze data is uploaded. */
object GazeCalibrationStore {
    private const val PREFS = "eyetype_gaze_calibration"
    private const val KEY_SAMPLES = "samples"

    fun save(context: Context, calibration: GazeCalibration) {
        val encoded = calibration.samplesSnapshot().joinToString(";") { sample ->
            listOf(sample.rawX, sample.rawY, sample.targetX, sample.targetY)
                .joinToString(",") { it.toString() }
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SAMPLES, encoded)
            .apply()
    }

    fun load(context: Context): GazeCalibration? {
        val encoded = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_SAMPLES, null) ?: return null
        val calibration = GazeCalibration()
        encoded.split(";").forEach { row ->
            val values = row.split(",").mapNotNull { it.toFloatOrNull() }
            if (values.size == 4) {
                calibration.addSample(
                    GazeCalibration.Sample(values[0], values[1], values[2], values[3])
                )
            }
        }
        return if (calibration.fit() && calibration.isUsable) calibration else null
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_SAMPLES)
            .apply()
    }
}
