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

        assertEquals(previousRoll, SensorController.calculateLabelRollRadians(
                new float[]{0f, 1f, 0f}, new float[]{0f, 0f, 1f}, previousRoll), 1e-6f);
        assertEquals(previousRoll, SensorController.calculateLabelRollRadians(
                new float[]{0f, -1f, 0f}, new float[]{0f, 0f, -1f}, previousRoll), 1e-6f);
    }
}
