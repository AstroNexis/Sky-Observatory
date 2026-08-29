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
 * Touch Module - Touch handling system for multi-touch gestures.
 *
 * This package provides a touch handling system with the following components:
 *
 * 1. [FingerTracker] - Tracks individual finger positions and touch points
 *    - Manages finger lifecycle (down, move, up)
 *    - Handles multi-touch scenarios with up to 10 fingers
 *    - Provides utilities for accessing finger positions and spans
 *
 * 2. [TouchController] - Main touch controller class
 *    - Handles single-finger pan and two-finger pinch gestures
 *    - Accumulates deltas consumed by the renderer on the GL thread
 *    - FOV-proportional drag sensitivity for consistent feel
 *
 * Usage Example:
 * ```kotlin
 * val touchController = TouchController()
 * touchController.onTouchEvent(event)
 * val deltaX = touchController.consumeDeltaX()
 * val deltaY = touchController.consumeDeltaY()
 * val pinch = touchController.consumePinch()
 * ```
 */
package com.skyobservatory.touch