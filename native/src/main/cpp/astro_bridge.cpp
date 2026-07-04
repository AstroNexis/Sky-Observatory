/**
 * JNI bridge between the Android Java layer and the SuperNOVAS C library.
 *
 * Computes topocentric azimuth and altitude for solar system bodies using
 * the SuperNOVAS astrometry library.
 *
 * No SuperNOVAS types or symbols appear in the function signatures here.
 * The Java-facing contract is expressed only in terms of primitive JNI types.
 */

#include <jni.h>
#include <android/log.h>
#include <cstdio>
#include <cmath>

#define LOG_TAG "AstroBridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// SuperNOVAS is compiled as C99; its headers lack C++ linkage guards.
// Include inside extern "C" so the C++ compiler generates the correct
// unmangled symbol references to match the static library.
extern "C" {
#include "novas.h"
}

extern "C" {

/**
 * Returns the native library version string.
 * Used by NativeGateway to confirm the library loaded correctly.
 */
JNIEXPORT jstring JNICALL
Java_com_skyobservatory_native_1bridge_NativeGateway_nativeGetVersion(
        JNIEnv* env,
        jclass /* clazz */) {
    char version[64];
    snprintf(version, sizeof(version), "astro_bridge/0.1.1 supernovas/%d.%d.%d%s",
             SUPERNOVAS_MAJOR_VERSION,
             SUPERNOVAS_MINOR_VERSION,
             SUPERNOVAS_PATCHLEVEL,
             SUPERNOVAS_RELEASE_STRING);
    return env->NewStringUTF(version);
}

/**
 * Computes azimuth and altitude for a solar system body using SuperNOVAS.
 *
 * @param naifId          NAIF integer identifier (10 = Sun)
 * @param julianDateTT    observation time as Julian Date in TT
 * @param latitudeDeg     observer geodetic latitude in degrees
 * @param longitudeDeg    observer geodetic longitude in degrees
 * @param altitudeMeters  observer altitude above ellipsoid in metres
 * @param azimuthOut      output azimuth in degrees [0, 360)
 * @param altitudeOut     output altitude in degrees [-90, 90]
 * @return 0 on success, -1 on error
 */
static int computeAzAlt(
        jint naifId,
        jdouble julianDateTT,
        jdouble latitudeDeg,
        jdouble longitudeDeg,
        jdouble altitudeMeters,
        double* azimuthOut,
        double* altitudeOut,
        double* raHoursOut,
        double* decDegOut,
        double* distanceAuOut,
        double* radialVelocityOut) {

    // Map NAIF ID to the SuperNOVAS planet enum.
    enum novas_planet planetId;
    if (naifId == 10) {
        planetId = NOVAS_SUN;
    } else if (naifId == 301) {
        planetId = NOVAS_MOON;
    } else {
        LOGE("computeAzAlt: unsupported NAIF ID %d", naifId);
        return -1;
    }

    // 1. Set up the time specification.
    novas_timespec ts = NOVAS_TIMESPEC_INIT;
    int ret = novas_set_time(NOVAS_TT, julianDateTT, 0, 0.0, &ts);
    if (ret != 0) {
        LOGE("computeAzAlt: novas_set_time failed: %d", ret);
        return -1;
    }

    // 2. Create the Earth-surface observer.
    observer obs;
    ret = make_observer_on_surface(
            static_cast<double>(latitudeDeg),
            static_cast<double>(longitudeDeg),
            static_cast<double>(altitudeMeters),
            0.0,     // temperature (not used with NOVAS_NO_ATMOSPHERE)
            1013.25, // pressure (not used with NOVAS_NO_ATMOSPHERE),
            &obs);
    if (ret != 0) {
        LOGE("computeAzAlt: make_observer_on_surface failed: %d", ret);
        return -1;
    }

    // 3. Create the observing frame.
    // xp=0, yp=0: no polar motion data available.
    novas_frame frame = NOVAS_FRAME_INIT;
    ret = novas_make_frame(NOVAS_REDUCED_ACCURACY, &obs, &ts, 0.0, 0.0, &frame);
    if (ret != 0) {
        LOGE("computeAzAlt: novas_make_frame failed: %d", ret);
        return -1;
    }

    // 4. Create the target body object (Sun).
    object target = {};
    ret = make_planet(planetId, &target);
    if (ret != 0) {
        LOGE("computeAzAlt: make_planet failed: %d", ret);
        return -1;
    }

    // 5. Compute the apparent topocentric position using built-in ephemeris.
    //    novas_approx_sky_pos works for Sun and Moon without external JPL files.
    sky_pos pos = {};
    ret = novas_approx_sky_pos(planetId, &frame, NOVAS_TOD, &pos);
    if (ret != 0) {
        LOGE("computeAzAlt: novas_approx_sky_pos failed: %d", ret);
        return -1;
    }

    // 6. Convert apparent TOD coordinates to local horizontal (azimuth, altitude).
    ret = novas_app_to_hor(&frame, NOVAS_TOD, pos.ra, pos.dec,
                           NULL, azimuthOut, altitudeOut);
    if (ret != 0) {
        LOGE("computeAzAlt: novas_app_to_hor failed: %d", ret);
        return -1;
    }

    // 7. Populate optional output fields.
    if (raHoursOut)        *raHoursOut        = pos.ra;
    if (decDegOut)         *decDegOut         = pos.dec;
    if (distanceAuOut)     *distanceAuOut     = pos.dis;
    if (radialVelocityOut) *radialVelocityOut = pos.rv;

    LOGI("computeAzAlt: naif=%d jd=%.6f lat=%.4f lon=%.4f alt=%.1f -> az=%.2f el=%.2f ra=%.4f dec=%.2f dist=%.6f rv=%.2f",
         naifId, julianDateTT, latitudeDeg, longitudeDeg, altitudeMeters,
         *azimuthOut, *altitudeOut, *raHoursOut, *decDegOut, *distanceAuOut, *radialVelocityOut);

    return 0;
}

/**
 * Native bulk calculation: returns all values from a single SuperNOVAS invocation.
 *
 * Returns a jdoubleArray of 7 values in this order:
 *   [azimuth, altitude, raHours, decDeg, distanceAu, radialVelocity, isErrorFlag]
 *   isErrorFlag: 0.0 = success, -1.0 = error
 */
JNIEXPORT jdoubleArray JNICALL
Java_com_skyobservatory_native_1bridge_NativeGateway_nativeCalculatePosition(
        JNIEnv* env,
        jclass  /* clazz */,
        jint    naifId,
        jdouble julianDateTT,
        jdouble latitudeDeg,
        jdouble longitudeDeg,
        jdouble altitudeMeters) {

    double azimuth = 0.0, altitude = 0.0;
    double raHours = 0.0, decDeg = 0.0, distanceAu = 0.0, radialVelocity = 0.0;

    int ret = computeAzAlt(naifId, julianDateTT, latitudeDeg, longitudeDeg,
                           altitudeMeters, &azimuth, &altitude,
                           &raHours, &decDeg, &distanceAu, &radialVelocity);

    jdouble results[7];
    if (ret != 0) {
        results[0] = -1.0;
        results[1] = -999.0;
        results[2] = 0.0; results[3] = 0.0; results[4] = 0.0; results[5] = 0.0;
        results[6] = -1.0;
    } else {
        results[0] = azimuth;
        results[1] = altitude;
        results[2] = raHours;
        results[3] = decDeg;
        results[4] = distanceAu;
        results[5] = radialVelocity;
        results[6] = 0.0;
    }

    jdoubleArray ary = env->NewDoubleArray(7);
    env->SetDoubleArrayRegion(ary, 0, 7, results);
    return ary;
}

/**
 * Computes the apparent topocentric azimuth of a solar system body.
 *
 * @param naifId          NAIF integer identifier of the target body
 * @param julianDateTT    observation time as Julian Date in TT
 * @param latitudeDeg     observer geodetic latitude in degrees
 * @param longitudeDeg    observer geodetic longitude in degrees
 * @param altitudeMeters  observer altitude above ellipsoid in metres
 * @return topocentric azimuth in degrees [0, 360), or -1.0 on error
 */
JNIEXPORT jdouble JNICALL
Java_com_skyobservatory_native_1bridge_NativeGateway_nativeCalculateAzimuth(
        JNIEnv* /* env */,
        jclass  /* clazz */,
        jint    naifId,
        jdouble julianDateTT,
        jdouble latitudeDeg,
        jdouble longitudeDeg,
        jdouble altitudeMeters) {

    double azimuth = 0.0;
    double altitude = 0.0;

    int ret = computeAzAlt(naifId, julianDateTT, latitudeDeg, longitudeDeg,
                           altitudeMeters, &azimuth, &altitude,
                           NULL, NULL, NULL, NULL);
    if (ret != 0) {
        LOGE("nativeCalculateAzimuth: calculation failed for naif=%d", naifId);
        return -1.0;
    }

    return azimuth;
}

/**
 * Computes the apparent topocentric altitude of a solar system body.
 *
 * @param naifId          NAIF integer identifier of the target body
 * @param julianDateTT    observation time as Julian Date in TT
 * @param latitudeDeg     observer geodetic latitude in degrees
 * @param longitudeDeg    observer geodetic longitude in degrees
 * @param altitudeMeters  observer altitude above ellipsoid in metres
 * @return topocentric altitude in degrees [-90, 90], or -999.0 on error
 */
JNIEXPORT jdouble JNICALL
Java_com_skyobservatory_native_1bridge_NativeGateway_nativeCalculateAltitude(
        JNIEnv* /* env */,
        jclass  /* clazz */,
        jint    naifId,
        jdouble julianDateTT,
        jdouble latitudeDeg,
        jdouble longitudeDeg,
        jdouble altitudeMeters) {

    double azimuth = 0.0;
    double altitude = 0.0;

    int ret = computeAzAlt(naifId, julianDateTT, latitudeDeg, longitudeDeg,
                           altitudeMeters, &azimuth, &altitude,
                           NULL, NULL, NULL, NULL);
    if (ret != 0) {
        LOGE("nativeCalculateAltitude: calculation failed for naif=%d", naifId);
        return -999.0;
    }

    return altitude;
}

} // extern "C"
