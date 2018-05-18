package ec.edu.uce.smartmobuce.controlador;

import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.LocationListener;

/**
 * Interface used by GpsTestActivity to communicate with Gps*Fragments
 */
public interface GpsTestListener extends LocationListener {

     void onNmeaMessage(String message, long timestamp);
}