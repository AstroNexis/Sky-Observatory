/*
 * Copyright 2026 Phuc An <phucan@tutamail.com>
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

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorController implements SensorEventListener {

    private static final String TAG = "SensorController";
    private static final float LOW_PASS = 0.55f;

    private final SensorManager sensorManager;
    private Sensor rotationVectorSensor;
    private Sensor accelerometer;
    private Sensor magneticField;

    private boolean usingRotationVector;
    private final float[] rotationMatrix = new float[9];
    private final float[] remappedMatrix = new float[9];

    private volatile float[] smoothedOrientation = new float[]{0, 0, -1, 0, 1, 0};
    private volatile boolean hasData;
    private final float[] lastLabelGravityUp = new float[3];
    private final float[] lastLabelForward = new float[3];

    private final float[] lastAccel = new float[3];
    private final float[] lastMag = new float[3];
    private boolean hasAccel, hasMag;

    public SensorController(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    public void start() {
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (rotationVectorSensor != null) {
            Log.d(TAG, "Using rotation vector sensor");
            usingRotationVector = true;
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME);
            return;
        }
        Log.d(TAG, "Rotation sensor not available, falling back to classic sensors");
        usingRotationVector = false;
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (accelerometer != null)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        if (magneticField != null)
            sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_GAME);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    public boolean hasOrientation() { return hasData; }

    public float getForwardX() { return smoothedOrientation[0]; }
    public float getForwardY() { return smoothedOrientation[1]; }
    public float getForwardZ() { return smoothedOrientation[2]; }
    public float getUpX() { return smoothedOrientation[3]; }
    public float getUpY() { return smoothedOrientation[4]; }
    public float getUpZ() { return smoothedOrientation[5]; }

    /**
     * Returns the rotation, in radians, to apply to screen-space labels so that
     * their text stays aligned with the device screen when the phone is rolled
     * in its own plane. The result snaps to one of four orientations (0, 90,
     * 180 or 270 degrees), so text is only ever horizontal or vertical. The
     * default orientation is used when looking nearly straight up or down,
     * where roll is undefined.
     */
    public float getLabelRollRadians() {
        float[] orientation = smoothedOrientation;
        return calculateLabelRollRadians(orientation, lastLabelGravityUp, lastLabelForward);
    }

    static float calculateLabelRollRadians(
            float[] orientation, float[] previousGravityUp, float[] previousForward) {
        float fx = orientation[0], fy = orientation[1], fz = orientation[2];
        float ux = orientation[3], uy = orientation[4], uz = orientation[5];

        // Gravity-up (world +Y) projected onto the plane perpendicular to forward.
        float gx = -fx * fy;
        float gy = 1f - fy * fy;
        float gz = -fz * fy;
        float gl = (float) Math.sqrt(gx * gx + gy * gy + gz * gz);
        if (gl < 0.05f) {
            previousForward[0] = fx;
            previousForward[1] = fy;
            previousForward[2] = fz;
            return 0f;
        }
        gx /= gl; gy /= gl; gz /= gl;

        float previousLength = (float) Math.sqrt(
                previousGravityUp[0] * previousGravityUp[0]
                        + previousGravityUp[1] * previousGravityUp[1]
                        + previousGravityUp[2] * previousGravityUp[2]);
        float alignment = gx * previousGravityUp[0]
                + gy * previousGravityUp[1]
                + gz * previousGravityUp[2];
        float forwardAlignment = fx * previousForward[0]
                + fy * previousForward[1]
                + fz * previousForward[2];
        if (previousLength > 0f && alignment < 0f && forwardAlignment > 0.9f) {
            gx = -gx; gy = -gy; gz = -gz;
        }
        previousGravityUp[0] = gx;
        previousGravityUp[1] = gy;
        previousGravityUp[2] = gz;
        previousForward[0] = fx;
        previousForward[1] = fy;
        previousForward[2] = fz;

        // Screen-right when upright: forward x gravity-up.
        float rx = fy * gz - fz * gy;
        float ry = fz * gx - fx * gz;
        float rz = fx * gy - fy * gx;

        float sinRoll = ux * rx + uy * ry + uz * rz;
        float cosRoll = ux * gx + uy * gy + uz * gz;
        float roll = (float) Math.atan2(sinRoll, cosRoll);

        // Snap to the nearest quarter turn so labels only read horizontally or
        // vertically. Text rotates opposite to the device's roll so it stays upright.
        return snapLabelRoll(roll);
    }

    static float snapLabelRoll(float roll) {
        final float quarterTurn = (float) (Math.PI / 2.0);
        float snapped = Math.copySign(
                Math.round(Math.abs(roll) / quarterTurn) * quarterTurn, roll);
        return -snapped;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (usingRotationVector) {
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                // Remap so that the device screen-face (+Z) becomes the camera forward axis.
                // AXIS_X keeps device right as right; AXIS_Z maps device +Z (screen out) to +Y
                // of the remapped frame, giving us the correct portrait-mode orientation.
                SensorManager.remapCoordinateSystem(rotationMatrix,
                        SensorManager.AXIS_X, SensorManager.AXIS_Z,
                        remappedMatrix);
                updateFromMatrix(remappedMatrix);
            }
        } else {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, lastAccel, 0, 3);
                hasAccel = true;
                tryComputeFromAccelMag();
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, lastMag, 0, 3);
                hasMag = true;
                tryComputeFromAccelMag();
            }
        }
    }

    private void tryComputeFromAccelMag() {
        if (!hasAccel || !hasMag) return;
        float[] R = new float[9];
        if (SensorManager.getRotationMatrix(R, null, lastAccel, lastMag)) {
            // Apply the same portrait remap for the accel/mag fallback path.
            SensorManager.remapCoordinateSystem(R,
                    SensorManager.AXIS_X, SensorManager.AXIS_Z,
                    remappedMatrix);
            updateFromMatrix(remappedMatrix);
        }
    }

    private void updateFromMatrix(float[] R) {
        float fxe =  R[3];
        float fyu =  R[5];
        float fzs = -R[4];

        float uxe =  R[6];
        float uyu =  R[8];
        float uzs = -R[7];

        float[] newOrientation = new float[6];

        if (!hasData) {
            newOrientation[0] = fxe; newOrientation[1] = fyu; newOrientation[2] = fzs;
            newOrientation[3] = uxe; newOrientation[4] = uyu; newOrientation[5] = uzs;
        } else {
            float[] orientation = smoothedOrientation;
            newOrientation[0] = orientation[0] + (fxe - orientation[0]) * LOW_PASS;
            newOrientation[1] = orientation[1] + (fyu - orientation[1]) * LOW_PASS;
            newOrientation[2] = orientation[2] + (fzs - orientation[2]) * LOW_PASS;
            newOrientation[3] = orientation[3] + (uxe - orientation[3]) * LOW_PASS;
            newOrientation[4] = orientation[4] + (uyu - orientation[4]) * LOW_PASS;
            newOrientation[5] = orientation[5] + (uzs - orientation[5]) * LOW_PASS;
            renormalize(newOrientation, 0);
            renormalize(newOrientation, 3);
        }

        smoothedOrientation = newOrientation;
        hasData         = true;
    }

    private static void renormalize(float[] v, int offset) {
        float l = (float) Math.sqrt(
                v[offset] * v[offset]
                        + v[offset + 1] * v[offset + 1]
                        + v[offset + 2] * v[offset + 2]);
        if (l > 1e-10f) {
            v[offset] /= l;
            v[offset + 1] /= l;
            v[offset + 2] /= l;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
