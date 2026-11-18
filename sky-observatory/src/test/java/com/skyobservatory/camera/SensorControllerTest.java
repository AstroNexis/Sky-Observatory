package com.skyobservatory.camera;

import android.hardware.SensorManager;
import android.view.Surface;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SensorControllerTest {

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
    public void pointingDownProducesDownwardCameraForward() {
        float[] orientation = SensorController.extractCameraOrientation(new float[]{
                1f, 0f, 0f,
                0f, 1f, 0f,
                0f, 0f, 1f
        }, 0f);

        assertEquals(0f, orientation[0], 1e-6f);
        assertEquals(-1f, orientation[1], 1e-6f);
        assertEquals(0f, orientation[2], 1e-6f);
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
