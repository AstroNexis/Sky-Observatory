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

/**
 * Touch Module - Modular Touch System Architecture
 * 
 * This package provides a modular touch handling system for multi-touch gestures
 * with the following components:
 * 
 * 1. [FingerTracker] - Tracks individual finger positions and touch points
 *    - Manages finger lifecycle (down, move, up)
 *    - Handles multi-touch scenarios with up to 10 fingers
 *    - Provides utilities for accessing finger positions and spans
 * 
 * 2. [GestureRecognizer] - Detects gestures from finger tracking data
 *    - Recognizes pan, pinch, and rotation gestures
 *    - Applies sensitivity and acceleration factors
 *    - Handles multi-touch gesture combinations
 *    - Provides smoothed gesture data for camera interaction
 * 
 * 3. [TouchStateManager] - Manages touch state, smoothing, and inertia
 *    - Applies exponential moving average smoothing
 *    - Tracks velocity for inertia support
 *    - Manages touch lifecycle and state transitions
 *    - Provides consume methods for gesture deltas
 * 
 * 4. [CameraInteractor] - Handles camera-specific interaction logic
 *    - Applies gestures to camera transformations
 *    - Supports pan, zoom, and rotation operations
 *    - Provides interface for custom camera implementations
 * 
 * 5. [TouchController] - Main facade class
 *    - Coordinates all components
 *    - Maintains backward compatibility with original API
 *    - Provides access to individual components for advanced usage
 * 
 * Architecture Benefits:
 * - Separation of concerns: Each component has a single responsibility
 * - Testability: Components can be tested independently
 * - Extensibility: New gesture types can be added without modifying core logic
 * - Maintainability: Clear component boundaries and dependencies
 * - Backward compatibility: Existing code continues to work unchanged
 * 
 * Usage Example:
 * ```kotlin
 * // Basic usage (same as original)
 * val touchController = TouchController()
 * touchController.onTouchEvent(event)
 * val deltaX = touchController.consumeDeltaX()
 * val deltaY = touchController.consumeDeltaY()
 * val pinch = touchController.consumePinch()
 * 
 * // Advanced usage with modular components
 * val fingerTracker = touchController.getFingerTracker()
 * val gestureRecognizer = touchController.getGestureRecognizer()
 * val touchStateManager = touchController.getTouchStateManager()
 * val cameraInteractor = touchController.getCameraInteractor()
 * ```
 */
package com.skyobservatory.touch