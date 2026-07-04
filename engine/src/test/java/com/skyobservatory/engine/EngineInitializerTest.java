package com.skyobservatory.engine;

import com.skyobservatory.api.AstronomyProvider;

import org.junit.Test;

import static org.junit.Assert.*;

public class EngineInitializerTest {

    @Test
    public void providerReturnsNonNullProvider() {
        AstronomyProvider provider = EngineInitializer.provider();
        assertNotNull(provider);
    }

    @Test
    public void registerDoesNotThrow() {
        EngineInitializer.register();
        EngineInitializer.register();
    }
}
