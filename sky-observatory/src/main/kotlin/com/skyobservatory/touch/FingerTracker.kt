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
 * Tracks finger positions and touch points for multi-touch input.
 */
class FingerTracker {

    companion object {
        private const val MAX_FINGERS = 10
    }

    private val fingers = Array(MAX_FINGERS) { Finger(-1, 0f, 0f) }
    private var activeFingerCount = 0

    private class Finger(var id: Int, var x: Float, var y: Float) {
        var active: Boolean = false
    }

    /**
     * Updates finger tracking state from a MotionEvent.
     */
    fun update(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                resetAllFingers()
                activeFingerCount = 1
                fingers[0].id = 0
                fingers[0].x = event.x
                fingers[0].y = event.y
                fingers[0].active = true
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)
                val pointerX = event.getX(pointerIndex)
                val pointerY = event.getY(pointerIndex)

                for (i in 0 until MAX_FINGERS) {
                    if (!fingers[i].active) {
                        fingers[i].id = pointerId
                        fingers[i].x = pointerX
                        fingers[i].y = pointerY
                        fingers[i].active = true
                        activeFingerCount++
                        break
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val pointerId = event.getPointerId(i)
                    for (j in 0 until MAX_FINGERS) {
                        if (fingers[j].active && fingers[j].id == pointerId) {
                            fingers[j].x = event.getX(i)
                            fingers[j].y = event.getY(i)
                            break
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                resetAllFingers()
                activeFingerCount = 0
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)

                for (i in 0 until MAX_FINGERS) {
                    if (fingers[i].active && fingers[i].id == pointerId) {
                        fingers[i].active = false
                        activeFingerCount--
                        break
                    }
                }
            }
        }
    }

    fun getActiveFingerCount(): Int = activeFingerCount

    fun hasSingleFinger(): Boolean = activeFingerCount == 1

    fun hasMultipleFingers(): Boolean = activeFingerCount >= 2

    fun getPrimaryFingerPosition(): Pair<Float, Float>? {
        for (i in 0 until MAX_FINGERS) {
            if (fingers[i].active) {
                return Pair(fingers[i].x, fingers[i].y)
            }
        }
        return null
    }

    fun getTwoFingerPositions(): Pair<Pair<Float, Float>, Pair<Float, Float>>? {
        if (activeFingerCount < 2) return null

        var first: Pair<Float, Float>? = null
        var second: Pair<Float, Float>? = null

        for (i in 0 until MAX_FINGERS) {
            if (fingers[i].active) {
                if (first == null) {
                    first = Pair(fingers[i].x, fingers[i].y)
                } else if (second == null) {
                    second = Pair(fingers[i].x, fingers[i].y)
                    break
                }
            }
        }

        return if (first != null && second != null) Pair(first, second) else null
    }

    fun getTwoFingerSpan(): Float? {
        val positions = getTwoFingerPositions() ?: return null
        val (x1, y1) = positions.first
        val (x2, y2) = positions.second

        val dx = x2 - x1
        val dy = y2 - y1
        return Math.sqrt(dx * dx + dy * dy).toFloat()
    }

    fun getTwoFingerCenter(): Pair<Float, Float>? {
        val positions = getTwoFingerPositions() ?: return null
        val (x1, y1) = positions.first
        val (x2, y2) = positions.second

        return Pair((x1 + x2) / 2f, (y1 + y2) / 2f)
    }

    fun getTwoFingerAngle(): Float? {
        val positions = getTwoFingerPositions() ?: return null
        val (x1, y1) = positions.first
        val (x2, y2) = positions.second

        val dx = x2 - x1
        val dy = y2 - y1
        return Math.atan2(dy.toDouble(), dx.toDouble()).toFloat()
    }

    fun getPrimaryFingerDelta(previousX: Float, previousY: Float): Pair<Float, Float>? {
        val current = getPrimaryFingerPosition() ?: return null
        return Pair(current.first - previousX, current.second - previousY)
    }

    private fun resetAllFingers() {
        for (i in 0 until MAX_FINGERS) {
            fingers[i].active = false
        }
    }
}
