# PR: Cleanup and Architectural Fixes

## Title
fix: cleanup hardcoded values, dead code, and architectural inconsistencies

## Label
refactor

## Body

### Summary
This PR addresses ~20 issues identified across the codebase: hardcoded values, duplicated code, dead code, architectural flaws, and synchronization gaps.

### Changes

#### Hardcoded Values
- NDK version: Centralized ndk;27.2.12479018 into env.NDK_VERSION across 6 workflow files
- Observer coordinates: Moved Hanoi fallback coordinates (21.0285, 105.8542) into named constants in RendererActivity and benchmark classes

#### Cleanup / Synchronization
- Removed duplicate ShaderLoader methods: createProgram() and compileShader() were duplicated in ShaderManager, now delegates to ShaderLoader
- Removed dead C++ JNI methods: nativeCalculateAzimuth() and nativeCalculateAltitude() - never called from Java
- Removed dead NativeGateway declarations for the above methods
- Fixed TouchStateManager.setInertiaDecay() - was a no-op, now actually sets the decay value

#### Architectural Fixes
- Fixed RendererActivity Cartesian coordinates: Changed from CartesianCoordinate(0,0,0) to using engine.project() which properly computes coordinates
- Fixed GPU memory leak in SkyRenderer: Added MeshRenderer.cleanup() with glDeleteVertexArrays/glDeleteBuffers before recreating objects in applySnapshot() and onSurfaceCreated()
- Fixed visibility bugs: VisibilityResolver, InputValidator, CoordinateConverter changed from public to package-private to match their Javadoc
- Fixed RendererBenchmark WORLD_SCALE: Changed from 10f to 9.5f to match SkyRenderer and CelestialGrid
- Added null check in NativeAstroCalculator.calculatePosition(): Guards against null JNI return
- Added UnsatisfiedLinkError handling in NativeAstroCalculator.getNativeLibraryVersion()

### Breaking Changes
None. All changes are internal refactoring with no public API changes.

### Testing
- All existing unit tests pass
- WORLD_SCALE is now consistent across renderer, grid, and benchmark
- Removed C++ native methods have no Java callers