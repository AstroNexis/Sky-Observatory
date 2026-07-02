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

class TouchController {

    companion object {
        private const val PAN_SENSITIVITY   = 0.18f
        private const val PINCH_SENSITIVITY = 0.40f
        private const val SMOOTH_FACTOR     = 0.55f
    }

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

    fun onTouchEvent(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touching  = true
                previousX = event.x
                previousY = event.y
                smoothedDx = 0f
                smoothedDy = 0f
            }

            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 1 && touching) {
                    val dx = (event.x - previousX) * PAN_SENSITIVITY
                    val dy = (event.y - previousY) * PAN_SENSITIVITY
                    smoothedDx += (dx - smoothedDx) * SMOOTH_FACTOR
                    smoothedDy += (dy - smoothedDy) * SMOOTH_FACTOR
                    distanceX += smoothedDx
                    distanceY += smoothedDy
                    previousX  = event.x
                    previousY  = event.y
                } else if (event.pointerCount >= 2) {
                    val sx   = event.getX(0) - event.getX(1)
                    val sy   = event.getY(0) - event.getY(1)
                    val span = kotlin.math.sqrt(sx * sx + sy * sy)
                    if (previousSpan > 0) {
                        val raw = (span - previousSpan) * PINCH_SENSITIVITY
                        smoothedPinch += (raw - smoothedPinch) * SMOOTH_FACTOR
                        pinchDelta    += smoothedPinch
                    }
                    previousSpan = span
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touching      = false
                distanceX     = 0f
                distanceY     = 0f
                smoothedDx    = 0f
                smoothedDy    = 0f
                previousSpan  = 0f
                pinchDelta    = 0f
                smoothedPinch = 0f
            }

            MotionEvent.ACTION_POINTER_UP -> {
                previousSpan  = 0f
                pinchDelta    = 0f
                smoothedPinch = 0f
            }
        }
    }

    fun consumeDeltaX(): Float {
        val v = distanceX
        distanceX = 0f
        return v
    }

    fun consumeDeltaY(): Float {
        val v = distanceY
        distanceY = 0f
        return v
    }

    fun consumePinch(): Float {
        val v = pinchDelta
        pinchDelta = 0f
        return v
    }
}
