package com.skyobservatory.camera;

import android.hardware.SensorManager;
import android.view.Surface;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SensorControllerTest {

    @Test
    public void labelRollSnapsSymmetricallyAtHalfQuarterTurns() {
        float halfQuarterTurn = (float) (Math.PI / 4.0);
        float quarterTurn = (float) (Math.PI / 2.0);

        assertEquals(-quarterTurn, SensorController.snapLabelRoll(halfQuarterTurn), 1e-6f);
        assertEquals(quarterTurn, SensorController.snapLabelRoll(-halfQuarterTurn), 1e-6f);
    }

    @Test
    public void labelRollUsesDefaultOrientationNearPoles() {
        float[] previousGravityUp = new float[3];
        float[] previousForward = new float[3];

        assertEquals(0f, SensorController.calculateLabelRollRadians(
                new float[]{0f, 1f, 0f, 0f, 0f, 1f},
                previousGravityUp, previousForward), 1e-6f);
        assertEquals(0f, SensorController.calculateLabelRollRadians(
                new float[]{0f, -1f, 0f, 0f, 0f, -1f},
                previousGravityUp, previousForward), 1e-6f);
        assertEquals(0f, SensorController.calculateLabelRollRadians(
                new float[]{0f, 0.999f, -0.0447f, 0f, 0.0447f, 0.999f},
                previousGravityUp, previousForward), 1e-6f);
        assertEquals(0f, SensorController.calculateLabelRollRadians(
                new float[]{0f, -0.999f, -0.0447f, 0f, 0.0447f, -0.999f},
                previousGravityUp, previousForward), 1e-6f);
    }

    @Test
    public void labelRollStaysHorizontalNearDownwardPoleRegardlessOfDeviceRoll() {
        float roll = SensorController.calculateLabelRollRadians(
                new float[]{0f, -0.98f, -0.198997f, 1f, 0f, 0f},
                new float[3], new float[3]);

        assertEquals(0f, roll, 1e-6f);
    }

    @Test
    public void labelRollStaysContinuousAcrossBothPoles() {
        float[] previousGravityUp = new float[3];
        float[] previousForward = new float[3];
        float before = SensorController.calculateLabelRollRadians(
                new float[]{0f, 0.995f, -0.1f, 0f, 0.1f, 0.995f},
                previousGravityUp, previousForward);
        float after = SensorController.calculateLabelRollRadians(
                new float[]{0f, 0.995f, 0.1f, 0f, 0.1f, 0.995f},
                previousGravityUp, previousForward);
        assertEquals(0f, before, 1e-6f);
        assertEquals(before, after, 1e-6f);

        previousGravityUp = new float[3];
        previousForward = new float[3];
        before = SensorController.calculateLabelRollRadians(
                new float[]{0f, -0.995f, -0.1f, 0f, 0.1f, -0.995f},
                previousGravityUp, previousForward);
        after = SensorController.calculateLabelRollRadians(
                new float[]{0f, -0.995f, 0.1f, 0f, 0.1f, -0.995f},
                previousGravityUp, previousForward);
        assertEquals(0f, before, 1e-6f);
        assertEquals(before, after, 1e-6f);
    }

    @Test
    public void abruptShakeDoesNotKeepStaleGravityReference() {
        float[] previousGravityUp = new float[3];
        float[] previousForward = new float[3];

        SensorController.calculateLabelRollRadians(
                new float[]{0f, 0.995f, -0.1f, 0f, 0.1f, 0.995f},
                previousGravityUp, previousForward);
        float roll = SensorController.calculateLabelRollRadians(
                new float[]{0f, -0.995f, -0.1f, 0f, 0.1f, -0.995f},
                previousGravityUp, previousForward);

        assertEquals(0f, roll, 1e-6f);
    }

    @Test
    public void rotationMatrixColumnsProduceCameraForwardAndScreenUp() {
        float[] levelFacingNorth = {
                1f, 0f, 0f,
                0f, 0f, -1f,
                0f, 1f, 0f
        };
        float[] orientation = SensorController.extractCameraOrientation(levelFacingNorth, 0f);

        assertEquals(0f, orientation[0], 1e-6f);
        assertEquals(0f, orientation[1], 1e-6f);
        assertEquals(-1f, orientation[2], 1e-6f);
        assertEquals(0f, orientation[3], 1e-6f);
        assertEquals(1f, orientation[4], 1e-6f);
        assertEquals(0f, orientation[5], 1e-6f);
    }

    @Test
    public void pointingDownUsesDefaultHorizontalLabelOrientation() {
        float[] orientation = SensorController.extractCameraOrientation(new float[]{
                1f, 0f, 0f,
                0f, 1f, 0f,
                0f, 0f, 1f
        }, 0f);

        assertEquals(-1f, orientation[1], 1e-6f);
        assertEquals(0f, SensorController.calculateLabelRollRadians(
                orientation, new float[3], new float[3]), 1e-6f);
    }

    @Test
    public void magneticNorthIsCorrectedToTrueNorth() {
        float declination = 12f;
        float[] orientation = SensorController.extractCameraOrientation(new float[]{
                1f, 0f, 0f,
                0f, 0f, -1f,
                0f, 1f, 0f
        }, declination);

        assertEquals((float) Math.sin(Math.toRadians(declination)),
                orientation[0], 1e-6f);
        assertEquals(-(float) Math.cos(Math.toRadians(declination)),
                orientation[2], 1e-6f);
    }

    @Test
    public void displayAxesFollowAllFourDisplayRotations() {
        assertAxes(Surface.ROTATION_0, SensorManager.AXIS_X, SensorManager.AXIS_Y);
        assertAxes(Surface.ROTATION_90, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X);
        assertAxes(Surface.ROTATION_180, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y);
        assertAxes(Surface.ROTATION_270, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X);
    }

    private static void assertAxes(int rotation, int expectedX, int expectedY) {
        assertEquals(expectedX, SensorController.displayAxisX(rotation));
        assertEquals(expectedY, SensorController.displayAxisY(rotation));
    }
}
