/*
 * Copyright (C) 2013 Sean J. Barbeau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ec.edu.uce.smartmobuce.controlador;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.GnssMeasurement;
import android.location.GnssNavigationMessage;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import ec.edu.uce.smartmobuce.controlador.DilutionOfPrecision;

public class GpsTestUtil {

    private static final String TAG = "GpsTestUtil";

    private static final String NMEA_OUTPUT_TAG = "GpsOutputNmea";

    private static final String MEASURE_OUTPUT_TAG = "GpsOutputMeasure";

    private static final String NM_OUTPUT_TAG = "GpsOutputNav";

    private static StringBuilder mNmeaOutput = new StringBuilder();


    /**
     * Returns true if this device supports the Sensor.TYPE_ROTATION_VECTOR sensor, false if it
     * doesn't
     *
     * @return true if this device supports the Sensor.TYPE_ROTATION_VECTOR sensor, false if it
     * doesn't
     */
    public static boolean isRotationVectorSensorSupported(Context context) {
        SensorManager sensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD &&
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null;
    }

    /**
     * Returns true if the app is running on a large screen device, false if it is not
     *
     * @return true if the app is running on a large screen device, false if it is not
     */
    public static boolean isLargeScreen(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Returns true if the device supports the Gnss status listener, false if it does not
     *
     * @return true if the device supports the Gnss status listener, false if it does not
     */
    public static boolean isGnssStatusListenerSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    /**
     * Returns true if the platform supports providing carrier frequencies for each satellite, false if it does not
     *
     * @return true if the platform supports providing carrier frequencies for each satellite, false if it does not
     */
    public static boolean isGnssCarrierFrequenciesSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    /**
     * Returns true if the platform supports providing vertical accuracy values, false if it does not
     *
     * @return true if the platform supports providing vertical accuracy values, false if it does not
     */
    public static boolean isVerticalAccuracySupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    /**
     * Returns true if the platform supports providing speed and bearing accuracy values, false if it does not
     *
     * @return true if the platform supports providing speed and bearing accuracy values, false if it does not
     */
    public static boolean isSpeedAndBearingAccuracySupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    /**
     * Creates a unique key to identify this satellite using a combination of both the svid and
     * constellation type
     *
     * @return a unique key to identify this satellite using a combination of both the svid and
     * constellation type
     */
    public static String createGnssSatelliteKey(int svid, int constellationType) {
        return String.valueOf(svid) + " " + String.valueOf(constellationType);
    }


    /**
     * Outputs the provided nmea message and timestamp to log
     *
     * @param timestamp timestamp to write to the log, or Long.MIN_VALUE to not write a timestamp
     *                  to
     *                  log
     */
    public static void writeNmeaToLog(String nmea, long timestamp) {
        mNmeaOutput.setLength(0);
        if (timestamp != Long.MIN_VALUE) {
            mNmeaOutput.append(timestamp);
            mNmeaOutput.append(",");
        }
        mNmeaOutput.append(nmea);
        Log.d(NMEA_OUTPUT_TAG, mNmeaOutput.toString());
    }

    /**
     * Outputs the provided GNSS navigation message to log
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void writeNavMessageToLog(GnssNavigationMessage message) {
        Log.d(NM_OUTPUT_TAG, message.toString());
    }

    /**
     * Outputs the provided GNSS measurement to log
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void writeGnssMeasurementToLog(GnssMeasurement measurement) {
        Log.d(MEASURE_OUTPUT_TAG, measurement.toString());
    }

    /**
     * Given a $GPGGA or $GNGNS NMEA sentence, return the altitude above mean sea level (geoid
     * altitude),
     * or null if the altitude can't be parsed.
     *
     * Example inputs are:
     * $GPGGA,032739.0,2804.732835,N,08224.639709,W,1,08,0.8,19.2,M,-24.0,M,,*5B
     * $GNGNS,015002.0,2804.733672,N,08224.631117,W,AAN,09,1.1,78.9,-24.0,,*23
     *
     * Example outputs would be:
     * 19.2
     * 78.9
     *
     * @param nmeaSentence a $GPGGA or $GNGNS NMEA sentence
     * @return the altitude above mean sea level (geoid altitude), or null if altitude can't be
     * parsed
     */
    public static Double getAltitudeMeanSeaLevel(String nmeaSentence) {
        final int ALTITUDE_INDEX = 9;
        String[] tokens = nmeaSentence.split(",");

        if (nmeaSentence.startsWith("$GPGGA") || nmeaSentence.startsWith("$GNGNS")) {
            String altitude;
            try {
                altitude = tokens[ALTITUDE_INDEX];
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "Bad NMEA sentence for geoid altitude - " + nmeaSentence + " :" + e);
                return null;
            }
            if (!TextUtils.isEmpty(altitude)) {
                Double altitudeParsed = null;
                try {
                    altitudeParsed = Double.parseDouble(altitude);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Bad geoid altitude value of '" + altitude + "' in NMEA sentence " + nmeaSentence + " :" + e);
                }
                return altitudeParsed;
            } else {
                Log.w(TAG, "Couldn't parse geoid altitude from NMEA: " + nmeaSentence);
                return null;
            }
        } else {
            Log.w(TAG, "Input must be a $GPGGA or $GNGNS NMEA: " + nmeaSentence);
            return null;
        }
    }

    /**
     * Given a $GNGSA or $GPGSA NMEA sentence, return the dilution of precision, or null if dilution of
     * precision can't be parsed.
     *
     * Example inputs are:
     * $GPGSA,A,3,03,14,16,22,23,26,,,,,,,3.6,1.8,3.1*38
     * $GNGSA,A,3,03,14,16,22,23,26,,,,,,,3.6,1.8,3.1,1*3B
     *
     * Example output is:
     * PDOP is 3.6, HDOP is 1.8, and VDOP is 3.1
     *
     * @param nmeaSentence a $GNGSA or $GPGSA NMEA sentence
     * @return the dilution of precision, or null if dilution of precision can't be parsed
     */
    public static DilutionOfPrecision getDop(String nmeaSentence) {
        final int PDOP_INDEX = 15;
        final int HDOP_INDEX = 16;
        final int VDOP_INDEX = 17;
        String[] tokens = nmeaSentence.split(",");

        if (nmeaSentence.startsWith("$GNGSA") || nmeaSentence.startsWith("$GPGSA")) {
            String pdop, hdop, vdop;
            try {
                pdop = tokens[PDOP_INDEX];
                hdop = tokens[HDOP_INDEX];
                vdop = tokens[VDOP_INDEX];
                System.out.println("valor PDOP: "+pdop);
                Log.e(TAG,"pDOP"+pdop);
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "Bad NMEA message for parsing DOP - " + nmeaSentence + " :" + e);
                return null;
            }

            // See https://github.com/barbeau/gpstest/issues/71#issuecomment-263169174
            if (vdop.contains("*")) {
                vdop = vdop.split("\\*")[0];
            }

            if (!TextUtils.isEmpty(pdop) && !TextUtils.isEmpty(hdop) && !TextUtils.isEmpty(vdop)) {
                DilutionOfPrecision dop = null;
                try {
                    dop = new DilutionOfPrecision(Double.valueOf(pdop), Double.valueOf(hdop),
                            Double.valueOf(vdop));
                } catch (NumberFormatException e) {
                    // See https://github.com/barbeau/gpstest/issues/71#issuecomment-263169174
                    Log.e(TAG, "Invalid DOP values in NMEA: " + nmeaSentence);
                }
                return dop;
            } else {
                Log.w(TAG, "Empty DOP values in NMEA: " + nmeaSentence);
                return null;
            }
        } else {
            Log.w(TAG, "Input must be a $GNGSA NMEA: " + nmeaSentence);
            return null;
        }
    }

}
