package com.skyobservatory.engine;

import com.skyobservatory.api.AstronomyProvider;
import com.skyobservatory.api.AstroSdk;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class EngineInitializerTest {

    @After
    public void tearDown() {
        AstroSdk.resetForTesting();
    }

    @Test
    public void providerReturnsNonNullProvider() {
        AstronomyProvider provider = EngineInitializer.provider();
        assertNotNull(provider);
    }

    @Test
    public void registerDoesNotThrow() {
        // register() should not throw even if called multiple times.
        EngineInitializer.register();
        EngineInitializer.register();
    }
}
