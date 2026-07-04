package com.skyobservatory.api;

import org.junit.Test;

import static org.junit.Assert.*;

public class AstroExceptionTest {

    @Test
    public void messageOnly() {
        AstroException e = new AstroException("test error");
        assertEquals("test error", e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    public void messageWithCause() {
        Throwable cause = new RuntimeException("root");
        AstroException e = new AstroException("wrapped", cause);
        assertEquals("wrapped", e.getMessage());
        assertSame(cause, e.getCause());
    }
}
