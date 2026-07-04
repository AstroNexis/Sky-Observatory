package com.skyobservatory.api;

import org.junit.Test;

import static org.junit.Assert.*;

public class ValidationExceptionTest {

    @Test
    public void isAstroException() {
        ValidationException e = new ValidationException("invalid");
        assertTrue(e instanceof AstroException);
    }

    @Test
    public void messageOnly() {
        ValidationException e = new ValidationException("bad input");
        assertEquals("bad input", e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    public void messageWithCause() {
        Throwable cause = new IllegalArgumentException("root");
        ValidationException e = new ValidationException("bad input", cause);
        assertEquals("bad input", e.getMessage());
        assertSame(cause, e.getCause());
    }
}
