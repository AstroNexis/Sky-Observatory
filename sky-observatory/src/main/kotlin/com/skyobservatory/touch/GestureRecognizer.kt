/*
 * Copyright 2026 Phuc An <pan2512811@gmail.com>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skyobservatory.touch

import android.view.MotionEvent
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min

/**
 * Recognizes touch gestures and provides gesture data for camera interaction.
 * 
 * Detects pan, rotate, and pinch gestures from finger tracking data.
 * Supports multi-touch scenarios with smooth gesture recognition.
 */
class GestureRecognizer(private val fingerTracker: FingerTracker) {

    companion object {
        const val PAN_SENSITIVITY = 0.18f
        const val PINCH_SENSITIVITY = 0.40f
        const val ROTATION_SENSITIVITY = 0.01f

        // Horizontal pan acceleration constants
        const val ACCEL_START_PX = 8f
        const val ACCEL_GAIN = 0.06f
        const val ACCEL_MAX_MULTIPLIER = 2.5f
    }

    private var previousSpan: Float = 0f
    private var previousAngle: Float? = null
    private var previousPrimaryX: Float = 0f
    private var previousPrimaryY: Float = 0f

    /**
     * Processes a touch event and recognizes gestures.
     * Returns gesture data based on the current touch state.
     */
    fun processEvent(event: MotionEvent): GestureData {
        fingerTracker.update(event)

        val gestureData = GestureData()

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // Store initial positions
                val primaryPos = fingerTracker.getPrimaryFingerPosition()
                if (primaryPos != null) {
                    previousPrimaryX = primaryPos.first
                    previousPrimaryY = primaryPos.second
                }
                previousSpan = 0f
                previousAngle = null
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                // Store initial multi-finger state
                val span = fingerTracker.getTwoFingerSpan()
                if (span != null) {
                    previousSpan = span
                }
                val angle = fingerTracker.getTwoFingerAngle()
                if (angle != null) {
                    previousAngle = angle
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (fingerTracker.hasSingleFinger()) {
                    // Single finger pan/drag
                    val primaryPos = fingerTracker.getPrimaryFingerPosition()
                    if (primaryPos != null) {
                        val currentX = primaryPos.first
                        val currentY = primaryPos.second

                        // Use historical data if available, otherwise use previous stored position
                        val prevX = if (event.historySize > 0 && event.actionIndex == 0) {
                            event.getHistoricalX(0, 0)
                        } else {
                            previousPrimaryX
                        }
                        val prevY = if (event.historySize > 0 && event.actionIndex == 0) {
                            event.getHistoricalY(0, 0)
                        } else {
                            previousPrimaryY
                        }

                        val rawDx = currentX - prevX
                        val rawDy = currentY - prevY

                        val dx = rawDx * PAN_SENSITIVITY * horizontalAccel(rawDx)
                        val dy = rawDy * PAN_SENSITIVITY

                        gestureData.panX = dx
                        gestureData.panY = dy
                        gestureData.isPanning = true

                        // Update previous positions
                        previousPrimaryX = currentX
                        previousPrimaryY = currentY
                    }
                } else if (fingerTracker.hasMultipleFingers()) {
                    // Multi-finger gestures
                    val span = fingerTracker.getTwoFingerSpan()
                    val center = fingerTracker.getTwoFingerCenter()
                    val angle = fingerTracker.getTwoFingerAngle()

                    if (span != null && center != null) {
                        // Pinch zoom - only if we have a previous span
                        if (previousSpan > 0) {
                            val rawDelta = (span - previousSpan) * PINCH_SENSITIVITY
                            gestureData.pinchDelta = rawDelta
                            gestureData.isPinching = abs(rawDelta) > 0.001f
                        }
                        previousSpan = span

                        // Rotation detection - only if we have a previous angle
                        if (angle != null && previousAngle != null) {
                            // Calculate delta angle, handling angle wrapping
                            val prevAngle = previousAngle!!  // Safe cast since we checked null
                            var deltaAngle = angle - prevAngle
                            val pi = PI.toFloat()
                            if (deltaAngle > pi) {
                                deltaAngle -= 2f * pi
                            } else if (deltaAngle < -pi) {
                                deltaAngle += 2f * pi
                            }

                            gestureData.rotationDelta = deltaAngle * ROTATION_SENSITIVITY
                            gestureData.isRotating = abs(deltaAngle) > 0.01f
                        }
                        previousAngle = angle ?: previousAngle
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // Reset gesture state
                previousSpan = 0f
                previousAngle = null
                previousPrimaryX = 0f
                previousPrimaryY = 0f
                gestureData.isPanning = false
                gestureData.isPinching = false
                gestureData.isRotating = false
            }

            MotionEvent.ACTION_POINTER_UP -> {
                // Reset multi-finger gesture state
                previousSpan = 0f
                previousAngle = null

                // If this leaves a single finger down, re-anchor the primary
                // finger reference to its current position (fingerTracker has
                // already dropped the lifted pointer above). Otherwise a
                // resumed single-finger drag would compute its first delta
                // against the stale pre-pinch position instead of where the
                // remaining finger actually is now.
                val primaryPos = fingerTracker.getPrimaryFingerPosition()
                if (primaryPos != null) {
                    previousPrimaryX = primaryPos.first
                    previousPrimaryY = primaryPos.second
                }
            }
        }

        return gestureData
    }

    /**
     * Calculates horizontal acceleration multiplier for fast drags.
     * Below ACCEL_START_PX returns 1x (no change); above it, multiplier
     * ramps up linearly with ACCEL_GAIN, capped at ACCEL_MAX_MULTIPLIER.
     */
    private fun horizontalAccel(rawDx: Float): Float {
        val speed = abs(rawDx)
        if (speed <= ACCEL_START_PX) return 1f
        val extra = (speed - ACCEL_START_PX) * ACCEL_GAIN
        return min(1f + extra, ACCEL_MAX_MULTIPLIER)
    }

    /**
     * Resets the gesture recognizer state.
     */
    fun reset() {
        previousSpan = 0f
        previousAngle = null
        previousPrimaryX = 0f
        previousPrimaryY = 0f
    }

    /**
     * Data class containing recognized gesture information.
     */
    data class GestureData(
        var panX: Float = 0f,
        var panY: Float = 0f,
        var pinchDelta: Float = 0f,
        var rotationDelta: Float = 0f,
        var rotationAngle: Float = 0f,
        var isPanning: Boolean = false,
        var isPinching: Boolean = false,
        var isRotating: Boolean = false
    )
}
