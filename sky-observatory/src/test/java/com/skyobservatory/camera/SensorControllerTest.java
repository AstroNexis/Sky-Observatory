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
    public void labelRollStaysDownAfterPointingAtGroundAndTurningLevel() {
        float[] previousGravityUp = new float[3];
        float[] previousForward = new float[3];

        SensorController.calculateLabelRollRadians(
                new float[]{0f, -0.995f, -0.1f, 0f, 0.1f, -0.995f},
                previousGravityUp, previousForward);
        SensorController.calculateLabelRollRadians(
                new float[]{0f, -0.995f, 0.1f, 0f, 0.1f, -0.995f},
                previousGravityUp, previousForward);
        assertEquals(0f, SensorController.calculateLabelRollRadians(
                new float[]{0f, -1f, 0f, 0f, 0f, -1f},
                previousGravityUp, previousForward), 1e-6f);

        assertEquals(0f, SensorController.calculateLabelRollRadians(
                new float[]{0f, 0f, -1f, 0f, -1f, 0f},
                previousGravityUp, previousForward), 1e-6f);
    }

    @Test
    public void displayXAxisFollowsBothLandscapeRotations() {
        assertEquals(SensorManager.AXIS_Y,
                SensorController.getDisplayXAxis(Surface.ROTATION_90));
        assertEquals(SensorManager.AXIS_MINUS_Y,
                SensorController.getDisplayXAxis(Surface.ROTATION_270));
    }
}
