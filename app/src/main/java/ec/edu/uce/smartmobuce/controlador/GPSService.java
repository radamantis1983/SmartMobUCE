package ec.edu.uce.smartmobuce.controlador;


import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.OnNmeaMessageListener;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ec.edu.uce.smartmobuce.R;
import ec.edu.uce.smartmobuce.vista.GPSActivity;

public class GPSService extends Service {



    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener sensorEventListener;
    private long tt = 0;
     private double lat;
    private double longi;
    private double alt;
    //private Float cond;
    private double aux1=0,aux2=0;
    private float press;
    private float vel;
    private String usr;
    private String prov;
    private String fecha;
    private String mac;
    private double dop;
    private double dopv;
    private double doph;

    private OnNmeaMessageListener mOnNmeaMessageListener;
    private ArrayList<Localizacion> mGpsTestListeners = new ArrayList<Localizacion>();
    private final ControladorSQLite controller = new ControladorSQLite(this);
    private final Metodos m = new Metodos();

    private final int tiempoEspera = 30 * 1000;//inicializa el tiempo de espera para guardar datos al iniciar la aplicacion
    private final int actualizar_gps = 15 * 1000;//5min*60seg*1000= 5min  refresca la captura de los datos para luego
    // envia a la pantalla del activity gps
    private final String horaActualizacion = "01:00:00";// para sincronizar datos hora de inicio
    private final String horaActualizacionf = "01:30:00";//para sincronizar datos hora de fin
    private final String horaInicial = "06:00:00"; //horas de actividad inicio
    private final String horaFinal = "22:00:00";//horas de actividad fin
    private float sensor_x;
    private float sensor_y;
    private float sensor_z;
    private int sattelite_num;
    private GpsStatus.NmeaListener mLegacyNmeaListener;

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

                Intent c = new Intent("acelerometro_update");
                c.putExtra("acelerometro","Acelerometer \n x :" + sensor_x
                        + "\t y :" + sensor_y
                        + "\t z :" + sensor_z);
                sendBroadcast(c);

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        start1();


    }
    private void start1() {
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    private void stop1() {
        sensorManager.unregisterListener(sensorEventListener);
    }



    @SuppressLint("MissingPermission")
    private void locationStart() {
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localizacion Local = new Localizacion();

         boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (gpsEnabled==false) {
            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30*1000, 1, Local);
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30*1000, 1, Local);

        System.out.println("inicio gps");




    }




    /*
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            if (requestCode == 1000) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationStart();
                    return;
                }
            }
        }


        /* Aqui empieza la Clase Localizacion */
    public class Localizacion implements LocationListener, GpsTestListener{




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
            //addNmeaListenerAndroidN();//nemea

            usr = m.cargarPreferencias(getBaseContext());
            lat = loc.getLatitude();
            longi = loc.getLongitude();
            press = loc.getAccuracy();
            alt = loc.getAltitude();
            vel = loc.getSpeed();
            prov = loc.getProvider();
            fecha = m.getFechaActual();
            sattelite_num=loc.getExtras().getInt("satellites");
            //System.out.println("tiene  movimiento"+loc.hasSpeed());
            //System.out.println("tiene cambio de lugar"+lastlocation(lat,longi));
            System.out.println("comprueba si hay movimiento o cambio de lugar");
            if (loc.hasSpeed()||lastlocation(lat,longi)) {


                Intent i = new Intent("location_update");
                i.putExtra("coordenadas", coordenadas0
                        + "\n" + latitude1 + " : " + lat
                        + "\n" + longitude1 + " : " + longi
                        + "\n" + accuracy1 + " : " + press
                        + "\n" + altitude1 + " : " + alt
                        + "\n" + speed1 + " : " + vel
                        + "\n" + provider1 + " : " + prov
                        + "\n" + hour1 + " : " + m.getHoraActual()
                        + "\n" + date1 + " : " + fecha
                        + "\n x :" + sensor_x
                        + "\n y :" + sensor_y
                        + "\n z :" + sensor_z
                        + "\n saltelite :" + sattelite_num
                        + "\n dop :" + dop
                        + "\n dopv :" + dopv
                        + "\n dop :" + doph );
                sendBroadcast(i);
                Boolean area1 = m.revisarArea(loc.getLatitude(), loc.getLongitude());
                //si se encuentra dentro del area capturamos los datos
                if (area1) {
                    //si la aplicacion esta en el horario definido guardamos los datos
                    if (m.rangoHoras(m.getHoraActual(), horaInicial, horaFinal)) {
                        //prepara los datos a ser enviados al query de insertar datos a la base
                        HashMap<String, String> queryValues = new HashMap<String, String>();
                        queryValues.put("usu_id", usr);
                        queryValues.put("dat_latitud", String.valueOf(lat));
                        queryValues.put("dat_longitud", String.valueOf(longi));
                        queryValues.put("dat_precision", String.valueOf(press));
                        queryValues.put("dat_altitud", String.valueOf(alt));
                        queryValues.put("dat_velocidad", String.valueOf(vel));
                        queryValues.put("dat_proveedor", prov);
                        queryValues.put("dat_fechahora_lectura", fecha);
                        queryValues.put("dat_acelerometro_x", String.valueOf(sensor_x));
                        queryValues.put("dat_acelerometro_x", String.valueOf(sensor_y));
                        queryValues.put("dat_acelerometro_x", String.valueOf(sensor_z));
                        queryValues.put("dat_numero_sat",String.valueOf(sattelite_num ));
                        queryValues.put("dat_amplitud_sat", "0");
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

                System.out.println(" Latitud = " + loc.getLatitude()
                        + "\n Longitud = " + loc.getLongitude()
                        + "\n Proveedor = " + loc.getProvider());
            }else{
                System.out.println("el celular no esta en movimiento y tampoco hubo cambio de lugar");
                //mostramos los datos en cero
                Intent i = new Intent("location_update");
                i.putExtra("coordenadas", coordenadas0
                        + "\n" + latitude1 + " : " + 0.0
                        + "\n" + longitude1 + " : " + 0.0
                        + "\n" + accuracy1 + " : " + 0.0
                        + "\n" + altitude1 + " : " + 0.0
                        + "\n" + speed1 + " : " + 0.0
                        + "\n" + provider1 + " : " + "n/a"
                        + "\n" + hour1 + " : " + m.getHoraActual()
                        + "\n" + date1 + " : " + fecha
                        + "\n x :" + sensor_x
                        + "\n y :" + sensor_y
                        + "\n z :" + sensor_z
                        + "\n saltelite :" + 0
                        + "\n dop :" + 0.0
                        + "\n dopv :" + 0.0
                        + "\n dop :" + 0.0
                );
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
                queryValues.put("dat_acelerometro_x", "0.0");
                queryValues.put("dat_acelerometro_x", "0.0");
                queryValues.put("dat_acelerometro_x", "0.0");
                queryValues.put("dat_numero_sat","0");
                queryValues.put("dat_amplitud_sat", "0");
                controller.insertDatos(queryValues);
                System.out.println(" Latitud0 = " + loc.getLatitude()
                        + "\n Longitud0 = " + loc.getLongitude());
            }




            // permite guardar un respaldo de la base de datos en la carpeta my documents
            //        m.backupdDatabase(getApplicationContext());


        }

        @Override
        public void onProviderDisabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es desactivado
            Intent k = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            k.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(k);


        }

        @Override
        public void onProviderEnabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es activado
            locationStart();

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

    public void onNmeaMessage(String message, long timestamp) {
        if (message.startsWith("$GNGSA") || message.startsWith("$GPGSA")) {
            DilutionOfPrecision dop1 = GpsTestUtil.getDop(message);
            if (dop1 != null ) {
                dop=dop1.getPositionDop();
                doph=dop1.getHorizontalDop();
                dopv=dop1.getVerticalDop();
                System.out.println(dop);
                System.out.println(doph);
                System.out.println(dopv);

            }
        }
    }

    }
    private void addNmeaListener() {
        if (GpsTestUtil.isGnssStatusListenerSupported()) {
            addNmeaListenerAndroidN();
        } else {
            addLegacyNmeaListener();
        }
    }
    @SuppressLint("MissingPermission")
    private void addLegacyNmeaListener() {
        LocationManager mlocManager1 = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (mLegacyNmeaListener == null) {
            mLegacyNmeaListener = new GpsStatus.NmeaListener() {
                @Override
                public void onNmeaReceived(long timestamp, String nmea) {
                    for (GpsTestListener listener : mGpsTestListeners) {
                        listener.onNmeaMessage(nmea, timestamp);
                    }

                }
            };
        }
        mlocManager1.addNmeaListener(mLegacyNmeaListener);
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addNmeaListenerAndroidN() {
        LocationManager mlocManager1 = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (mOnNmeaMessageListener == null) {
            mOnNmeaMessageListener = new OnNmeaMessageListener() {
                @Override
                public void onNmeaMessage(String message, long timestamp) {
                    for (Localizacion listener : mGpsTestListeners) {
                        listener.onNmeaMessage(message, timestamp);
                    }
                }
            };
        }
        mlocManager1.addNmeaListener(mOnNmeaMessageListener);
    }
public boolean lastlocation(double last_latitud, double last_longitud){
    System.out.println("latitud anterior"+aux1);
    System.out.println("longitud anterior"+aux2);
    boolean estado=false;
        if(aux1!=last_latitud || aux2!=last_longitud){
            aux1=last_latitud;
            aux2=last_longitud;
            System.out.println("cambio de lugar");
            estado =true;
            return estado;
        }
        else{
            System.out.println("no cambio de lugar");
            estado=false;
            return estado;
        }

}

}

