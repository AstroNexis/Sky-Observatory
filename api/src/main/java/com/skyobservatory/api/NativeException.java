/*
 * Copyright 2026 Phuc An <pan2512811@gmail.com>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skyobservatory.api;

/**
 * Thrown when the native calculation layer reports an error.
 *
 * This exception indicates that a valid input was provided but the underlying
 * native library was unable to produce a result. Possible causes include
 * unsupported body types, ephemeris data gaps, or internal library failures.
 *
 * Callers may catch this to handle native failures separately from validation
 * failures. The base {@link AstroException} covers both.
 */
public final class NativeException extends AstroException {

    public NativeException(String message) {
        super(message);
    }

    public NativeException(String message, Throwable cause) {
        super(message, cause);
    }
}
