package ec.edu.uce.smartmobuce.controlador;


import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ec.edu.uce.smartmobuce.R;
import ec.edu.uce.smartmobuce.vista.GPSActivity;

public class GPSService extends Service {

    //private LocationListener listener;
    //private LocationManager locationManager;
    private LocationManager mlocManager;
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


    public GPSService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate() {


        locationStart();


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

    private void start1() {
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    private void stop1() {
        sensorManager.unregisterListener(sensorEventListener);
    }
*/
    }
    @SuppressLint("MissingPermission")
    private void locationStart() {
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localizacion Local = new Localizacion();
        //Local.setMainGPSActivity(ec.edu.uce.gps.GPSActivity);
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }

        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30*1000, 0, (LocationListener) Local);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30*1000, 0, (LocationListener) Local);
        System.out.println("inicio gps");
        // mensaje1.setText("Localizacion agregada");
        // mensaje2.setText("");
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationStart();
                return;
            }
        }
    }

    public void setLocation(Location loc) {
        //Obtener la direccion de la calle a partir de la latitud y la longitud
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                    String coordenadas0 = getString(R.string.coordenadas1);


                    Intent i = new Intent("location_update1");
                    i.putExtra("mensaje2", coordenadas0
                            + "Mi direccion es: \n"
                            + DirCalle.getAddressLine(0)
                    );
                    sendBroadcast(i);
                    //  mensaje2.setText();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /* Aqui empieza la Clase Localizacion */
    public class Localizacion implements LocationListener {
        GPSActivity mainGPSActivity;

        public GPSActivity getMainGPSActivity() {
            return mainGPSActivity;
        }

        public void setMainGPSActivity(GPSActivity mainGPSActivity) {
            this.mainGPSActivity = mainGPSActivity;
        }

        @Override
        public void onLocationChanged(Location loc) {
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la deteccion de un cambio de ubicacion

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

            //this.mainGPSActivity.setLocation(loc);
            // permite guardar un respaldo de la base de datos en la carpeta my documents
            //        m.backupdDatabase(getApplicationContext());

        }

        @Override
        public void onProviderDisabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es desactivado
            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            String coordenadas0 = getString(R.string.coordenadas1);
            String latitude1 = getString(R.string.latitude);

            Intent i1 = new Intent("location_update");
            i1.putExtra("coordenadas", " GPS Disable");
            sendBroadcast(i);

        }

        @Override
        public void onProviderEnabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es activado
            String coordenadas0 = getString(R.string.coordenadas1);

            Intent i = new Intent("location_update");
            i.putExtra("coordenadas", coordenadas0 + " : GPS Activado");
            sendBroadcast(i);


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    }
}

