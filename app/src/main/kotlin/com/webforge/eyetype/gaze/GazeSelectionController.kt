package com.webforge.eyetype.gaze

/**
 * Converts stable normalized gaze coordinates into a single key selection.
 *
 * Camera / landmark code will call update() with coordinates in the 0..1 range.
 * Coordinates must be calibrated to the keyboard's visible bounds first.
 */
class GazeSelectionController(
    private val dwellMillis: Long = 500L,
    private val onSelect: (x: Float, y: Float) -> Unit
) {
    private var candidateX = -1f
    private var candidateY = -1f
    private var candidateSince = 0L
    private var hasSelected = false
    var isEnabled: Boolean = false

    fun update(x: Float, y: Float, confidence: Float, nowMillis: Long = System.currentTimeMillis()) {
        if (!isEnabled || confidence < MIN_CONFIDENCE || x !in 0f..1f || y !in 0f..1f) {
            reset()
            return
        }

        val sameTarget = candidateX >= 0f &&
            kotlin.math.abs(candidateX - x) < TARGET_TOLERANCE &&
            kotlin.math.abs(candidateY - y) < TARGET_TOLERANCE

        if (!sameTarget) {
            candidateX = x
            candidateY = y
            candidateSince = nowMillis
            hasSelected = false
            return
        }

        if (!hasSelected && nowMillis - candidateSince >= dwellMillis) {
            hasSelected = true
            onSelect(candidateX, candidateY)
        }
    }

    fun reset() {
        candidateX = -1f
        candidateY = -1f
        candidateSince = 0L
        hasSelected = false
    }

    companion object {
        private const val MIN_CONFIDENCE = 0.70f
        private const val TARGET_TOLERANCE = 0.035f
    }
}
