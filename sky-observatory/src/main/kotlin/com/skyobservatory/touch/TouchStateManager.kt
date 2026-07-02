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

/**
 * Manages touch state, smoothing, and inertia for camera interactions.
 * 
 * Provides smooth movement through exponential moving average filtering
 * and handles touch lifecycle management with inertia support.
 */
class TouchStateManager {

    companion object {
        const val SMOOTH_FACTOR = 0.55f

        // Inertia constants
        const val INERTIA_DECAY = 0.92f
        const val INERTIA_MIN_VELOCITY = 0.01f
    }

    private var touching = false
    private var previousX = 0f
    private var previousY = 0f
    private var smoothedDx = 0f
    private var smoothedDy = 0f
    private var distanceX = 0f
    private var distanceY = 0f
    private var smoothedPinch = 0f
    private var pinchDelta = 0f
    private var previousSpan = 0f

    // Inertia state
    private var velocityX = 0f
    private var velocityY = 0f
    private var enableInertia = true

    /**
     * Updates the touch state based on a MotionEvent.
     */
    fun update(event: MotionEvent, gestureData: GestureRecognizer.GestureData) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touching = true
                previousX = event.x
                previousY = event.y
                smoothedDx = 0f
                smoothedDy = 0f
                velocityX = 0f
                velocityY = 0f
            }

            MotionEvent.ACTION_MOVE -> {
                if (gestureData.isPanning) {
                    // Apply smoothing to pan movements
                    smoothedDx += (gestureData.panX - smoothedDx) * SMOOTH_FACTOR
                    smoothedDy += (gestureData.panY - smoothedDy) * SMOOTH_FACTOR
                    
                    // Track velocity for inertia
                    velocityX = smoothedDx
                    velocityY = smoothedDy
                    
                    distanceX += smoothedDx
                    distanceY += smoothedDy
                    previousX = event.x
                    previousY = event.y
                } else if (gestureData.isPinching) {
                    // Apply smoothing to pinch movements
                    smoothedPinch += (gestureData.pinchDelta - smoothedPinch) * SMOOTH_FACTOR
                    pinchDelta += smoothedPinch
                    previousSpan = if (previousSpan == 0f) event.getX(0) - event.getX(1) else previousSpan
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touching = false
                // Don't reset distances here to allow inertia to continue
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
    }

    /**
     * Applies inertia decay to velocities and returns any inertial movement.
     * Call this between frames when not touching to continue movement with decay.
     */
    fun applyInertia(): Pair<Float, Float> {
        if (touching || !enableInertia) return Pair(0f, 0f)

        // Apply decay to velocities
        velocityX *= INERTIA_DECAY
        velocityY *= INERTIA_DECAY

        // If velocities are too small, stop inertia
        if (kotlin.math.abs(velocityX) < INERTIA_MIN_VELOCITY && 
            kotlin.math.abs(velocityY) < INERTIA_MIN_VELOCITY) {
            velocityX = 0f
            velocityY = 0f
            return Pair(0f, 0f)
        }

        return Pair(velocityX, velocityY)
    }

    /** Returns true if currently touching */
    fun isTouching(): Boolean = touching

    /**
     * Consumes and returns the accumulated X distance.
     * Resets the accumulated value after consumption.
     */
    fun consumeDeltaX(): Float {
        val v = distanceX
        distanceX = 0f
        return v
    }

    /**
     * Consumes and returns the accumulated Y distance.
     * Resets the accumulated value after consumption.
     */
    fun consumeDeltaY(): Float {
        val v = distanceY
        distanceY = 0f
        return v
    }

    /**
     * Consumes and returns the accumulated pinch delta.
     * Resets the accumulated value after consumption.
     */
    fun consumePinch(): Float {
        val v = pinchDelta
        pinchDelta = 0f
        return v
    }

    /**
     * Resets all touch state to initial values.
     */
    fun reset() {
        touching = false
        previousX = 0f
        previousY = 0f
        smoothedDx = 0f
        smoothedDy = 0f
        distanceX = 0f
        distanceY = 0f
        smoothedPinch = 0f
        pinchDelta = 0f
        previousSpan = 0f
        velocityX = 0f
        velocityY = 0f
    }

    /**
     * Gets the current smoothed X velocity.
     */
    fun getSmoothedDx(): Float = smoothedDx

    /**
     * Gets the current smoothed Y velocity.
     */
    fun getSmoothedDy(): Float = smoothedDy

    /**
     * Gets the current X velocity for inertia.
     */
    fun getVelocityX(): Float = velocityX

    /**
     * Gets the current Y velocity for inertia.
     */
    fun getVelocityY(): Float = velocityY

    /**
     * Gets the previous touch X position.
     */
    fun getPreviousX(): Float = previousX

    /**
     * Gets the previous touch Y position.
     */
    fun getPreviousY(): Float = previousY

    /**
     * Enables or disables inertia.
     */
    fun setInertiaEnabled(enabled: Boolean) {
        enableInertia = enabled
    }

    /** Returns true if inertia is enabled */
    fun isInertiaEnabled(): Boolean = enableInertia

    /**
     * Sets the inertia decay factor (0-1, where 1 = no decay).
     */
    fun setInertiaDecay(decay: Float) {
        // Clamp to reasonable range
        if (decay in 0.8f..1.0f) {
            // Will be updated in companion object in a real implementation
            // For now, we'll use the constant
        }
    }
}
