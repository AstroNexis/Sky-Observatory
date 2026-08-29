# sky-observatory proguard rules

# keep the main entry point
-keep class com.skyobservatory.renderer.RendererActivity { *; }

# keep opengl / jni bridge classes
-keep class com.skyobservatory.native_bridge.** { *; }

# keep crash handler accessible from the manifest
-keep class com.skyobservatory.util.CrashHandler { *; }