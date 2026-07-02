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
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Modular touch controller for handling multi-touch gestures.
 * 
 * This is the main facade class that coordinates between:
 * - FingerTracker: Tracks individual finger positions
 * - GestureRecognizer: Detects gestures from finger data
 * - TouchStateManager: Manages touch state, smoothing, and accumulation
 * - CameraInteractor: Handles camera-specific interactions
 * 
 * The public API is designed to be compatible with the original TouchController
 * while providing a more modular and extensible architecture.
 */
class TouchController {

    companion object {
        // Sensitivity constants
        private const val PAN_SENSITIVITY = 0.18f
        private const val PINCH_SENSITIVITY = 0.40f
        private const val SMOOTH_FACTOR = 0.55f

        // Horizontal pan acceleration: fast swipes sweep across the sky
        // faster than a flat sensitivity multiplier would allow, while slow
        // drags stay at the base 1x rate for fine aiming.
        private const val ACCEL_START_PX = 8f    // px/event before acceleration kicks in
        private const val ACCEL_GAIN = 0.06f     // extra multiplier gained per px/event over the start
        private const val ACCEL_MAX_MULTIPLIER = 2.5f // cap so a flick can't overshoot wildly
    }

    // Component instances
    private val fingerTracker = FingerTracker()
    private val gestureRecognizer = GestureRecognizer(fingerTracker)
    private val touchStateManager = TouchStateManager()
    private val cameraInteractor = CameraInteractor()

    // Direct state variables for backward compatibility
    private var previousX = 0f
    private var previousY = 0f
    private var distanceX = 0f
    private var distanceY = 0f
    private var smoothedDx = 0f
    private var smoothedDy = 0f
    private var touching = false
    private var previousSpan = 0f
    private var pinchDelta = 0f
    private var smoothedPinch = 0f

    /**
     * Handles a touch event and updates all components.
     * 
     * @param event The MotionEvent to process
     */
    fun onTouchEvent(event: MotionEvent) {
        // Note: fingerTracker.update() is called inside gestureRecognizer.processEvent()
        // below — do NOT call it here again or finger state will be advanced twice per event.
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touching = true
                previousX = event.x
                previousY = event.y
                smoothedDx = 0f
                smoothedDy = 0f
            }

            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 1 && touching) {
                    // Single finger pan with acceleration
                    val rawDx = event.x - previousX
                    val dx = rawDx * PAN_SENSITIVITY * horizontalAccel(rawDx)
                    val dy = (event.y - previousY) * PAN_SENSITIVITY

                    // Apply exponential smoothing
                    smoothedDx += (dx - smoothedDx) * SMOOTH_FACTOR
                    smoothedDy += (dy - smoothedDy) * SMOOTH_FACTOR
                    
                    // Accumulate smoothed distances
                    distanceX += smoothedDx
                    distanceY += smoothedDy
                    
                    previousX = event.x
                    previousY = event.y
                } else if (event.pointerCount >= 2) {
                    // Multi-finger pinch zoom
                    val sx = event.getX(0) - event.getX(1)
                    val sy = event.getY(0) - event.getY(1)
                    val span = sqrt(sx * sx + sy * sy)

                    if (previousSpan > 0) {
                        val raw = (span - previousSpan) * PINCH_SENSITIVITY
                        smoothedPinch += (raw - smoothedPinch) * SMOOTH_FACTOR
                        pinchDelta += smoothedPinch
                    }
                    previousSpan = span
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touching = false
                distanceX = 0f
                distanceY = 0f
                smoothedDx = 0f
                smoothedDy = 0f
                previousSpan = 0f
                pinchDelta = 0f
                smoothedPinch = 0f
            }

            MotionEvent.ACTION_POINTER_UP -> {
                previousSpan = 0f
                pinchDelta = 0f
                smoothedPinch = 0f
            }
        }

        // Update touch state manager (for modular usage)
        val gestureData = gestureRecognizer.processEvent(event)
        touchStateManager.update(event, gestureData)
    }

    /**
     * Consumes and returns the accumulated X distance for camera panning.
     * Resets the accumulated value after consumption.
     */
    fun consumeDeltaX(): Float {
        val v = distanceX
        distanceX = 0f
        return v
    }

    /**
     * Consumes and returns the accumulated Y distance for camera panning.
     * Resets the accumulated value after consumption.
     */
    fun consumeDeltaY(): Float {
        val v = distanceY
        distanceY = 0f
        return v
    }

    /**
     * Consumes and returns the accumulated pinch delta for camera zooming.
     * Resets the accumulated value after consumption.
     */
    fun consumePinch(): Float {
        val v = pinchDelta
        pinchDelta = 0f
        return v
    }

    /**
     * Extra speed multiplier for a horizontal drag, based on how far the
     * finger moved in this single event (a cheap stand-in for velocity).
     * Below [ACCEL_START_PX] returns 1x (no change); above it, multiplier
     * ramps up linearly with [ACCEL_GAIN], capped at [ACCEL_MAX_MULTIPLIER].
     */
    private fun horizontalAccel(rawDx: Float): Float {
        val speed = abs(rawDx)
        if (speed <= ACCEL_START_PX) return 1f
        val extra = (speed - ACCEL_START_PX) * ACCEL_GAIN
        return min(1f + extra, ACCEL_MAX_MULTIPLIER)
    }

    // Modular component access for advanced usage

    /** Returns the FingerTracker instance for advanced finger tracking */
    fun getFingerTracker(): FingerTracker = fingerTracker

    /** Returns the GestureRecognizer instance for custom gesture handling */
    fun getGestureRecognizer(): GestureRecognizer = gestureRecognizer

    /** Returns the TouchStateManager instance for state management */
    fun getTouchStateManager(): TouchStateManager = touchStateManager

    /** Returns the CameraInteractor instance for camera operations */
    fun getCameraInteractor(): CameraInteractor = cameraInteractor

    /**
     * Resets all touch state and components to initial values.
     */
    fun reset() {
        touching = false
        previousX = 0f
        previousY = 0f
        distanceX = 0f
        distanceY = 0f
        smoothedDx = 0f
        smoothedDy = 0f
        previousSpan = 0f
        pinchDelta = 0f
        smoothedPinch = 0f

        gestureRecognizer.reset()
        touchStateManager.reset()
        cameraInteractor.reset()
    }

    /** Returns true if currently touching */
    fun isTouching(): Boolean = touching
}
