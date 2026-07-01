# Keep NativeAstroCalculator so the engine module can resolve it after shrinking.
-keep class com.skyobservatory.astronomy.native_bridge.NativeAstroCalculator { public *; }

# Keep native method declarations so JNI registration remains valid.
-keepclasseswithmembernames class * {
    native <methods>;
}
