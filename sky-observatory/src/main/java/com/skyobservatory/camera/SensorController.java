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
import android.view.Display;
import android.view.Surface;

public class SensorController implements SensorEventListener {

    private static final String TAG = "SensorController";
    private static final float LOW_PASS = 0.55f;

    private final SensorManager sensorManager;
    private final Display display;
    private Sensor rotationVectorSensor;
    private Sensor accelerometer;
    private Sensor magneticField;

    private boolean usingRotationVector;
    private final float[] rotationMatrix = new float[9];
    private final float[] remappedMatrix = new float[9];
    private int displayRotation = -1;
    private int displayAxisX;
    private int displayAxisY;
    private volatile float declinationDegrees;

    private volatile float[] smoothedOrientation = new float[]{0, 0, -1, 0, 1, 0};
    private volatile boolean hasData;

    private final float[] lastAccel = new float[3];
    private final float[] lastMag = new float[3];
    private boolean hasAccel, hasMag;

    public SensorController(SensorManager sensorManager, Display display) {
        this.sensorManager = sensorManager;
        this.display = display;
    }

    public void start() {
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (rotationVectorSensor != null) {
            Log.d(TAG, "Using fused rotation-vector sensor");
            usingRotationVector = true;
            if (sensorManager.registerListener(
                    this, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME)) {
                return;
            }
            Log.w(TAG, "Rotation-vector sensor registration failed; trying accelerometer and magnetometer");
            sensorManager.unregisterListener(this);
        } else {
            Log.d(TAG, "Rotation-vector sensor unavailable; trying accelerometer and magnetometer");
        }
        usingRotationVector = false;
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        boolean accelRegistered = accelerometer != null
                && sensorManager.registerListener(
                        this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        boolean magneticRegistered = magneticField != null
                && sensorManager.registerListener(
                        this, magneticField, SensorManager.SENSOR_DELAY_GAME);
        if (!accelRegistered || !magneticRegistered) {
            sensorManager.unregisterListener(this);
            Log.e(TAG, "Unable to register both accelerometer and magnetometer; orientation unavailable");
        }
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    public boolean hasOrientation() { return hasData; }

    /** Copies one coherent forward-then-up vector pair into the caller's six-element buffer. */
    public void copyOrientation(float[] destination) {
        System.arraycopy(smoothedOrientation, 0, destination, 0, 6);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (usingRotationVector) {
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                remapToDisplay(rotationMatrix);
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
            remapToDisplay(R);
            updateFromMatrix(remappedMatrix);
        }
    }

    public void setDeclinationDegrees(float declinationDegrees) {
        this.declinationDegrees = declinationDegrees;
    }

    private void remapToDisplay(float[] matrix) {
        int rotation = display.getRotation();
        if (rotation != displayRotation) {
            displayAxisX = displayAxisX(rotation);
            displayAxisY = displayAxisY(rotation);
            displayRotation = rotation;
        }
        SensorManager.remapCoordinateSystem(
                matrix, displayAxisX, displayAxisY, remappedMatrix);
    }

    static int displayAxisX(int rotation) {
        switch (rotation) {
            case Surface.ROTATION_90:
                return SensorManager.AXIS_Y;
            case Surface.ROTATION_180:
                return SensorManager.AXIS_MINUS_X;
            case Surface.ROTATION_270:
                return SensorManager.AXIS_MINUS_Y;
            case Surface.ROTATION_0:
            default:
                return SensorManager.AXIS_X;
        }
    }

    static int displayAxisY(int rotation) {
        switch (rotation) {
            case Surface.ROTATION_90:
                return SensorManager.AXIS_MINUS_X;
            case Surface.ROTATION_180:
                return SensorManager.AXIS_MINUS_Y;
            case Surface.ROTATION_270:
                return SensorManager.AXIS_X;
            case Surface.ROTATION_0:
            default:
                return SensorManager.AXIS_Y;
        }
    }

    static float[] extractCameraOrientation(float[] matrix, float declinationDegrees) {
        float[] orientation = {
                -matrix[2], -matrix[8], matrix[5],
                matrix[1], matrix[7], -matrix[4]
        };
        double declination = Math.toRadians(declinationDegrees);
        float cos = (float) Math.cos(declination);
        float sin = (float) Math.sin(declination);
        for (int offset = 0; offset < orientation.length; offset += 3) {
            float east = orientation[offset];
            float south = orientation[offset + 2];
            orientation[offset] = east * cos - south * sin;
            orientation[offset + 2] = east * sin + south * cos;
        }
        return orientation;
    }

    private void updateFromMatrix(float[] R) {
        float[] measured = extractCameraOrientation(R, declinationDegrees);
        float[] newOrientation = hasData ? measured.clone() : measured;

        if (hasData) {
            float[] orientation = smoothedOrientation;
            for (int i = 0; i < newOrientation.length; i++) {
                newOrientation[i] = orientation[i]
                        + (measured[i] - orientation[i]) * LOW_PASS;
            }
            if (!orthogonalize(newOrientation)) {
                newOrientation = measured;
            }
        }

        smoothedOrientation = newOrientation;
        hasData         = true;
    }

    private static boolean orthogonalize(float[] v) {
        float forwardLength = normalize(v, 0);
        if (forwardLength < 1e-5f) return false;

        float dot = v[0] * v[3] + v[1] * v[4] + v[2] * v[5];
        v[3] -= dot * v[0];
        v[4] -= dot * v[1];
        v[5] -= dot * v[2];
        return normalize(v, 3) >= 1e-5f;
    }

    private static float normalize(float[] v, int offset) {
        float length = (float) Math.sqrt(
                v[offset] * v[offset]
                        + v[offset + 1] * v[offset + 1]
                        + v[offset + 2] * v[offset + 2]);
        if (length > 1e-10f) {
            v[offset] /= length;
            v[offset + 1] /= length;
            v[offset + 2] /= length;
        }
        return length;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
