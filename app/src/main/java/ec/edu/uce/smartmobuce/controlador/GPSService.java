package ec.edu.uce.smartmobuce.controlador;


import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.HashMap;

import ec.edu.uce.smartmobuce.R;

public class GPSService extends Service {

    private LocationListener listener;
    private LocationManager locationManager;

    private long tt = 0;

    private Double lat;
    private Double longi;
    private Double alt;
    //private Float cond;
    private Float press;
    private Float vel;
    private String usr;
    private String prov;
    private String fecha;
    private String mac;


    private final ControladorSQLite controller = new ControladorSQLite(this);
    private final Metodos m = new Metodos();

    private final int tiempoEspera = 60 * 1000;//inicializa el tiempo de espera para guardar datos al iniciar la aplicacion
    private final int actualizar_gps=30*1000;//5min*60seg*1000= 5min  refresca la captura de los datos para luego
    // envia a la pantalla del activity gps
    private final String horaActualizacion = "00:00:00";// para sincronizar datos hora de inicio
    private final String horaActualizacionf = "00:15:00";//para sincronizar datos hora de fin
    private final String horaInicial = "06:00:00"; //horas de actividad inicio
    private final String horaFinal = "22:00:00";//horas de actividad fin



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        listener = new LocationListener() {


            @Override
            public void onLocationChanged(Location loc) {


                //me permite mostrar el mac adrres del dispositivo
                WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = manager.getConnectionInfo();
                String address = info.getMacAddress();


                Boolean area1 = m.revisarArea(loc.getLatitude(), loc.getLongitude());

                //datos a guardar en variables para luego guardar en la base

                usr=m.cargarPreferencias(getBaseContext());
                lat = loc.getLatitude();
                longi = loc.getLongitude();
                //cond = loc.getBearing();
                press = loc.getAccuracy();
                alt = loc.getAltitude();
                vel = loc.getSpeed();
                prov = loc.getProvider();
                mac = address;
                fecha = m.getFechaActual();
                String coordenadas0=getString(R.string.coordenadas1);
                String latitude1=getString(R.string.latitude);
                String longitude1=getString(R.string.longitude);
                // String bearing1=getString(R.string.bearing);
                String accuracy1=getString(R.string.accuracy);
                String altitude1=getString(R.string.altitude);
                String speed1=getString(R.string.speed);
                String provider1=getString(R.string.provider);
                String hour1=getString(R.string.hour);
                String date1=getString(R.string.date);


                Intent i = new Intent("location_update");
                i.putExtra("coordenadas", coordenadas0
                        +"\n"+latitude1+" : " + loc.getLatitude()
                        +"\n"+longitude1+" : " + longi
                        //  +"\n"+bearing1+" : " + cond
                        +"\n"+accuracy1+" : " + press
                        +"\n"+altitude1+" : " + alt
                        +"\n"+speed1+" : " + vel
                        +"\n"+provider1+" : " + prov
                        //+ "\n MAC address = " + address
                        //+ "\n estado = " + area1
                        //+ "\n contador = " + tt
                        +"\n"+hour1+" : " + m.getHoraActual()
                        +"\n"+date1+" : " + m.getFechaActual());
                sendBroadcast(i);

/*
*/
                //si se encuentra dentro del area capturamos los datos
                if (area1) {
                    //si el tiempo llega a 1 y la hora esta en el rango definido guardamos los datos


                    //if ((tt == 1) && m.rangoHoras(m.getHoraActual(), horaInicial, horaFinal)) {
                    if ( m.rangoHoras(m.getHoraActual(), horaInicial, horaFinal)) {
                        //prepara los datos a ser enviados al query de insertar datos a la base

                        HashMap<String, String> queryValues = new HashMap<String, String>();

                        queryValues.put("usu_id", usr.toString());
                        queryValues.put("dat_latitud", lat.toString());
                        queryValues.put("dat_longitud", longi.toString());
                        queryValues.put("dat_precision", press.toString());
                        queryValues.put("dat_altitud", alt.toString());
                        queryValues.put("dat_velocidad", vel.toString());
                        queryValues.put("dat_proveedor", prov);
                        queryValues.put("dat_fechahora_lectura", fecha);

                        controller.insertDatos(queryValues);

/*
                        //lista los datos para sincronizar
                        ArrayList<HashMap<String, String>> userList = controller.getAllUsers();
                        if (userList.size() != 0) {

                        }
                        //tt = 0;
*/
                    }
                    //comprueba la hora para sincronizacón con la base de datos
  /*                  if (m.compararHoras(m.getHoraActual(), horaActualizacion)) {
                        System.out.println("sincronizando");
                        System.out.println(horaActualizacion+"la hora de sincronizacion");
                        m.syncSQLiteMySQLDB(getApplicationContext());


                    }
*/




                    //comprueba la hora para sincronizacón con la base de datos
                    if (m.rangoHorassincronizacion(m.getHoraActual(),horaActualizacion,horaActualizacionf)) {
                        //lista los datos para sincronizar


                        ArrayList<HashMap<String, String>> userList = controller.getAllUsers();
                        if (userList.size() != 0) {

                        }
                        m.syncSQLiteMySQLDB(getApplicationContext());


                    }


                }
                System.out.println( " Latitud = " + loc.getLatitude()
                        + "\n Longitud = " + loc.getLongitude());

            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };



        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, actualizar_gps, 0, listener);
        System.out.println("ejecuto listener 2");
        // permite guardar un respaldo de la base de datos en la carpeta my documents
        //        m.backupdDatabase(getApplicationContext());
/*
        final CountDownTimer start = new CountDownTimer(tiempoEspera, 1000) {

            public void onTick(long millisUntilFinished) {
                tt = (millisUntilFinished / 1000);
              //  System.out.println(tt);
            }


            @SuppressLint("MissingPermission")
            public void onFinish() {


                start();
          /*      System.out.println("start");

                locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);


                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30*1000, 0, listener);
                System.out.println("ejecuto listener");


            }

        }.start();

*/



    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
        }
    }


}
