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
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ec.edu.uce.smartmobuce.R;
import ec.edu.uce.smartmobuce.vista.GPSActivity;

import static android.content.ContentValues.TAG;
import static ec.edu.uce.smartmobuce.controlador.GpsTestUtil.getDop;

public class GPSService extends Service implements SensorEventListener,LocationListener,GpsTestListener {


    DecimalFormat df = new DecimalFormat("#.0000");
    private LocationManager locationManager;
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
    private String pdop="", hdop="", vdop="";
    private String pdop1="0", hdop1="0", vdop1="0";
    private double dop;
    private double dopv;
    private double doph;

    boolean isGPSEnabled = false;
    Location location;
    boolean estado1=false;

    private OnNmeaMessageListener mOnNmeaMessageListener;
    //private ArrayList<Localizacion> mGpsTestListeners = new ArrayList<Localizacion>();
    private final ControladorSQLite controller = new ControladorSQLite(this);
    private final Metodos m = new Metodos();

    private final int tiempoEspera = 15 * 1000;//inicializa el tiempo de espera para guardar datos al iniciar la aplicacion
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


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor  = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor , SensorManager.SENSOR_DELAY_NORMAL);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15*1000, 0, this);


    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Servicio iniciado...");

        return START_STICKY;
    }

        @Override
    public void onSensorChanged(SensorEvent event) {

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                sensor_x = event.values[0];
                sensor_y = event.values[1];
                sensor_z = event.values[2];
             /*   System.out.println("ACELEROMETRO");
                System.out.println(sensor_x);
                System.out.println(sensor_y);
                System.out.println(sensor_z);
*/
                Intent c = new Intent("acelerometro_update");
                c.putExtra("acelerometro","Acelerometer \n x :" + sensor_x
                        + "\t y :" + sensor_y
                        + "\t z :" + sensor_z
                        + "\t pod :" + pdop1
                        + "\t hdop :" + hdop1
                        + "\t vdop :" + vdop1

                );
                sendBroadcast(c);
            }


    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(Location loc) {

        locationManager.addNmeaListener(new GpsStatus.NmeaListener() {

            public void onNmeaReceived(long timestamp, String nmea) {

                //Log.d(TAG,"Nmea Received :");
                //Log.d(TAG,"Timestamp is :" +timestamp+"   nmea is :"+nmea);
                String[] tokens = nmea.split(",");
                if (nmea.startsWith("$GNGSA") || nmea.startsWith("$GPGSA")) {
  
                    try {
                        pdop = tokens[15];
                        hdop = tokens[16];
                        vdop = tokens[17];

                        if (vdop.contains("*")) {
                            vdop = vdop.split("\\*")[0];
                        }
                        pdop1 =pdop;
                        hdop1 = hdop;
                        vdop1 = vdop;

                        Log.e(TAG, "recibiendo valor pod");
                        System.out.println("valor PDOP:: " + pdop + " el dato H: " + hdop + " el dato v es: " + vdop);
                        Log.e(TAG, "valor PDOP: " + pdop + "el dato H" + hdop + "el dato v es " + vdop);
                        if (pdop.isEmpty()) {
                            pdop1 = "0.0";
                        }
                        if (vdop.isEmpty()) {
                            vdop1 = "0.0";
                        }
                        if (hdop.isEmpty()) {
                            hdop1 = "0.0";
                        }
                        Log.e(TAG, "recibiendo  valor 000000");
                        System.out.println("valor PDOP:: " + pdop1 + " el dato H: " + hdop1 + " el dato v es: " + vdop1);
                        Log.e(TAG, "valor PDOP: " + pdop1 + "el dato H" + hdop1 + "el dato v es " + vdop1);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Log.e(TAG, "Bad NMEA message for parsing DOP - " + nmea + " :" + e);

                    }
                    // See https://github.com/barbeau/gpstest/issues/71#issuecomment-263169174


                }




            }});
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
        if (loc.hasSpeed()&&vel!=0.0||(lastlocation(lat,longi) )) {
            System.out.println("hubo velocidad"+(loc.hasSpeed()&&vel!=0.0));

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
                    + "\n satelite :" + sattelite_num
                    + "\n dop :" + pdop1
                    + "\n dopv :" + vdop1
                    + "\n dop :" + hdop1 );
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
                    queryValues.put("dat_pdop",String.valueOf(pdop1 ));
                    queryValues.put("dat_hdop",String.valueOf(hdop1 ));
                    queryValues.put("dat_vdop",String.valueOf(vdop1 ));

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

            System.out.println(" Latitud = " + truncateDecimal(loc.getLatitude(),4)
                    + "\n Longitud = " + truncateDecimal(loc.getLongitude(),4)
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
                    + "\n x :" + 0.0
                    + "\n y :" + 0.0
                    + "\n z :" + 0.0
                    + "\n satelite :" + 0
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
            queryValues.put("dat_pdop","0.0");
            queryValues.put("dat_hdop","0.0");
            queryValues.put("dat_vdop","0.0");
            controller.insertDatos(queryValues);
            System.out.println(" Latitud0 = " + loc.getLatitude()
                    + "\n Longitud0 = " + loc.getLongitude());
        }




        // permite guardar un respaldo de la base de datos en la carpeta my documents
        //        m.backupdDatabase(getApplicationContext());



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

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        // Este metodo se ejecuta cuando el GPS es desactivado
        Intent k = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        k.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(k);

    }

    @Override
    public void onNmeaMessage(String message, long timestamp) {
        if (message.startsWith("$GNGSA") || message.startsWith("$GPGSA")) {
            DilutionOfPrecision dop1 = getDop(message);
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
    private boolean lastlocation(double last_latitud, double last_longitud){

        double a=truncateDecimal(last_latitud,4);
        double b=truncateDecimal(last_longitud,4);
        System.out.println("latitud a comparar"+a);
        System.out.println("longitud a comparar"+b);
        System.out.println("latitud anterior" + aux1);//alamacenar solo 4 digitos0.0000
        System.out.println("longitud anterior" + aux2);//alamacenar solo 4 digitos

        if(aux1!=a || aux2!=b){
            aux1= truncateDecimal(last_latitud,4);
            aux2= truncateDecimal(last_longitud,4);

            System.out.println("cambio de lugar true");
            estado1 =true;
            return estado1;
        }
        else{
            System.out.println("no cambio de lugar");
            estado1=false;
            return estado1;
        }

    }
    private static double truncateDecimal(double x,int numberofDecimals)
    {
        if ( x > 0) {
            return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_FLOOR).doubleValue();
        } else {
            return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_CEILING).doubleValue();
        }
    }
}

