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

import com.skyobservatory.camera.SkyCamera

/**
 * Handles camera-specific interaction logic for touch gestures.
 * 
 * Provides methods to apply touch gestures to camera transformations
 * including pan, rotate, and zoom operations.
 */
class CameraInteractor {

    /**
     * Interface for camera operations that can be performed based on touch gestures.
     */
    interface CameraOperations {
        fun applyYawDelta(degrees: Float)
        fun applyPitchDelta(degrees: Float)
        fun zoomBy(amount: Float)
        fun rotateYawPitch(yaw: Float, pitch: Float)
    }

    private var camera: CameraOperations? = null

    /**
     * Sets the camera to interact with.
     */
    fun setCamera(camera: CameraOperations) {
        this.camera = camera
    }

    /**
     * Sets a SkyCamera instance directly.
     */
    fun setSkyCamera(camera: SkyCamera) {
        this.camera = object : CameraOperations {
            override fun applyYawDelta(degrees: Float) {
                camera.applyYawDelta(degrees)
            }

            override fun applyPitchDelta(degrees: Float) {
                camera.applyPitchDelta(degrees)
            }

            override fun zoomBy(amount: Float) {
                camera.zoomBy(amount)
            }

            override fun rotateYawPitch(yaw: Float, pitch: Float) {
                camera.applyYawDelta(yaw)
                camera.applyPitchDelta(pitch)
            }
        }
    }

    /**
     * Applies pan gesture to the camera.
     * 
     * @param deltaX The horizontal movement delta
     * @param deltaY The vertical movement delta
     */
    fun applyPan(deltaX: Float, deltaY: Float) {
        camera?.applyYawDelta(deltaX)
        camera?.applyPitchDelta(-deltaY)
    }

    /**
     * Applies zoom gesture to the camera.
     * 
     * @param pinchDelta The pinch gesture delta (positive for zoom out, negative for zoom in)
     */
    fun applyZoom(pinchDelta: Float) {
        camera?.zoomBy(pinchDelta)
    }

    /**
     * Applies rotation gesture to the camera.
     * 
     * @param angleDelta The rotation angle delta in radians
     */
    fun applyRotation(angleDelta: Float) {
        // Convert rotation angle to yaw change
        // Positive angle = counter-clockwise rotation
        camera?.applyYawDelta(angleDelta * 180f / Math.PI.toFloat())
    }

    /**
     * Applies a combined pan and zoom gesture to the camera.
     * 
     * @param panX Horizontal pan delta
     * @param panY Vertical pan delta  
     * @param zoomDelta Zoom delta
     */
    fun applyPanAndZoom(panX: Float, panY: Float, zoomDelta: Float) {
        applyPan(panX, panY)
        applyZoom(zoomDelta)
    }

    /**
     * Applies a complete gesture to the camera.
     * 
     * @param gestureData The gesture data to apply
     */
    fun applyGesture(gestureData: GestureRecognizer.GestureData) {
        if (gestureData.isPanning) {
            applyPan(gestureData.panX, gestureData.panY)
        }
        if (gestureData.isPinching) {
            applyZoom(gestureData.pinchDelta)
        }
        if (gestureData.isRotating) {
            applyRotation(gestureData.rotationDelta)
        }
    }

    /**
     * Resets the camera interactor state.
     */
    fun reset() {
        camera = null
    }

    /**
     * Returns true if a camera is currently set.
     */
    fun hasCamera(): Boolean = camera != null

    /**
     * Returns the current camera operations instance.
     */
    fun getCamera(): CameraOperations? = camera
}
