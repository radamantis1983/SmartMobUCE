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
    private static final String LOG_TAG = "Servicio";
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String usr,fecha;
    private LocationRequest mLocationRequest;
    private final Metodos m = new Metodos();
    private final ControladorSQLite controller = new ControladorSQLite(this);



    public GpsService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();
        Log.i(LOG_TAG, "onCreate");

           }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        Log.i(LOG_TAG, "onStartCommand");

        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();

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

                Intent i = new Intent("location_update");
                i.putExtra("Latitud",String.valueOf(mLastLocation.getLatitude())+"°");
                i.putExtra("Longitud",String.valueOf(mLastLocation.getLongitude())+"°");
                i.putExtra("Precision",String.valueOf(mLastLocation.getAccuracy())+"m");
                i.putExtra("Altitud",String.valueOf(mLastLocation.getAltitude())+"m");
                i.putExtra("Velocidad",String.valueOf(mLastLocation.getSpeed())+"m/s");
                i.putExtra("Proveedor",String.valueOf(mLastLocation.getProvider()));
                i.putExtra( "fecha",String.valueOf(m.getFechaActual()));
                sendBroadcast(i);
                Log.e(LOG_TAG," Coordenadas"+ "Latitud\",String.valueOf(mLastLocation.getLatitude()));\n" +
                        "Longitud"+String.valueOf(mLastLocation.getLongitude())+
                        "Precision"+String.valueOf(mLastLocation.getAccuracy())+
                        "Altitud"+String.valueOf(mLastLocation.getAltitude())+
                        "Velocidad"+String.valueOf(mLastLocation.getSpeed())+
                        "Proveedor"+String.valueOf(mLastLocation.getProvider())+
                        "fecha"+String.valueOf(m.getFechaActual()));
            }

            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(Constantes.INTERVALOS_DETECCION_GPS_EN_MILISEGUNDOS);
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
        Log.e(LOG_TAG,"Localizacion"+location.toString());
        usr = m.cargarPreferencias(getBaseContext());
        fecha = m.getFechaActual();
        Boolean area1 = m.revisarArea(location.getLatitude(), location.getLongitude());
        Log.e(LOG_TAG,"msg comprueba si hay cambio de lugar");
        System.out.println("comprueba si hay cambio de lugar");
        if (m.lastlocation(location.getLatitude(),location.getLongitude())) {

            Intent i = new Intent("location_update");
            i.putExtra("Latitud",String.valueOf(mLastLocation.getLatitude())+"°");
            i.putExtra("Longitud",String.valueOf(mLastLocation.getLongitude())+"°");
            i.putExtra("Precision",String.valueOf(mLastLocation.getAccuracy())+"m");
            i.putExtra("Altitud",String.valueOf(mLastLocation.getAltitude())+"m");
            i.putExtra("Velocidad",String.valueOf(mLastLocation.getSpeed())+"m/s");
            i.putExtra("Proveedor",String.valueOf(mLastLocation.getProvider()));
            i.putExtra( "fecha",String.valueOf(m.getFechaActual()));
            sendBroadcast(i);

            //si se encuentra dentro del area capturamos los datos
        if (area1) {
            //si la aplicacion esta en el horario definido guardamos los datos
            if (m.rangoHoras(m.getHoraActual(), Constantes.horaInicial, Constantes.horaFinal)) {
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
            if (m.rangoHorassincronizacion(m.getHoraActual(), Constantes.horaActualizacion, Constantes.horaActualizacionf)) {
                //lista los datos para sincronizar
                ArrayList<HashMap<String, String>> userList = controller.getAllUsers();
                if (userList.size() != 0) {
                }
                m.syncSQLiteMySQLDB(getApplicationContext());
            }
        }
        }
        else{
            Log.e(LOG_TAG,"msg NO hubo cambio de lugar");
            System.out.println("NO hubo cambio de lugar");
            //mostramos los datos en cero
            Intent i = new Intent("location_update");
            i.putExtra("Latitud","0°");
            i.putExtra("Longitud","0°");
            i.putExtra("Precision","0 m");
            i.putExtra("Altitud","0 m");
            i.putExtra("Velocidad","0 m/s");
            i.putExtra("Proveedor","n/a");
            i.putExtra( "fecha",fecha);
            sendBroadcast(i);

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
            Log.e(LOG_TAG,"Latitud0 = " + location.getLatitude()
                    + "\n Longitud0 = " + location.getLongitude());
            System.out.println(" Latitud0 = " + location.getLatitude()
                    + "\n Longitud0 = " + location.getLongitude());
        }

    }

    private synchronized void buildGoogleApiClient() {
        Log.e(LOG_TAG,"Mensaje Build Google API");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }



}
