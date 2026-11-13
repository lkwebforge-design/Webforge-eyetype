package com.webforge.eyetype.gaze

import kotlin.math.sqrt

/**
 * Fits a two-dimensional affine calibration from raw eye-position samples to
 * normalized keyboard coordinates. Raw coordinates come from the camera gaze
 * estimator; target coordinates are the points the user was asked to look at.
 *
 * This class deliberately does not guess when calibration is weak: callers
 * should keep gaze selection disabled until [isUsable] is true.
 */
class GazeCalibration {
    data class Sample(
        val rawX: Float,
        val rawY: Float,
        val targetX: Float,
        val targetY: Float
    )

    data class Point(val x: Float, val y: Float)

    private val samples = mutableListOf<Sample>()
    private var xCoefficients: DoubleArray? = null
    private var yCoefficients: DoubleArray? = null

    var rmsError: Float = Float.POSITIVE_INFINITY
        private set

    val sampleCount: Int get() = samples.size
    fun samplesSnapshot(): List<Sample> = samples.toList()
    val isUsable: Boolean
        get() = samples.size >= REQUIRED_SAMPLES &&
            xCoefficients != null &&
            yCoefficients != null &&
            rmsError <= MAX_RMS_ERROR

    fun addSample(sample: Sample) {
        if (!sample.rawX.isFinite() || !sample.rawY.isFinite() ||
            !sample.targetX.isFinite() || !sample.targetY.isFinite()) return
        if (sample.rawX !in 0f..1f || sample.rawY !in 0f..1f ||
            sample.targetX !in 0f..1f || sample.targetY !in 0f..1f) return

        samples.add(sample)
        xCoefficients = null
        yCoefficients = null
        rmsError = Float.POSITIVE_INFINITY
    }

    fun clear() {
        samples.clear()
        xCoefficients = null
        yCoefficients = null
        rmsError = Float.POSITIVE_INFINITY
    }

    fun fit(): Boolean {
        if (samples.size < REQUIRED_SAMPLES) return false

        val normal = Array(3) { DoubleArray(3) }
        val rhsX = DoubleArray(3)
        val rhsY = DoubleArray(3)

        samples.forEach { s ->
            val row = doubleArrayOf(s.rawX.toDouble(), s.rawY.toDouble(), 1.0)
            for (i in 0..2) {
                rhsX[i] += row[i] * s.targetX
                rhsY[i] += row[i] * s.targetY
                for (j in 0..2) normal[i][j] += row[i] * row[j]
            }
        }

        val fittedX = solve3x3(normal, rhsX) ?: return false
        val fittedY = solve3x3(normal, rhsY) ?: return false

        var squaredError = 0.0
        samples.forEach { s ->
            val predictedX = fittedX[0] * s.rawX + fittedX[1] * s.rawY + fittedX[2]
            val predictedY = fittedY[0] * s.rawX + fittedY[1] * s.rawY + fittedY[2]
            val dx = predictedX - s.targetX
            val dy = predictedY - s.targetY
            squaredError += dx * dx + dy * dy
        }

        xCoefficients = fittedX
        yCoefficients = fittedY
        rmsError = sqrt(squaredError / (samples.size * 2)).toFloat()
        return isUsable
    }

    fun map(rawX: Float, rawY: Float): Point? {
        val xFit = xCoefficients ?: return null
        val yFit = yCoefficients ?: return null
        if (!isUsable || !rawX.isFinite() || !rawY.isFinite()) return null

        val mappedX = xFit[0] * rawX + xFit[1] * rawY + xFit[2]
        val mappedY = yFit[0] * rawX + yFit[1] * rawY + yFit[2]
        if (!mappedX.isFinite() || !mappedY.isFinite()) return null

        return Point(
            mappedX.coerceIn(0.0, 1.0).toFloat(),
            mappedY.coerceIn(0.0, 1.0).toFloat()
        )
    }

    private fun solve3x3(matrix: Array<DoubleArray>, values: DoubleArray): DoubleArray? {
        val a = Array(3) { row -> DoubleArray(4) { col ->
            if (col == 3) values[row] else matrix[row][col]
        } }

        for (pivot in 0..2) {
            var best = pivot
            for (row in pivot + 1..2) {
                if (kotlin.math.abs(a[row][pivot]) > kotlin.math.abs(a[best][pivot])) best = row
            }
            if (kotlin.math.abs(a[best][pivot]) < 1e-9) return null

            val temp = a[pivot]
            a[pivot] = a[best]
            a[best] = temp

            val divisor = a[pivot][pivot]
            for (col in pivot..3) a[pivot][col] /= divisor

            for (row in 0..2) {
                if (row == pivot) continue
                val factor = a[row][pivot]
                for (col in pivot..3) a[row][col] -= factor * a[pivot][col]
            }
        }
        return DoubleArray(3) { a[it][3] }
    }

    companion object {
        const val REQUIRED_SAMPLES = 5
        const val MAX_RMS_ERROR = 0.12f
    }
}
