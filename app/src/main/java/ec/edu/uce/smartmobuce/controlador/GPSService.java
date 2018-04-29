package ec.edu.uce.smartmobuce.controlador;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import ec.edu.uce.smartmobuce.R;
import ec.edu.uce.smartmobuce.vista.GPSActivity;

public class GPSService extends Service {

    private LocationListener listener;
    private LocationManager locationManager;
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener sensorEventListener;
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

    private final int tiempoEspera = 30 * 1000;//inicializa el tiempo de espera para guardar datos al iniciar la aplicacion
    private final int actualizar_gps = 15 * 1000;//5min*60seg*1000= 5min  refresca la captura de los datos para luego
    // envia a la pantalla del activity gps
    private final String horaActualizacion = "00:00:00";// para sincronizar datos hora de inicio
    private final String horaActualizacionf = "00:15:00";//para sincronizar datos hora de fin
    private final String horaInicial = "06:00:00"; //horas de actividad inicio
    private final String horaFinal = "22:00:00";//horas de actividad fin
    private float sensor_x;
    private float sensor_y;
    private float sensor_z;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {

     /*   final CountDownTimer start = new CountDownTimer(tiempoEspera, 1000) {

            public void onTick(long millisUntilFinished) {
                tt = (millisUntilFinished / 1000);
                //  System.out.println(tt);
            }


            @SuppressLint("MissingPermission")
            public void onFinish() {


                start();
                System.out.println("start");

                locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
                System.out.println("ejecuto listener");


            }

        }.start();

*/
        listener = new LocationListener() {


            @Override
            public void onLocationChanged(Location loc) {
                String coordenadas0 = getString(R.string.coordenadas1);
                String latitude1 = getString(R.string.latitude);
                String longitude1 = getString(R.string.longitude);
                String accuracy1 = getString(R.string.accuracy);
                String altitude1 = getString(R.string.altitude);
                String speed1 = getString(R.string.speed);
                String provider1 = getString(R.string.provider);
                String hour1 = getString(R.string.hour);
                String date1 = getString(R.string.date);
                System.out.println("x:" + sensor_x);
                System.out.println("y:" + sensor_y);
                System.out.println("z:" + sensor_z);

                Intent i = new Intent("location_update");
                i.putExtra("coordenadas", coordenadas0
                        + "\n" + latitude1 + " : " + loc.getLatitude()
                        + "\n" + longitude1 + " : " + loc.getLongitude()
                        + "\n" + accuracy1 + " : " + loc.getAccuracy()
                        + "\n" + altitude1 + " : " + loc.getAltitude()
                        + "\n" + speed1 + " : " + loc.getSpeed()
                        + "\n" + provider1 + " : " + loc.getProvider()
                        + "\n" + hour1 + " : " + m.getHoraActual()
                        + "\n" + date1 + " : " + m.getFechaActual()
                        + "\n x :" + sensor_x
                        + "\n y :" + sensor_y
                        + "\n z :" + sensor_z);
                sendBroadcast(i);


                Boolean area1 = m.revisarArea(loc.getLatitude(), loc.getLongitude());

                //datos a guardar en variables para luego guardar en la base

                usr = m.cargarPreferencias(getBaseContext());
                lat = loc.getLatitude();
                longi = loc.getLongitude();
                press = loc.getAccuracy();
                alt = loc.getAltitude();
                vel = loc.getSpeed();
                prov = loc.getProvider();
                fecha = m.getFechaActual();

/*
*/
                //si se encuentra dentro del area capturamos los datos
                if (area1) {
                    //si el tiempo llega a 1 y la hora esta en el rango definido guardamos los datos


                    //if ((tt == 1) && m.rangoHoras(m.getHoraActual(), horaInicial, horaFinal)) {
                    if (m.rangoHoras(m.getHoraActual(), horaInicial, horaFinal)) {
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
                    if (m.rangoHorassincronizacion(m.getHoraActual(), horaActualizacion, horaActualizacionf)) {
                        //lista los datos para sincronizar


                        ArrayList<HashMap<String, String>> userList = controller.getAllUsers();
                        if (userList.size() != 0) {

                        }
                        m.syncSQLiteMySQLDB(getApplicationContext());


                    }


                }
                System.out.println(" Latitud = " + loc.getLatitude()
                        + "\n Longitud = " + loc.getLongitude());
                sendBroadcast(i);
//                start1();

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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, listener);
        System.out.println("inicio gps");


        //   locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission

        // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, actualizar_gps, 0, listener);
        //System.out.println("ejecuto listener gps");
        // permite guardar un respaldo de la base de datos en la carpeta my documents
        //        m.backupdDatabase(getApplicationContext());


/*
//sensor acelerometro
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sensor == null)
            Toast.makeText(getApplicationContext(), "dispositivo no cuenta con acelerometro", Toast.LENGTH_SHORT).show();
        sensorEventListener = new SensorEventListener() {

            @SuppressLint("MissingPermission")
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                sensor_x = sensorEvent.values[0];
                sensor_y = sensorEvent.values[1];
                sensor_z = sensorEvent.values[2];
                //System.out.println("valor giro x" + sensor_x);
                //System.out.println("valor giro y" + sensor_y);
                //System.out.println("valor giro z" + sensor_z);
                if ((sensor_x > 0 && sensor_x < 1) && (sensor_y > -1 && sensor_y < 0.3) && (sensor_z < -10 || sensor_z > 9)) {

                    //System.out.println(tt++);
                    tt++;
                    if (tt > 333) {
                        System.out.println("device NO esta en movimiento gps inactivo");
                        stop1();
                        tt = 0;

                    }
                } else {
                    tt++;
                    //System.out.println(tt++);
                    if (tt > 150) {
                        System.out.println("valor giro x" + sensor_x);
                        System.out.println("device ESTA en movimiento gps activo");
                        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

                        //noinspection MissingPermission


                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30*1000, 0, listener);
                        System.out.println("ejecuto listener gps");
                        tt = 0;
                    }
                }


            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        start1();
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

/*
    private void start1() {
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    private void stop1() {
        sensorManager.unregisterListener(sensorEventListener);
    }

*/



}
