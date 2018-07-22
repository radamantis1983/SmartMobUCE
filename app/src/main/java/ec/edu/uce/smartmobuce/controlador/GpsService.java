package ec.edu.uce.smartmobuce.controlador;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.HashMap;

import ec.edu.uce.smartmobuce.R;
import ec.edu.uce.smartmobuce.vista.GPSActivity;

public class GpsService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    protected static final String LOG_TAG = "Servicio";
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    private String usr,fecha;
    protected LocationRequest mLocationRequest;
    private final Metodos m = new Metodos();
    private final ControladorSQLite controller = new ControladorSQLite(this);
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private TextView mAccuracyText;
    private TextView mAltitudeText;
    private TextView mSpeedText;
    private TextView mProviderText;
    private TextView mDatetext;
    //horas que permite guardar datos en la base interna
    public static final String horaInicial = "06:00:00";
    public static final String horaFinal = "22:00:00";
    //Horas en la cual se ejecuta automaticamente la actualizacion
    public static final String horaActualizacion = "01:00:00";//hora de inicio para sincronizar datos
    public static final String horaActualizacionf = "01:30:00";//hora de fin para sincronizar datos
    public static final long INTERVALOS_DETECCION_GPS_EN_MILISEGUNDOS = 1000; //1000 MILISEGUNDOS EQUIVALE A UN SEGUNDO(5*60*1000) EQUIVALE A 5 MIN


    public GpsService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

           }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        buildGoogleApiClient();
        return  START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(LOG_TAG,"Connection OK!!! ");

        int permissionCheck= ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck==-1) {
            ActivityCompat.requestPermissions((GPSActivity) getBaseContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            permissionCheck= ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if(permissionCheck==0){
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(mLastLocation != null){
            /*    Intent i = new Intent("location_update");
                i.putExtra("coordenadas", "\n Latitud = " + mLastLocation.getLatitude()
                        + "\n Longitud = " + mLastLocation.getLongitude()
                        + "\n conducta = " + mLastLocation.getBearing()
                        + "\n precision = " + mLastLocation.getAccuracy()
                        + "\n altitud = " + mLastLocation.getAltitude()
                        + "\n Speed = " + mLastLocation.getSpeed()
                        + "\n Provider = " + mLastLocation.getProvider()
                        + "\n Fecha = " + m.getFechaActual());
                sendBroadcast(i);
*/

                mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
                mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
                mAccuracyText.setText(String.valueOf(mLastLocation.getAccuracy()));
                mAltitudeText.setText(String.valueOf(mLastLocation.getAltitude()));
                mSpeedText.setText(String.valueOf(mLastLocation.getSpeed()));
                mProviderText.setText(String.valueOf(mLastLocation.getProvider()));
                mDatetext.setText(String.valueOf(m.getFechaActual()));


            }

            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(INTERVALOS_DETECCION_GPS_EN_MILISEGUNDOS);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);


        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG,"Connection Suspended!!! " + i + " cause");
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(LOG_TAG,"Connection Failed!!! " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(LOG_TAG,"Localizacion"+location.toString());
        usr = m.cargarPreferencias(getBaseContext());
        mLatitudeText.setText(String.valueOf(location.getLatitude()));
        mLongitudeText.setText(String.valueOf(location.getLongitude()));
        mAccuracyText.setText(String.valueOf(location.getAccuracy()));
        mAltitudeText.setText(String.valueOf(location.getAltitude()));
        mSpeedText.setText(String.valueOf(location.getSpeed()));
        mProviderText.setText(String.valueOf(location.getProvider()));
        mDatetext.setText(String.valueOf(m.getFechaActual()));
        fecha = m.getFechaActual();
        Boolean area1 = m.revisarArea(location.getLatitude(), location.getLongitude());

        System.out.println("comprueba si hay cambio de lugar");
        if (m.lastlocation(location.getLatitude(),location.getLongitude())) {

        //si se encuentra dentro del area capturamos los datos
        if (area1) {
            //si la aplicacion esta en el horario definido guardamos los datos
            if (m.rangoHoras(m.getHoraActual(), horaInicial, horaFinal)) {
                //prepara los datos a ser enviados al query de insertar datos a la base
                HashMap<String, String> queryValues = new HashMap<String, String>();
                queryValues.put("usu_id", usr);
                queryValues.put("dat_latitud", String.valueOf(location.getLatitude()));
                queryValues.put("dat_longitud", String.valueOf(location.getLongitude()));
                queryValues.put("dat_precision", String.valueOf(location.getAccuracy()));
                queryValues.put("dat_altitud", String.valueOf(location.getAltitude()));
                queryValues.put("dat_velocidad", String.valueOf(location.getSpeed()));
                queryValues.put("dat_proveedor", location.getProvider());
                queryValues.put("dat_fechahora_lectura", fecha);
                controller.insertDatos(queryValues);
            }

            //comprueba la hora para sincronizacón con la base de datos
            if (m.rangoHorassincronizacion(m.getHoraActual(), horaActualizacion, horaActualizacionf)) {
                //lista los datos para sincronizar
                ArrayList<HashMap<String, String>> userList = controller.getAllUsers();
                if (userList.size() != 0) {
                }
                m.syncSQLiteMySQLDB(getApplicationContext());
            }
        }
        }
        else{
            System.out.println("el celular no esta en movimiento y tampoco hubo cambio de lugar");
            //mostramos los datos en cero
            mLatitudeText.setText("0");
            mLongitudeText.setText("0");
            mAccuracyText.setText("0");
            mAltitudeText.setText("0");
            mSpeedText.setText("0");
            mProviderText.setText("0");
            mDatetext.setText(String.valueOf(m.getFechaActual()));

            //insertamos los datos en cero
            HashMap<String, String> queryValues = new HashMap<String, String>();
            queryValues.put("usu_id", usr);
            queryValues.put("dat_latitud", "0.0");
            queryValues.put("dat_longitud", "0.0");
            queryValues.put("dat_precision", "0.0");
            queryValues.put("dat_altitud", "0.0");
            queryValues.put("dat_velocidad", "0.0");
            queryValues.put("dat_proveedor", "n/a");
            queryValues.put("dat_fechahora_lectura", fecha);
            controller.insertDatos(queryValues);
            System.out.println(" Latitud0 = " + location.getLatitude()
                    + "\n Longitud0 = " + location.getLongitude());
        }

    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(LOG_TAG,"Build Google API");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


}
