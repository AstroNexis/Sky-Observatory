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

    private volatile float[] smoothedForward = new float[]{0, 0, -1};
    private volatile float[] smoothedUp = new float[]{0, 1, 0};
    private volatile boolean hasData;

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

    public float getForwardX() { return smoothedForward[0]; }
    public float getForwardY() { return smoothedForward[1]; }
    public float getForwardZ() { return smoothedForward[2]; }
    public float getUpX() { return smoothedUp[0]; }
    public float getUpY() { return smoothedUp[1]; }
    public float getUpZ() { return smoothedUp[2]; }

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

        float[] newForward;
        float[] newUp;

        if (!hasData) {
            newForward = new float[]{fxe, fyu, fzs};
            newUp      = new float[]{uxe, uyu, uzs};
        } else {
            float[] f = smoothedForward;
            float[] u = smoothedUp;
            newForward = new float[]{
                f[0] + (fxe - f[0]) * LOW_PASS,
                f[1] + (fyu - f[1]) * LOW_PASS,
                f[2] + (fzs - f[2]) * LOW_PASS
            };
            newUp = new float[]{
                u[0] + (uxe - u[0]) * LOW_PASS,
                u[1] + (uyu - u[1]) * LOW_PASS,
                u[2] + (uzs - u[2]) * LOW_PASS
            };
            renormalize(newForward);
            renormalize(newUp);
        }

        smoothedForward = newForward;
        smoothedUp      = newUp;
        hasData         = true;
    }

    private static void renormalize(float[] v) {
        float l = (float) Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
        if (l > 1e-10f) { v[0] /= l; v[1] /= l; v[2] /= l; }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
