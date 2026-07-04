cmake_minimum_required(VERSION 3.22.1)

project(astro_bridge VERSION 0.1.1 LANGUAGES C CXX)

# ---------------------------------------------------------------------------
# SuperNOVAS as a subdirectory dependency.
#
# The supernovas submodule lives at a path with a leading space relative to
# the workspace root. From this CMakeLists.txt, the relative path going up
# four levels from native/src/main/cpp/ reaches astronomy-sdk/, and then
# into "native/src/main/cpp/supernovas".
#
# We force all the optional SuperNOVAS features off so the Android build
# only compiles the core C99 astrometry library.
# ---------------------------------------------------------------------------

set(BUILD_TESTING     OFF CACHE BOOL "" FORCE)
set(BUILD_EXAMPLES    OFF CACHE BOOL "" FORCE)
set(BUILD_BENCHMARK   OFF CACHE BOOL "" FORCE)
set(BUILD_DOC         OFF CACHE BOOL "" FORCE)
set(ENABLE_CPP        OFF CACHE BOOL "" FORCE)
set(ENABLE_CALCEPH    OFF CACHE BOOL "" FORCE)
set(ENABLE_CSPICE     OFF CACHE BOOL "" FORCE)
set(WITHOUT_CURL      ON  CACHE BOOL "" FORCE)

# Resolve the absolute path to the supernovas submodule, which lives in a
# sibling directory at the workspace root named " native" (leading space).
# CMake handles spaces in paths correctly when paths are quoted.
get_filename_component(
    WORKSPACE_ROOT
    "${CMAKE_CURRENT_SOURCE_DIR}/../../../.."
    ABSOLUTE
)
set(SUPERNOVAS_SOURCE_DIR "${WORKSPACE_ROOT}/native/src/main/cpp/supernovas")
set(SUPERNOVAS_INCLUDE_DIR "${SUPERNOVAS_SOURCE_DIR}/include")

add_subdirectory(
    "${SUPERNOVAS_SOURCE_DIR}"
    "${CMAKE_CURRENT_BINARY_DIR}/supernovas"
    EXCLUDE_FROM_ALL
)

# ---------------------------------------------------------------------------
# JNI bridge shared library
#
# This is the only symbol surface exposed to the Java layer. SuperNOVAS
# symbols are linked in but not re-exported.
# ---------------------------------------------------------------------------

add_library(
    astro_bridge
    SHARED
    astro_bridge.cpp
)

target_include_directories(astro_bridge PRIVATE
    "${SUPERNOVAS_INCLUDE_DIR}"
)

target_link_libraries(astro_bridge
    PRIVATE
        core          # the supernovas static/shared library target
        android
        log
)

# Prevent SuperNOVAS internal symbols from being visible outside astro_bridge.so
target_link_options(astro_bridge PRIVATE
    -Wl,--version-script=${CMAKE_CURRENT_SOURCE_DIR}/astro_bridge.map
)
