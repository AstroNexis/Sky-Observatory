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
 * Thrown when the SDK cannot complete an astronomical calculation.
 *
 * Subclasses carry more specific failure categories (validation, native errors, etc.)
 * but callers that do not need to distinguish error kinds can catch this base type.
 */
public class AstroException extends Exception {

    public AstroException(String message) {
        super(message);
    }

    public AstroException(String message, Throwable cause) {
        super(message, cause);
    }
}
