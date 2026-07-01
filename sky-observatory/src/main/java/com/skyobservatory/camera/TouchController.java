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

package com.skyobservatory.camera;

import android.view.MotionEvent;

public class TouchController {

    private float previousX, previousY;
    private float distanceX, distanceY;
    private boolean touching;
    private float previousSpan;
    private float pinchDelta;

    public void onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touching = true;
                previousX = event.getX();
                previousY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1 && touching) {
                    float dx = event.getX() - previousX;
                    float dy = event.getY() - previousY;
                    distanceX = dx * 0.2f;
                    distanceY = dy * 0.2f;
                    previousX = event.getX();
                    previousY = event.getY();
                } else if (event.getPointerCount() >= 2) {
                    float sx = event.getX(0) - event.getX(1);
                    float sy = event.getY(0) - event.getY(1);
                    float span = (float) Math.sqrt(sx * sx + sy * sy);
                    if (previousSpan > 0) {
                        pinchDelta = span - previousSpan;
                    }
                    previousSpan = span;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touching = false;
                distanceX = 0;
                distanceY = 0;
                previousSpan = 0;
                pinchDelta = 0;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                previousSpan = 0;
                pinchDelta = 0;
                break;
        }
    }

    public float consumeDeltaX() {
        float v = distanceX;
        distanceX = 0;
        return v;
    }

    public float consumeDeltaY() {
        float v = distanceY;
        distanceY = 0;
        return v;
    }

    public float consumePinch() {
        float v = pinchDelta;
        pinchDelta = 0;
        return v;
    }
}
