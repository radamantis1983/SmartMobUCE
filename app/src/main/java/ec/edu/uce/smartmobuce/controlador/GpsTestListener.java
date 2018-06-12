package ec.edu.uce.smartmobuce.controlador;


import android.location.LocationListener;

/**
 * Interface used by GpsTestActivity to communicate with Gps*Fragments
 */
public interface GpsTestListener extends LocationListener {

     void onNmeaMessage(String message, long timestamp);
}