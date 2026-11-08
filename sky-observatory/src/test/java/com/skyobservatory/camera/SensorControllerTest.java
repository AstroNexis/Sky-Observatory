package com.skyobservatory.camera;

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
    public void labelRollRetainsPreviousOrientationAtPoles() {
        float previousRoll = (float) (Math.PI / 2.0);
        float[] previousGravityUp = new float[3];

        assertEquals(previousRoll, SensorController.calculateLabelRollRadians(
                new float[]{0f, 1f, 0f}, new float[]{0f, 0f, 1f},
                previousRoll, previousGravityUp), 1e-6f);
        assertEquals(previousRoll, SensorController.calculateLabelRollRadians(
                new float[]{0f, -1f, 0f}, new float[]{0f, 0f, -1f},
                previousRoll, previousGravityUp), 1e-6f);
    }

    @Test
    public void labelRollStaysContinuousAcrossBothPoles() {
        float[] previousGravityUp = new float[3];
        float before = SensorController.calculateLabelRollRadians(
                new float[]{0f, 0.995f, -0.1f}, new float[]{0f, 0.1f, 0.995f},
                0f, previousGravityUp);
        float after = SensorController.calculateLabelRollRadians(
                new float[]{0f, 0.995f, 0.1f}, new float[]{0f, 0.1f, 0.995f},
                before, previousGravityUp);
        assertEquals(0f, before, 1e-6f);
        assertEquals(before, after, 1e-6f);

        previousGravityUp = new float[3];
        before = SensorController.calculateLabelRollRadians(
                new float[]{0f, -0.995f, -0.1f}, new float[]{0f, 0.1f, -0.995f},
                0f, previousGravityUp);
        after = SensorController.calculateLabelRollRadians(
                new float[]{0f, -0.995f, 0.1f}, new float[]{0f, 0.1f, -0.995f},
                before, previousGravityUp);
        assertEquals(0f, before, 1e-6f);
        assertEquals(before, after, 1e-6f);
    }
}
