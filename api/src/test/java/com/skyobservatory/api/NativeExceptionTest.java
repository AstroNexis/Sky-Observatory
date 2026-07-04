package com.skyobservatory.api;

import org.junit.Test;

import static org.junit.Assert.*;

public class NativeExceptionTest {

    @Test
    public void isAstroException() {
        NativeException e = new NativeException("native fail");
        assertTrue(e instanceof AstroException);
    }

    @Test
    public void messageOnly() {
        NativeException e = new NativeException("library error");
        assertEquals("library error", e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    public void messageWithCause() {
        Throwable cause = new UnsatisfiedLinkError("not found");
        NativeException e = new NativeException("library error", cause);
        assertEquals("library error", e.getMessage());
        assertSame(cause, e.getCause());
    }
}
