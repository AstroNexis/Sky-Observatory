# Public API models and interfaces must never be obfuscated or stripped.
# Consumers depend on stable names for reflection and serialization.
-keep public class com.skyobservatory.astronomy.api.** { public *; }
