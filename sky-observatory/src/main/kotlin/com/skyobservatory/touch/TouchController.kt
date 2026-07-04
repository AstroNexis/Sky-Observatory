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
 *
 * Interaction model (matches Touch.zip / Sky Map / Star Walk reference):
 *   Single-finger drag:
 *     horizontal (X) -> azimuth   (consumeDeltaX, fed to camera.applyYawDelta)
 *     vertical   (Y) -> altitude  (consumeDeltaY, fed to camera.applyPitchDelta)
 *     Both axes use the same FOV-proportional scale so a diagonal drag feels
 *     exactly like turning a real head -- no asymmetric acceleration, no
 *     trackball rotation, roll stays 0 deg.
 *   Two-finger pinch:
 *     Changes FOV only (consumePinch -> camera.zoomBy).  No yaw, pitch or roll
 *     is touched; two-finger rotation is intentionally ignored to prevent drift.
 */
class TouchController {

    companion object {
        // Sensitivity constants.
        // pixelsToRadians reference from MapMover (Touch.zip):
        //   pixelsToRadians = fieldOfView / (screenHeight * RADIANS_TO_DEGREES)
        // We model this as a dimensionless scale factor applied per pixel.
        // DRAG_SCALE is tuned so that at FOV = 60 deg the feel matches the reference.
        // It is multiplied by the live (fovDeg / 60 deg) ratio at runtime so that
        // zooming in slows the drag proportionally (narrower window = finer control).
        private const val DRAG_SCALE = 0.15f          // degrees per pixel at FOV 60 deg
        private const val REFERENCE_FOV_DEG = 60f     // FOV at which DRAG_SCALE was tuned
        private const val PINCH_SENSITIVITY = 0.40f   // span-pixels -> FOV-change scale
    }

    // Component instances
    private val fingerTracker = FingerTracker()
    private val gestureRecognizer = GestureRecognizer(fingerTracker)
    private val touchStateManager = TouchStateManager()
    private val cameraInteractor = CameraInteractor()

    // Gesture-state machine (mirrors DragRotateZoomGestureDetector in the reference).
    private enum class State { READY, DRAGGING, DRAGGING2 }
    private var state = State.READY

    // Single-finger drag anchors
    private var last1X = 0f
    private var last1Y = 0f

    // Two-finger pinch anchors
    private var last2X = 0f
    private var last2Y = 0f

    // Accumulated camera deltas consumed by SkyRenderer on the GL thread.
    private var distanceX = 0f
    private var distanceY = 0f
    private var pinchDelta = 0f

    /**
     * Supplier for the camera's current field-of-view in degrees.
     * When set, drag sensitivity scales with FOV so zoomed-in drags feel slower.
     * Matches the reference: pixelsToRadians = fieldOfView / (height * rad_to_deg).
     * If null (default) the reference FOV (60 deg) is used -- sensitivity is constant.
     */
    var fovDegSupplier: (() -> Float)? = null

    /**
     * Handles a touch event and updates all components.
     *
     * State machine transitions mirror DragRotateZoomGestureDetector (Touch.zip):
     *   ACTION_DOWN        -> READY -> DRAGGING
     *   ACTION_MOVE (1f)   -> single-finger pan (azimuth + altitude)
     *   ACTION_POINTER_DOWN-> DRAGGING -> DRAGGING2
     *   ACTION_MOVE (2f)   -> pinch zoom (FOV only, no rotate/drift)
     *   ACTION_POINTER_UP  -> DRAGGING2 -> READY  (drop back to READY, not DRAGGING,
     *                        to avoid a position jump when re-anchoring to one finger)
     *   ACTION_UP/CANCEL   -> READY
     *
     * @param event The MotionEvent to process
     */
    fun onTouchEvent(event: MotionEvent) {
        // Note: fingerTracker.update() is called inside gestureRecognizer.processEvent()
        // below -- do NOT call it here again or finger state will be advanced twice per event.
        val action = event.action and MotionEvent.ACTION_MASK

        when {
            // -- Finger down ------------------------------------------------
            action == MotionEvent.ACTION_DOWN ||
            (action == MotionEvent.ACTION_DOWN && state == State.READY) -> {
                state = State.DRAGGING
                last1X = event.x
                last1Y = event.y
            }

            // -- Single-finger move -----------------------------------------
            action == MotionEvent.ACTION_MOVE && state == State.DRAGGING -> {
                val current1X = event.x
                val current1Y = event.y

                // FOV-proportional sensitivity: same formula as Touch.zip MapMover.
                //   pixelsToRadians = fov / (screenHeight * RADIANS_TO_DEGREES)
                // We express it as degrees-per-pixel (skip the /deg->rad conversion
                // because SkyCamera.applyYawDelta/applyPitchDelta accept degrees).
                val fov = fovDegSupplier?.invoke() ?: REFERENCE_FOV_DEG
                val scale = DRAG_SCALE * (fov / REFERENCE_FOV_DEG)

                // Both axes use the same scale -- identical to the reference --
                // so diagonal drags feel like turning a real head, not orbiting a globe.
                distanceX += (current1X - last1X) * scale
                distanceY += (current1Y - last1Y) * scale

                last1X = current1X
                last1Y = current1Y
            }

            // -- Two-finger move: pinch zoom only ---------------------------
            action == MotionEvent.ACTION_MOVE && state == State.DRAGGING2 -> {
                if (event.pointerCount == 2) {
                    val current1X = event.getX(0)
                    val current1Y = event.getY(0)
                    val current2X = event.getX(1)
                    val current2Y = event.getY(1)

                    // Span delta -> FOV change only.  No yaw, pitch or roll is
                    // touched here.  Two-finger rotation (angleDelta in the reference)
                    // is deliberately ignored: applying it to yaw would cause drift,
                    // and SkyCamera enforces up=(0,1,0) so roll is always 0 deg.
                    val prevSpan = sqrt(
                        (last1X - last2X) * (last1X - last2X) +
                        (last1Y - last2Y) * (last1Y - last2Y)
                    )
                    val curSpan = sqrt(
                        (current1X - current2X) * (current1X - current2X) +
                        (current1Y - current2Y) * (current1Y - current2Y)
                    )
                    if (prevSpan > 0f) {
                        pinchDelta += (curSpan - prevSpan) * PINCH_SENSITIVITY
                    }

                    last1X = current1X; last1Y = current1Y
                    last2X = current2X; last2Y = current2Y
                }
            }

            // -- Second finger down: switch to pinch mode -------------------
            action == MotionEvent.ACTION_POINTER_DOWN && state == State.DRAGGING -> {
                if (event.pointerCount == 2) {
                    state = State.DRAGGING2
                    last1X = event.getX(0); last1Y = event.getY(0)
                    last2X = event.getX(1); last2Y = event.getY(1)
                    // Discard any in-flight single-finger delta so it doesn't
                    // leak into the camera once the pinch gesture starts.
                    distanceX = 0f
                    distanceY = 0f
                }
            }

            // -- Finger lifted (pinch end): drop back to READY -------------
            // We go to READY rather than back to DRAGGING (unlike some implementations)
            // to avoid computing a position jump against the stale single-finger anchor.
            action == MotionEvent.ACTION_POINTER_UP && state == State.DRAGGING2 -> {
                state = State.READY
                pinchDelta = 0f
            }

            // -- All fingers up / cancelled ---------------------------------
            action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL -> {
                state = State.READY
                distanceX = 0f
                distanceY = 0f
                pinchDelta = 0f
            }
        }

        // Update modular components (for any code that reads them directly).
        val gestureData = gestureRecognizer.processEvent(event)
        touchStateManager.update(event, gestureData)
    }

    /**
     * Consumes and returns the accumulated X distance for camera azimuth (yaw).
     * Positive = look right.  Resets the accumulated value after consumption.
     */
    fun consumeDeltaX(): Float {
        val v = distanceX
        distanceX = 0f
        return v
    }

    /**
     * Consumes and returns the accumulated Y distance for camera altitude (pitch).
     * Positive = drag downward on screen (SkyRenderer negates this for pitch-up).
     * Resets the accumulated value after consumption.
     */
    fun consumeDeltaY(): Float {
        val v = distanceY
        distanceY = 0f
        return v
    }

    /**
     * Consumes and returns the accumulated pinch delta for camera FOV zoom.
     * Positive = fingers spreading apart (zoom out / wider FOV).
     * Resets the accumulated value after consumption.
     */
    fun consumePinch(): Float {
        val v = pinchDelta
        pinchDelta = 0f
        return v
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
        state = State.READY
        last1X = 0f; last1Y = 0f
        last2X = 0f; last2Y = 0f
        distanceX = 0f
        distanceY = 0f
        pinchDelta = 0f

        gestureRecognizer.reset()
        touchStateManager.reset()
        cameraInteractor.reset()
    }

    /** Returns true if currently touching */
    fun isTouching(): Boolean = state != State.READY
}
